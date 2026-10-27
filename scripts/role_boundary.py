#!/usr/bin/env python3
"""Shared path policy for hooks and post-stage checks (standard library only)."""
import json
import os
from pathlib import Path
import subprocess
import sys


def git(root, *args):
    return subprocess.check_output(['git', '-C', str(root), *args])


def allowed(role, path):
    if role == 'writer':
        return path.startswith('src/main/')
    if role == 'verifier':
        return path.startswith('src/test/')
    if role == 'reviewer':
        return False
    if role == 'manager':
        return path.startswith(('docs/', '.claude/', 'scripts/')) or ('/' not in path and path.endswith('.md'))
    raise ValueError('알 수 없는 역할: ' + role)


def changes(root, base):
    # --no-renames checks both source and destination; NUL handles spaces/newlines.
    tracked = git(root, 'diff', '--no-renames', '--name-only', '-z', base)
    staged = git(root, 'diff', '--cached', '--no-renames', '--name-only', '-z', base)
    new = git(root, 'ls-files', '--others', '--exclude-standard', '-z')
    return sorted(set(os.fsdecode(p) for p in (tracked + staged + new).split(b'\0') if p))


def check(root, role, base):
    paths = changes(root, base)
    bad = [p for p in paths if not allowed(role, p)]
    if bad:
        raise ValueError(f'역할 경계 위반 ({role}): ' + repr(bad))
    return paths


def read_role(root):
    role_file = root / '.work/role'
    return role_file.read_text().strip() if role_file.exists() else None


def worktree_root(path):
    for parent in (path, *path.parents):
        if (parent / '.git').exists():
            return parent
    return None


def hook():
    session = Path(os.environ.get('CLAUDE_PROJECT_DIR', os.getcwd())).resolve()
    session_role = read_role(session)
    data = json.load(sys.stdin)
    tool = data.get('tool_name', '')
    args = data.get('tool_input', {})
    # Shell commands cannot reliably be classified as read-only. Workers use a
    # manager-run test/check command instead of unrestricted Bash during a stage.
    if tool == 'Bash':
        if session_role and session_role != 'manager':
            raise ValueError('작업 역할의 Bash는 차단됩니다. 테스트 실행은 관리자에게 요청하세요.')
        return
    raw = args.get('file_path') or args.get('notebook_path')
    if not raw:
        if session_role:
            raise ValueError('수정 대상 경로를 확인할 수 없습니다.')
        return
    target = Path(raw)
    target = (session / target).resolve() if not target.is_absolute() else target.resolve()
    # A subagent launched from the manager session runs with the manager's
    # CLAUDE_PROJECT_DIR, so the role of the worktree that owns the file wins.
    owner = worktree_root(target)
    owner_role = read_role(owner) if owner and owner != session else None
    if owner_role:
        root, role = owner, owner_role
    elif session_role:
        root, role = session, session_role
    else:
        return
    try:
        rel = target.relative_to(root).as_posix()
    except ValueError:
        raise ValueError('작업 공간 밖의 파일 수정은 허용하지 않습니다.')
    if rel.startswith('.work/') and rel != '.work/role' and rel != '.work/task.json':
        return
    if not allowed(role, rel):
        raise ValueError(f'role-guard: {role}은 {rel!r}을 수정할 수 없습니다.')


if __name__ == '__main__':
    try:
        if sys.argv[1:] == ['hook']:
            hook()
        else:
            root = Path(git('.', 'rev-parse', '--show-toplevel').decode().strip())
            paths = check(root, sys.argv[1], sys.argv[2])
            print('역할 경계 OK:', sys.argv[1], repr(paths))
    except (ValueError, IndexError, OSError, subprocess.CalledProcessError) as exc:
        print(str(exc), file=sys.stderr)
        sys.exit(2)
