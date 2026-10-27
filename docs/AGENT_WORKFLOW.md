# 에이전트 워크플로

관리자 세션 하나가 여러 작업을 관리한다. 각 작업은 **별도 Git worktree와 브랜치**에서 검증자 → 작성자 → 리뷰 순서로 진행하며, 서로 다른 작업의 작성자는 동시에 실행할 수 있다. 관리자는 검증을 통과한 결과만 한 건씩 통합한다.

## 역할과 경계

| 역할 | 담당 | 수정 가능 범위 |
|---|---|---|
| 관리자 | 메인 세션 (Claude 또는 Codex) | 명세·설정·작업 등록·테스트 실행·판정·커밋·통합 |
| 검증자 | 작업 worktree에서 실행하는 `verifier`, write-tests 모드 | `src/test/**`, `.work` 보고서 |
| 작성자 | `scripts/run-writer.sh`가 실행하는 Codex | 등록 시 지정한 `src/main/**` 하위 경로만 |
| 리뷰어 | 작업 worktree의 `verifier`, review 모드 | `.work` 보고서만 |

검증자·작성자·리뷰어는 커밋하지 않는다. 모든 커밋은 관리자가 실행하는 `scripts/pipeline.py`가 만든다. 관리자가 인프라를 설정하는 작업은 도메인 작성자 단계와 별개이며 `scripts/**`, `.claude/**`, 문서를 수정할 수 있다.

Claude의 Edit/Write 훅은 정규화한 경로를 검사한다. 작업 역할의 Bash는 우회 수정을 막기 위해 차단한다. 테스트/Gradle 실행과 Git diff 수집은 관리자가 한다. Codex에는 Claude 훅이 적용되지 않으므로 실행 후 변경 경로·HEAD를 검사하고 위반을 반려한다. 이는 협업 실수를 막는 장치이며, 같은 OS 계정의 악의적인 프로세스를 격리하는 보안 경계는 아니다.

## 상태와 결과 위치

- 관리자 공간: `.work/tasks/{id}.json`에 상태·worktree·브랜치·기준 커밋·허용 경로·반려 횟수·결과 경로를 보관한다.
- 관리자 공간: `.work/results/{id}/`에 시도별 작성자 보고서/로그, 리뷰, Red/전체 검사/통합 검사 로그를 보관한다.
- 작업 공간: `.work/task.json`은 관리자 위치, `.work/role`은 해당 작업의 역할이다. 전역 역할 파일을 공유하지 않는다.
- `.work`는 Git에 올리지 않는다. 프로세스가 끝나도 기록은 남는다. `.work`를 지우면 추적 정보가 사라지므로 완료 전 삭제하지 않는다.
- 작업별 파일 잠금으로 같은 작업의 중복 실행을 차단하고, 등록과 통합에는 관리자 잠금을 추가한다. 다른 작업의 작성자 실행은 서로 기다리지 않는다.

상태: `testing → ready → running → review → accepted → integrated`.
실행 실패는 `failed`, 리뷰 반려는 `rejected`, 3회 반려는 `blocked`, 병합/통합 검사 실패는 `integration-failed`다. `accepted`는 작업 단독 검증 완료이며 `integrated`만 통합 완료다.

## 준비와 작업 발행

Python 3, Git, Codex CLI와 인증, Java 17, Docker가 필요하다. 작업 공간 생성에는 설치된 Orca CLI를 사용한다. 설정 변경과 작업 명세를 먼저 관리자가 커밋한다. 작업 공간에는 커밋된 파일만 전달된다.

1. `docs/tasks/TEMPLATE.md`를 복사해 `docs/tasks/{id}.md`를 작성하고 커밋한다. ID는 소문자·숫자·하이픈을 사용한다. public API, 수정 경로, 의존 작업, Flyway 파일을 명시한다.
2. 관리자의 현재 커밋을 확인하고 그 커밋에서 Orca worktree를 만든다. 아래 `<관리자 HEAD>`에는 `git rev-parse HEAD` 결과를, `<작업 경로>`에는 Orca가 반환한 실제 경로를 넣는다. 환경에 따라 `orca` 대신 세션의 `ORCA_CLI_COMMAND`를 사용한다.

```sh
git rev-parse HEAD
orca worktree create --name user-entity --base-branch <관리자 HEAD> --setup skip --json
python3 scripts/pipeline.py register user-entity --workspace '<작업 경로>' \
  --write-path src/main/java/com/cusfit/cusfitbe/user \
  --migration src/main/resources/db/migration/V1__create_users.sql
```

등록은 같은 저장소의 다른 worktree, 동일 HEAD, 깨끗한 작업 트리, 커밋된 명세를 요구한다. 다른 미완료 작업과 수정 경로 또는 Flyway 버전이 겹치면 거부한다. Flyway 버전은 이미 있는 마이그레이션의 최대 버전보다 커야 한다. 공통 코드가 필요하면 먼저 별도 작업으로 통합하고, 의존 작업은 그 뒤 새 HEAD에서 만든다. 작업자에게 기존 관리자 폴더를 그대로 주지 않는다.

## 작업 루프 — 모든 pipeline 명령은 관리자 공간에서 실행

### 1. 테스트 작성과 Red 확인

관리자가 검증자를 호출한다. 방법은 둘 중 하나다.

- 관리자 세션에서 `verifier` 서브에이전트로 호출: 작업 디렉터리가 관리자 폴더로 고정되므로 작업 worktree의 **절대 경로**를 전달하고 그 경로만 쓰게 한다. 훅은 수정 대상 파일이 속한 worktree의 `.work/role`을 읽어 경계를 적용한다.
- 작업 worktree에서 별도 Claude 세션을 열어 `verifier` 지침으로 실행: 훅은 그 세션 폴더의 `.work/role`을 적용한다.

