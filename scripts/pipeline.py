#!/usr/bin/env python3
"""Manager-owned task registry and gates for isolated Git worktrees."""
import argparse
from contextlib import contextmanager
from datetime import datetime, timezone
import fcntl
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys

sys.dont_write_bytecode = True

from role_boundary import check, git


def sha(root):
    return git(root, 'rev-parse', 'HEAD').decode().strip()


def clean(root):
    return not git(root, 'status', '--porcelain', '--untracked-files=all').strip()


def snapshot(root):
    digest = hashlib.sha256(sha(root).encode())
    digest.update(git(root, 'diff', '--binary', '--no-ext-diff', 'HEAD'))
    digest.update(git(root, 'diff', '--cached', '--binary', '--no-ext-diff', 'HEAD'))
    for raw in git(root, 'ls-files', '--others', '--exclude-standard', '-z').split(b'\0'):
        if raw:
            p = root / os.fsdecode(raw)
            digest.update(raw + b'\0')
            digest.update(os.fsencode(os.readlink(p)) if p.is_symlink() else p.read_bytes())
    return digest.hexdigest()


@contextmanager
def lock(path):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open('a') as handle:
        try:
            fcntl.flock(handle, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError:
            raise ValueError('다른 관리자 명령이 실행 중입니다: ' + str(path))
        yield


def save(path, data):
    data['updated_at'] = datetime.now(timezone.utc).isoformat()
    temp = path.with_suffix('.tmp')
    temp.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')
    temp.replace(path)


def role(workspace, value):
    (workspace / '.work').mkdir(exist_ok=True)
    (workspace / '.work/role').write_text(value + '\n')


def require(state, *values):
    if state['status'] not in values:
        raise ValueError(f"현재 상태 {state['status']}; 필요한 상태: {values}")


def run_logged(command, root, log):
    print('실행:', repr(command), '로그:', log, flush=True)
    with log.open('w') as output:
        return subprocess.run(command, cwd=root, stdout=output, stderr=subprocess.STDOUT).returncode


def scope(state, workspace):
    paths = check(workspace, 'writer', state['test_commit'])
    for path in paths:
        if not any(path == p or path.startswith(p.rstrip('/') + '/') for p in state['write_paths']):
            raise ValueError('작업 범위 밖 변경: ' + path)
        if path.startswith('src/main/resources/db/migration/'):
            if path != state.get('migration'):
                raise ValueError('예약되지 않은 마이그레이션: ' + path)
            if subprocess.run(['git', '-C', str(workspace), 'cat-file', '-e', state['test_commit'] + ':' + path],
                              stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode == 0:
                raise ValueError('기존 마이그레이션은 수정할 수 없습니다: ' + path)


def version(migration):
    return int(Path(migration).name.split('__')[0][1:])


def register(args, root, registry):
    if not re.fullmatch(r'[a-z0-9][a-z0-9-]*', args.task):
        raise ValueError('작업 ID는 소문자, 숫자, 하이픈만 사용합니다.')
    workspace = Path(args.workspace).resolve()
    if workspace == root or Path(git(workspace, 'rev-parse', '--show-toplevel').decode().strip()).resolve() != workspace:
        raise ValueError('관리자와 다른 독립 worktree 루트를 지정하세요.')
    common = lambda p: Path(git(p, 'rev-parse', '--path-format=absolute', '--git-common-dir').decode().strip()).resolve()
    if common(root) != common(workspace):
        raise ValueError('같은 저장소의 worktree만 등록할 수 있습니다.')
    if not clean(workspace) or sha(root) != sha(workspace):
        raise ValueError('작업 공간은 관리자 HEAD에서 분기한 깨끗한 상태여야 합니다.')
    branch = git(workspace, 'symbolic-ref', '--short', 'HEAD').decode().strip()
    spec = f'docs/tasks/{args.task}.md'
    git(workspace, 'cat-file', '-e', 'HEAD:' + spec)
    paths = args.write_path
    if any(not p.startswith('src/main/') or '..' in p.split('/') or '*' in p for p in paths):
        raise ValueError('--write-path는 src/main/ 아래의 실제 파일/폴더 경로여야 합니다.')
    if args.migration:
        if not re.fullmatch(r'src/main/resources/db/migration/V[0-9]+__[A-Za-z0-9_]+\.sql', args.migration):
            raise ValueError('마이그레이션 경로 형식: src/main/resources/db/migration/V번호__설명.sql')
        existing = [version(p) for p in (workspace / 'src/main/resources/db/migration').glob('V*__*.sql')]
        if existing and version(args.migration) <= max(existing):
            raise ValueError(f'Flyway 버전은 기존 최대 버전 V{max(existing)}보다 커야 합니다.')
        paths = paths + [args.migration]
    path = registry / (args.task + '.json')
    if path.exists() or (workspace / '.work/task.json').exists():
        raise ValueError('이미 등록된 작업 또는 작업 공간입니다.')
    for other in registry.glob('*.json'):
        data = json.loads(other.read_text())
        if data['workspace'] == str(workspace):
            raise ValueError('이미 다른 작업에 배정된 worktree입니다.')
        if data['status'] == 'integrated':
            continue
        for left in paths:
            for right in data['write_paths']:
                if left == right or left.startswith(right.rstrip('/') + '/') or right.startswith(left.rstrip('/') + '/'):
                    raise ValueError(f"작업 {data['task']}와 수정 경로가 겹칩니다: {left}")
        if args.migration and data.get('migration'):
            if version(args.migration) == version(data['migration']):
                raise ValueError('다른 작업이 같은 Flyway 버전을 예약했습니다.')
    data = dict(task=args.task, workspace=str(workspace), branch=branch, spec=spec,
                base=sha(workspace), status='testing', rejects=0, attempt=0,
                write_paths=paths, migration=args.migration)
    role(workspace, 'verifier')
    save(workspace / '.work/task.json', {'manager': str(root), 'task': args.task})
    save(path, data)
    print(json.dumps(data, ensure_ascii=False, indent=2))


def task_command(args, root, registry):
    path = registry / (args.task + '.json')
    state = json.loads(path.read_text())
    workspace = Path(state['workspace'])
    out = root / '.work/results' / args.task
    out.mkdir(parents=True, exist_ok=True)
    if args.command == 'tests-ready':
        require(state, 'testing')
        if sha(workspace) != state['base']:
            raise ValueError('검증자가 커밋을 만들었습니다. 관리자가 변경을 확인하세요.')
        if not check(workspace, 'verifier', state['base']):
            raise ValueError('작성된 테스트가 없습니다.')
        deleted = [p for p in (git(workspace, 'diff', '--no-renames', '--name-only', '--diff-filter=D', '-z', state['base'])
                               + git(workspace, 'diff', '--cached', '--no-renames', '--name-only', '--diff-filter=D', '-z',
                                     state['base'])).split(b'\0') if p]
        if deleted:
            raise ValueError('기존 테스트 파일을 삭제할 수 없습니다: ' + repr(sorted({os.fsdecode(p) for p in deleted})))
        report = Path(args.report).read_text()
        before = snapshot(workspace)
        code = run_logged(['./gradlew', 'test', '--tests', args.tests], workspace, out / 'red.log')
        if code == 0:
            raise ValueError('Red 단계에서 테스트가 통과했습니다. 테스트와 명세를 확인하세요.')
        if code in (126, 127) or snapshot(workspace) != before:
            raise ValueError('테스트 실행 실패 또는 실행 중 파일 변경. red.log를 확인하세요.')
        # Manager must inspect red.log: infrastructure failure is never a valid Red.
        if 'RED_REASON: missing-implementation' not in report:
            raise ValueError('Red 보고서에 RED_REASON: missing-implementation 판정이 필요합니다.')
        (out / 'red-review.md').write_text(report)
        git(workspace, 'add', '--', 'src/test')
        git(workspace, 'commit', '-m', 'test: ' + args.task)
        state.update(status='ready', test_commit=sha(workspace))
        role(workspace, 'writer')
    elif args.command == 'run-writer':
        require(state, 'ready', 'rejected', 'failed')
        if sha(workspace) != state['test_commit']:
            raise ValueError('테스트 커밋 이후 HEAD가 바뀌었습니다.')
        scope(state, workspace)
        state['attempt'] += 1
        state['status'] = 'running'
        save(path, state)
        role(workspace, 'writer')
        report = workspace / '.work' / f"writer-{state['attempt']}.md"
        prompt = (f"너는 작성자(writer)다. AGENTS.md, CLAUDE.md, docs/CODE_CONVENTIONS.md와 {state['spec']}를 읽어라. "
                  f"허용 경로: {state['write_paths']}. src/test 및 설정/문서/스크립트 수정과 git commit은 금지한다. "
                  '테스트를 통과하는 최소 구현을 작성하고 변경 파일, 테스트 명령/결과, 남은 문제를 보고하라. '
                  + args.extra)
        if state.get('review_report'):
            prompt += '\n이전 리뷰:\n' + Path(state['review_report']).read_text()
        try:
            code = run_logged(['codex', 'exec', '-s', 'workspace-write', '-C', str(workspace),
                               '-o', str(report), prompt], workspace, out / f"writer-{state['attempt']}.log")
            if code != 0 or not report.exists():
                raise ValueError('작성자 실행 실패. 로그를 확인한 뒤 재실행하세요.')
            if sha(workspace) != state['test_commit']:
                raise ValueError('작성자가 HEAD를 변경했습니다.')
            scope(state, workspace)
            saved_report = out / report.name
            saved_report.write_text(report.read_text())
            state.update(status='review', snapshot=snapshot(workspace), writer_report=str(saved_report))
            role(workspace, 'reviewer')
        except BaseException:
            state['status'] = 'failed'
            save(path, state)
            raise
    elif args.command == 'review':
        require(state, 'review')
        report = Path(args.report).read_text()
        current = snapshot(workspace)
        if current != state['snapshot'] or f'SNAPSHOT: {current}' not in report.splitlines():
            raise ValueError('리뷰 대상이 변경되었거나 보고서 SNAPSHOT이 일치하지 않습니다.')
        verdicts = re.findall(r'^VERDICT: (PASS|REJECT)$', report, re.M)
        if len(verdicts) != 1:
            raise ValueError('보고서에 VERDICT: PASS 또는 REJECT 한 줄이 필요합니다.')
        scope(state, workspace)
        review_file = out / f"review-{state['attempt']}.md"
        review_file.write_text(report)
        state['review_report'] = str(review_file)
        if verdicts[0] == 'REJECT':
            state['rejects'] += 1
            state['status'] = 'blocked' if state['rejects'] >= 3 else 'rejected'
            role(workspace, 'writer')
        else:
            code = run_logged(['./gradlew', 'check'], workspace, out / f"check-{state['attempt']}.log")
            if code != 0:
                raise ValueError('전체 검증 실패. check 로그를 확인하세요. 상태는 review로 유지합니다.')
            if snapshot(workspace) != current:
                raise ValueError('검증 중 파일이 변경되었습니다. 다시 리뷰하세요.')
            scope(state, workspace)
            git(workspace, 'add', '--', 'src/main')
            git(workspace, 'commit', '-m', 'feat: ' + args.task)
            state.update(status='accepted', accepted_commit=sha(workspace))
            role(workspace, 'reviewer')
    elif args.command == 'refresh-review':
        require(state, 'review')
        if sha(workspace) != state['test_commit']:
            raise ValueError('HEAD가 변경되었습니다.')
        scope(state, workspace)
        state['snapshot'] = snapshot(workspace)
    elif args.command == 'recover':
        require(state, 'running')
        # Explicit operator acknowledgement; do not infer process death from elapsed time.
        if not args.worker_stopped:
            raise ValueError('실행 프로세스 종료를 확인한 뒤 --worker-stopped를 지정하세요.')
        state['status'] = 'failed'
    elif args.command == 'integrate':
        require(state, 'accepted', 'integration-failed')
        if not clean(workspace) or sha(workspace) != state['accepted_commit']:
            raise ValueError('승인 이후 작업 공간이 변경되었습니다.')
        merge_head = subprocess.run(['git', '-C', str(root), 'rev-parse', '-q', '--verify', 'MERGE_HEAD'],
                                    capture_output=True, text=True)
        if merge_head.returncode == 0:
            if state['status'] != 'integration-failed' or merge_head.stdout.strip() != state['accepted_commit']:
                raise ValueError('다른 병합이 진행 중입니다.')
        else:
            if not clean(root):
                raise ValueError('관리자 작업 공간을 먼저 정리하세요. 기존 변경을 자동으로 되돌리지 않습니다.')
            # Flyway rejects a lower version once a higher one is applied, so integrate in version order.
            if state.get('migration'):
                for other in registry.glob('*.json'):
                    data = json.loads(other.read_text())
                    if (data['task'] != state['task'] and data['status'] != 'integrated' and data.get('migration')
                            and version(data['migration']) < version(state['migration'])):
                        raise ValueError(f"Flyway 순서: 낮은 버전을 가진 작업 {data['task']}를 먼저 통합하세요.")
            state['integration_base'] = sha(root)
            state['status'] = 'integration-failed'
            save(path, state)
            code = run_logged(['git', 'merge', '--no-ff', '--no-commit', state['accepted_commit']], root, out / 'merge.log')
            if code != 0:
                raise ValueError('병합 충돌. merge.log 확인 후 관리자가 해결하고 integrate를 재실행하세요.')
        if git(root, 'ls-files', '-u').strip():
            raise ValueError('미해결 병합 충돌이 있습니다.')
        before = snapshot(root)
        code = run_logged(['./gradlew', 'check'], root, out / 'integration-check.log')
        if code != 0 or snapshot(root) != before:
            raise ValueError('통합 검증 실패 또는 실행 중 변경. 병합 상태를 보존했습니다.')
        git(root, 'add', '-u')
        git(root, 'commit', '-m', 'merge: ' + args.task)
        state.update(status='integrated', integrated_commit=sha(root))
    save(path, state)
    print(json.dumps(state, ensure_ascii=False, indent=2))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    subs = parser.add_subparsers(dest='command', required=True)
    p = subs.add_parser('register')
    p.add_argument('task')
    p.add_argument('--workspace', required=True)
    p.add_argument('--write-path', action='append', required=True)
    p.add_argument('--migration')
    p = subs.add_parser('status')
    p.add_argument('task', nargs='?')
    for name in ('tests-ready', 'run-writer', 'review', 'refresh-review', 'recover', 'integrate'):
        p = subs.add_parser(name)
        p.add_argument('task')
        if name == 'tests-ready':
            p.add_argument('--tests', required=True)
            p.add_argument('--report', required=True)
        if name == 'run-writer':
            p.add_argument('--extra', default='')
        if name == 'review':
            p.add_argument('--report', required=True)
        if name == 'recover':
            p.add_argument('--worker-stopped', action='store_true')
    args = parser.parse_args()
    root = Path(git('.', 'rev-parse', '--show-toplevel').decode().strip()).resolve()
    if (root / '.work/task.json').exists():
        raise ValueError('이 명령은 작업자 공간이 아닌 관리자 공간에서 실행하세요.')
    if getattr(args, 'task', None) and not re.fullmatch(r'[a-z0-9][a-z0-9-]*', args.task):
        raise ValueError('잘못된 작업 ID입니다.')
    registry = root / '.work/tasks'
    registry.mkdir(parents=True, exist_ok=True)
    if args.command == 'status':
        paths = [registry / (args.task + '.json')] if args.task else sorted(registry.glob('*.json'))
        print(json.dumps([json.loads(p.read_text()) for p in paths], ensure_ascii=False, indent=2))
        return
    # Registration/integration share one manager lock; different worker tasks can run concurrently.
    with lock(registry / (args.task + '.lock')):
        if args.command in ('register', 'integrate'):
            with lock(registry / 'manager.lock'):
                if args.command == 'register':
                    register(args, root, registry)
                else:
                    task_command(args, root, registry)
        else:
            task_command(args, root, registry)


if __name__ == '__main__':
    try:
        main()
    except (ValueError, OSError, subprocess.CalledProcessError) as exc:
        print('pipeline:', exc, file=sys.stderr)
        sys.exit(1)
