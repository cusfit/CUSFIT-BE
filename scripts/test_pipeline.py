#!/usr/bin/env python3
"""Offline integration tests; all Git mutations occur in temporary repositories."""
import fcntl
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

SOURCE = Path(__file__).resolve().parent.parent


class PipelineTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix='cusfit pipeline 한글 ')
        self.addCleanup(self.temp.cleanup)
        self.base = Path(self.temp.name)
        self.root = self.base / 'manager'
        self.root.mkdir()
        shutil.copytree(SOURCE / 'scripts', self.root / 'scripts', ignore=shutil.ignore_patterns('__pycache__'))
        shutil.copytree(SOURCE / '.claude', self.root / '.claude')
        (self.root / '.gitignore').write_text('.work/\n')
        (self.root / 'docs/tasks').mkdir(parents=True)
        for task in ('user', 'chat', 'overlap'):
            (self.root / f'docs/tasks/{task}.md').write_text(task)
        (self.root / 'src/main').mkdir(parents=True)
        (self.root / 'src/test').mkdir()
        (self.root / 'src/main/base.txt').write_text('base')
        gradle = self.root / 'gradlew'
        gradle.write_text('#!/bin/sh\nif [ "$1" = test ]; then echo "missing implementation"; exit 1; fi\nexit "${CHECK_EXIT:-0}"\n')
        gradle.chmod(0o755)
        self.bin = self.base / 'bin'
        self.bin.mkdir()
        codex = self.bin / 'codex'
        codex.write_text('''#!/usr/bin/env python3
import os, pathlib, sys
if os.environ.get('WRITER_FAIL'):
    sys.exit(1)
p = pathlib.Path(os.environ['WRITE_FILE'])
p.parent.mkdir(parents=True, exist_ok=True)
p.write_text('implementation')
pathlib.Path(sys.argv[sys.argv.index('-o') + 1]).write_text('writer result')
''')
        codex.chmod(0o755)
        self.env = dict(os.environ, PATH=str(self.bin) + os.pathsep + os.environ['PATH'],
                        PYTHONDONTWRITEBYTECODE='1', GIT_CONFIG_NOSYSTEM='1',
                        GIT_CONFIG_GLOBAL=os.devnull)
        self.git(self.root, 'init', '-q')
        self.git(self.root, 'config', 'user.name', 'Pipeline test')
        self.git(self.root, 'config', 'user.email', 'test@example.invalid')
        self.git(self.root, 'add', '.')
        self.git(self.root, 'commit', '-qm', 'baseline')

    def git(self, root, *args):
        return subprocess.check_output(['git', '-C', str(root), *args], env=self.env, stderr=subprocess.PIPE).decode().strip()

    def call(self, *args, ok=True, env=None):
        result = subprocess.run([sys.executable, str(self.root / 'scripts/pipeline.py'), *args],
                                cwd=self.root, env=dict(self.env, **(env or {})), capture_output=True, text=True)
        if ok:
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        else:
            self.assertNotEqual(result.returncode, 0, result.stdout)
        return result

    def state(self, task='user'):
        return json.loads((self.root / f'.work/tasks/{task}.json').read_text())

    def register(self, task='user', migration=None):
        work = self.base / task
        self.git(self.root, 'worktree', 'add', '-qb', 'task-' + task, str(work), 'HEAD')
        extra = ['--migration', migration] if migration else []
        self.call('register', task, '--workspace', str(work), '--write-path', f'src/main/{task}', *extra)
        return work

    def ready(self, task='user', migration=None):
        work = self.register(task, migration)
        test = work / f'src/test/{task}.txt'
        test.parent.mkdir(parents=True, exist_ok=True)
        test.write_text('spec test')
        report = work / '.work/red-review.md'
        report.write_text('RED_REASON: missing-implementation\nManager inspected missing implementation failure.')
        self.call('tests-ready', task, '--tests', '*Test', '--report', str(report))
        return work

    def run_writer(self, task='user', **env):
        return self.call('run-writer', task, env=dict(WRITE_FILE=f'src/main/{task}/new.txt', **env))

    def review(self, verdict='PASS', task='user', ok=True, **env):
        data = self.state(task)
        report = Path(data['workspace']) / '.work/review.md'
        report.write_text(f"SNAPSHOT: {data['snapshot']}\nVERDICT: {verdict}\n- independent review\n")
        return self.call('review', task, '--report', str(report), ok=ok, env=env)

    def test_parallel_tasks_and_complete_integration(self):
        self.ready('user')
        self.ready('chat')
        commands = []
        for task in ('user', 'chat'):
            commands.append(subprocess.Popen([sys.executable, str(self.root / 'scripts/pipeline.py'), 'run-writer', task],
                cwd=self.root, env=dict(self.env, WRITE_FILE=f'src/main/{task}/new.txt'), stdout=subprocess.PIPE, stderr=subprocess.PIPE))
        for command in commands:
            out, err = command.communicate(timeout=20)
            self.assertEqual(command.returncode, 0, out.decode() + err.decode())
        for task in ('user', 'chat'):
            self.assertEqual(self.state(task)['status'], 'review')
            self.review(task=task)
            self.call('integrate', task)
            self.assertEqual(self.state(task)['status'], 'integrated')
            self.assertTrue((self.root / f'src/main/{task}/new.txt').exists())
        self.assertEqual(self.git(self.root, 'status', '--porcelain'), '')

    def test_failed_writer_and_retry(self):
        self.ready()
        self.call('run-writer', 'user', ok=False, env={'WRITER_FAIL': '1'})
        self.assertEqual(self.state()['status'], 'failed')
        self.run_writer()
        self.assertEqual(self.state()['status'], 'review')

    def test_writer_scope_violation(self):
        self.ready()
        self.call('run-writer', 'user', ok=False, env={'WRITE_FILE': 'src/test/cheat.txt'})
        self.assertEqual(self.state()['status'], 'failed')

    def test_review_fingerprint_and_check_failure(self):
        work = self.ready()
        self.run_writer()
        (work / 'src/main/user/new.txt').write_text('modified after review')
        self.review(ok=False)
        self.call('refresh-review', 'user')
        self.review(ok=False, CHECK_EXIT='1')
        self.assertEqual(self.state()['status'], 'review')
        self.review()
        self.assertEqual(self.state()['status'], 'accepted')

    def test_three_rejections_block(self):
        self.ready()
        for _ in range(3):
            self.run_writer()
            self.review('REJECT')
        self.assertEqual(self.state()['status'], 'blocked')
        self.call('run-writer', 'user', ok=False)

    def test_integration_failure_preserved_and_resumed(self):
        self.ready()
        self.run_writer()
        self.review()
        self.call('integrate', 'user', ok=False, env={'CHECK_EXIT': '1'})
        self.assertEqual(self.state()['status'], 'integration-failed')
        self.git(self.root, 'rev-parse', '--verify', 'MERGE_HEAD')
        self.call('integrate', 'user')
        self.assertEqual(self.state()['status'], 'integrated')

    def test_manager_dirty_files_preserved(self):
        self.ready()
        self.run_writer()
        self.review()
        personal = self.root / 'personal.txt'
        personal.write_text('keep')
        self.call('integrate', 'user', ok=False)
        self.assertEqual(personal.read_text(), 'keep')
        self.assertEqual(self.state()['status'], 'accepted')

    def test_duplicate_task_lock_and_overlap(self):
        self.register()
        work = self.base / 'overlap'
        self.git(self.root, 'worktree', 'add', '-qb', 'overlap', str(work), 'HEAD')
        self.call('register', 'overlap', '--workspace', str(work), '--write-path', 'src/main/user/child', ok=False)
        with (self.root / '.work/tasks/user.lock').open('a') as handle:
            fcntl.flock(handle, fcntl.LOCK_EX)
            result = self.call('recover', 'user', '--worker-stopped', ok=False)
            self.assertIn('다른 관리자', result.stderr)

    def test_verifier_boundary_and_rename(self):
        work = self.register()
        (work / 'build.gradle').write_text('forbidden')
        command = ['bash', str(self.root / 'scripts/check-role-boundary.sh'), 'verifier', 'HEAD']
        result = subprocess.run(command, cwd=work, env=self.env, capture_output=True)
        self.assertNotEqual(result.returncode, 0)
        (work / 'build.gradle').unlink()
        (work / 'src/test').mkdir(exist_ok=True)
        self.git(work, 'mv', 'src/main/base.txt', 'src/test/renamed.txt')
        result = subprocess.run(command, cwd=work, env=self.env, capture_output=True)
        self.assertNotEqual(result.returncode, 0)

    def test_staged_violation_cannot_hide_behind_working_copy(self):
        work = self.register()
        original = (work / 'scripts/run-writer.sh').read_text()
        (work / 'scripts/run-writer.sh').write_text('staged forbidden change')
        self.git(work, 'add', 'scripts/run-writer.sh')
        (work / 'scripts/run-writer.sh').write_text(original)
        result = subprocess.run(['bash', str(self.root / 'scripts/check-role-boundary.sh'), 'verifier', 'HEAD'],
                                cwd=work, env=self.env, capture_output=True)
        self.assertNotEqual(result.returncode, 0)

    def test_migration_version_reservation(self):
        for task in ('user', 'chat'):
            work = self.base / task
            self.git(self.root, 'worktree', 'add', '-qb', 'task-' + task, str(work), 'HEAD')
            self.call('register', task, '--workspace', str(work), '--write-path', f'src/main/{task}',
                      '--migration', f'src/main/resources/db/migration/V1__{task}.sql', ok=task == 'user')

    def test_migration_version_must_exceed_existing(self):
        migration = self.root / 'src/main/resources/db/migration/V3__existing.sql'
        migration.parent.mkdir(parents=True)
        migration.write_text('select 1;')
        self.git(self.root, 'add', '.')
        self.git(self.root, 'commit', '-qm', 'existing migration')
        work = self.base / 'user'
        self.git(self.root, 'worktree', 'add', '-qb', 'task-user', str(work), 'HEAD')
        self.call('register', 'user', '--workspace', str(work), '--write-path', 'src/main/user',
                  '--migration', 'src/main/resources/db/migration/V2__user.sql', ok=False)

    def test_integration_follows_migration_order(self):
        self.ready('user', 'src/main/resources/db/migration/V1__user.sql')
        self.ready('chat', 'src/main/resources/db/migration/V2__chat.sql')
        for task in ('user', 'chat'):
            self.run_writer(task)
            self.review(task=task)
        result = self.call('integrate', 'chat', ok=False)
        self.assertIn('user', result.stderr)
        self.assertEqual(self.state('chat')['status'], 'accepted')
        self.call('integrate', 'user')
        self.call('integrate', 'chat')
        self.assertEqual(self.state('chat')['status'], 'integrated')

    def test_verifier_cannot_delete_existing_tests(self):
        existing = self.root / 'src/test/ArchitectureTest.txt'
        existing.write_text('architecture rules')
        self.git(self.root, 'add', '.')
        self.git(self.root, 'commit', '-qm', 'existing test')
        work = self.register()
        (work / 'src/test/ArchitectureTest.txt').unlink()
        (work / 'src/test/user.txt').write_text('spec test')
        report = work / '.work/red-review.md'
        report.write_text('RED_REASON: missing-implementation\n')
        result = self.call('tests-ready', 'user', '--tests', '*Test', '--report', str(report), ok=False)
        self.assertIn('ArchitectureTest', result.stderr)
        self.assertEqual(self.state()['status'], 'testing')

    def test_hook_uses_role_of_target_worktree_for_subagents(self):
        work = self.register()
        def hook(path):
            return subprocess.run(['bash', str(self.root / '.claude/hooks/role-guard.sh')],
                input=json.dumps({'tool_name': 'Write', 'tool_input': {'file_path': str(path)}}),
                cwd=self.root, env=dict(self.env, CLAUDE_PROJECT_DIR=str(self.root)), capture_output=True, text=True)
        self.assertNotEqual(hook(work / 'src/main/user/Impl.java').returncode, 0)
        self.assertEqual(hook(work / 'src/test/user/UserTest.java').returncode, 0)
        self.assertEqual(hook(work / '.work/red-review.md').returncode, 0)
        self.assertEqual(hook(self.root / 'src/main/manager.txt').returncode, 0)

    def test_interrupted_worker_requires_explicit_recovery(self):
        self.ready()
        path = self.root / '.work/tasks/user.json'
        data = self.state()
        data['status'] = 'running'
        path.write_text(json.dumps(data))
        self.call('recover', 'user', ok=False)
        self.call('recover', 'user', '--worker-stopped')
        self.assertEqual(self.state()['status'], 'failed')
        self.run_writer()

    def test_merge_conflict_is_preserved_and_resolved(self):
        self.ready()
        self.run_writer()
        self.review()
        target = self.root / 'src/main/user/new.txt'
        target.parent.mkdir(parents=True)
        target.write_text('concurrent manager change')
        self.git(self.root, 'add', '.')
        self.git(self.root, 'commit', '-qm', 'concurrent change')
        self.call('integrate', 'user', ok=False)
        self.assertEqual(self.state()['status'], 'integration-failed')
        self.assertTrue(self.git(self.root, 'ls-files', '-u'))
        target.write_text('resolved implementation')
        self.git(self.root, 'add', str(target))
        self.call('integrate', 'user')
        self.assertEqual(self.state()['status'], 'integrated')

    def test_hooks_reject_shell_traversal_and_symlink(self):
        work = self.register()
        (work / 'src/test').mkdir(exist_ok=True)
        (work / 'src/test/escape').symlink_to(self.base)
        def hook(tool, path=''):
            return subprocess.run(['bash', str(work / '.claude/hooks/role-guard.sh')],
                input=json.dumps({'tool_name': tool, 'tool_input': {'file_path': path}}),
                cwd=work, env=dict(self.env, CLAUDE_PROJECT_DIR=str(work)), capture_output=True, text=True)
        self.assertEqual(hook('Write', 'src/test/valid.txt').returncode, 0)
        for tool, path in [('Bash', ''), ('Write', 'src/test/../../build.gradle'),
                           ('Write', 'src/test/escape/outside.txt'), ('Write', '.work/role')]:
            self.assertNotEqual(hook(tool, path).returncode, 0, path)


if __name__ == '__main__':
    unittest.main(verbosity=2)