`.claude/agents/verifier.md`의 write-tests 지침과 명세 경로를 전달한다. 검증자는 테스트와 `.work/red-review.md`(실행할 `--tests` 패턴, 예상 실패 원인)를 작성하고 종료한다. 서브에이전트는 실행 도중 관리자에게 테스트 실행을 요청할 수 없으므로, 테스트 오류를 고쳐야 하면 관리자가 로그를 붙여 다시 호출한다. 검증자는 기존 테스트 파일을 삭제할 수 없다(게이트가 거부).

관리자는 해당 worktree에서 대상 테스트를 실행해 실패 원인이 구현 부재인지 확인한다. 의존성 다운로드·Docker·JDK 오류는 Red가 아니다. 확인한 보고서에 `RED_REASON: missing-implementation`과 실제 명령/실패 원인을 기록한 후 아래 게이트를 실행한다. 게이트는 테스트를 재실행해 실패·경계·파일 불변 여부를 확인하고 테스트 커밋을 만든다.

```sh
python3 scripts/pipeline.py tests-ready user-entity --tests '*UserTest' \
  --report '<작업 경로>/.work/red-review.md'
```

### 2. 작성자 실행과 결과 회수

```sh
scripts/run-writer.sh user-entity
python3 scripts/pipeline.py status
```

등록된 작업마다 별도 관리자 터미널에서 `run-writer.sh`를 실행하면 병렬로 처리된다. 실행 명령은 완료까지 대기하며 로그와 최종 보고서를 자동으로 수집한다. 같은 작업의 중복 실행은 거부한다. `codex exec` 성공만으로 완료로 처리하지 않고 HEAD·허용 경로를 다시 검사한 뒤 `review`로 전환한다.

실패하면 기존 변경을 보존하고 `failed`로 기록한다. 관리자가 원인을 해결하고 같은 명령을 다시 실행한다. 강제 종료 후 `running`이 남았다면 실제 작성자 프로세스가 끝났는지 먼저 확인하고 아래 명령을 실행한다. 시간 경과만으로 완료/실패를 추정하지 않는다.

```sh
python3 scripts/pipeline.py recover user-entity --worker-stopped
```

### 3. 독립 리뷰와 전체 검증

관리자는 `status user-entity`의 `test_commit`, `snapshot`, 보고서 경로를 리뷰어에게 전달하고 `git diff <test_commit>` 결과도 제공한다. 리뷰어는 동일 작업 worktree에서 명세·테스트·구현을 읽고 `.work/review.md`를 작성한다.

```text
SNAPSHOT: <status에 기록된 snapshot 값>
VERDICT: PASS
- 검토한 명세/불변식/실패 케이스와 판단 근거
```

```sh
python3 scripts/pipeline.py review user-entity --report '<작업 경로>/.work/review.md'
```

`PASS`일 때 관리자의 게이트가 `./gradlew check`를 실행한다. 경계, 리뷰 대상의 지문, 검사 전후 파일 불변 여부를 확인한 뒤 구현을 커밋하고 `accepted`로 전환한다. 리뷰 이후 수정된 파일은 승인할 수 없다. 의도적으로 수정한 경우 `refresh-review {id}`로 지문을 갱신하고 **새 리뷰**를 받는다.

`REJECT`면 보고서를 보존하고 다음 `run-writer.sh` 실행에 자동으로 전달한다. 3회 반려면 `blocked`에서 멈추고 사용자에게 이유를 보고한다. 임의로 횟수를 초기화하지 않는다. 검사가 인프라 오류로 실패하면 `review` 상태를 유지하며 환경을 해결한 뒤 재실행한다.

### 4. 순차 통합

```sh
python3 scripts/pipeline.py integrate user-entity
```

관리자 폴더가 깨끗하고 승인된 작업 커밋이 그대로일 때만 병합한다. 마이그레이션이 있는 작업은 Flyway 버전 순서대로만 통합할 수 있다. 더 낮은 버전을 가진 미통합 작업이 있으면 거부한다. 높은 버전이 먼저 적용된 DB에 낮은 버전이 들어오면 Flyway 검증이 실패하기 때문이다. 관리자는 통합하는 동안 다른 파일 작업을 하지 않는다. `--no-ff --no-commit` 병합 후 통합된 코드로 `./gradlew check`를 실행하고 성공해야 병합 커밋과 `integrated` 상태를 기록한다.

충돌 또는 검사 실패 시 병합 상태와 로그를 보존한다. 관리자가 충돌/통합 문제를 해결하고 충돌 파일을 stage한 뒤 같은 `integrate` 명령을 재실행한다. 중대한 구현 변경이 필요하면 관리자가 `git merge --abort`로 이 병합만 중단하고, 작업 결과를 새 작업으로 수정·검증한 뒤 통합한다. 기존 사용자 변경을 자동 reset/checkout하지 않는다. 완료한 worktree도 자동 삭제하지 않는다.

## 설정 자체의 검증

```sh
python3 scripts/test_pipeline.py
```

임시 Git 저장소에서 역할 위반, 서브에이전트 훅 경계, 기존 테스트 삭제, 병렬 작업 등록, 중복 실행, 작성자 실패, 리뷰 지문, 반려 한도, Flyway 버전 순서, 병합과 통합 검증을 검사한다. 외부 모델 호출과 Gradle은 테스트 대역으로 대체한다. 실제 서비스 코드는 각 작업의 `./gradlew check`로 별도 검증한다.
