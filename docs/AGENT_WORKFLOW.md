# 에이전트 워크플로 (관리자 / 검증자 / 작성자)

역할이 분리된 에이전트로 TDD를 강제하는 작업 방식입니다. 검증자가 실패하는 테스트를 먼저 커밋하고(Red), 작성자는 테스트를 건드리지 못한 채 구현해 통과시킵니다(Green). 쓰는 쪽과 검증하는 쪽의 모델을 다르게 두어 같은 맹점을 공유하지 않게 합니다.

## 역할

| 역할 | 담당 | 수정 가능 경로 | 강제 수단 |
|---|---|---|---|
| 관리자 | Claude Code 메인 세션 | `docs/**`, `.claude/**`, `*.md`, `.work/**` | `.claude/hooks/role-guard.sh` |
| 검증자 | Claude 서브에이전트 `verifier` (`.claude/agents/verifier.md`) | `src/test/**`, `.work/**` | 훅 + `scripts/check-role-boundary.sh verifier` |
| 작성자 | Codex CLI (`scripts/run-writer.sh`) | `src/test/**`를 제외한 전부 | `AGENTS.md` 지시 + `scripts/check-role-boundary.sh writer` |

- 관리자는 직접 구현하지 않고, 작업 분해·발행, 단계 판정, 커밋을 맡습니다.
- 현재 역할은 `.work/role` 파일(`manager` / `verifier` / `writer`)에 기록하며, 훅이 이 값을 읽어 Claude의 Edit/Write를 제한합니다. 파일이 없으면 파이프라인 밖의 평소 작업으로 보고 제한하지 않습니다.
- Codex는 Claude 훅의 적용을 받지 않으므로, 모든 단계가 끝날 때 diff 검사 스크립트로 경계를 다시 확인합니다.
- `.work/`는 역할 표식과 단계별 산출물(작성자 결과, 리뷰 결과)을 두는 곳이며 git에 올리지 않습니다.

## 작업 1건의 루프

1. **발행** — 관리자가 `docs/tasks/TEMPLATE.md`를 복사해 `docs/tasks/{작업}.md`를 작성하고 커밋합니다. 이 커밋을 base로 기록하고 `.work/role`을 `verifier`로 바꿉니다.
2. **Red** — 검증자(`write-tests` 모드)가 테스트를 작성하고 실패를 확인합니다. 관리자는 `scripts/check-role-boundary.sh verifier <base>`를 실행한 뒤 테스트만 커밋합니다(`test: ...`).
3. **Green** — `.work/role`을 `writer`로 바꾸고 `scripts/run-writer.sh docs/tasks/{작업}.md`로 Codex를 실행합니다. `scripts/check-role-boundary.sh writer <테스트 커밋>`이 실패하면 작성자의 `src/test` 변경을 되돌리고 다시 지시합니다. 관리자가 `./gradlew check`를 실행합니다.
4. **Review** — `.work/role`을 `verifier`로 바꾸고 검증자(`review` 모드)를 실행합니다. `REJECT`면 사유를 `.work/{작업}/review.md`에 저장하고 `run-writer.sh`의 추가 지시로 넘겨 3단계로 돌아갑니다. 3회 반려되면 사용자에게 보고하고 멈춥니다.
5. **완료** — `.work/role`을 `manager`로 되돌리고 구현을 커밋합니다(엔티티와 마이그레이션은 같은 커밋, `CODE_CONVENTIONS.md` 6.2). 끝나면 `.work/role`을 지워 평소 모드로 돌아갑니다.

커밋 이력은 작업마다 "작업 스펙 → 테스트 → 구현" 순서가 됩니다.

## 알려진 제약

- Codex의 `workspace-write` 샌드박스에서는 Gradle 의존성 다운로드나 Docker(Testcontainers) 접근이 막힐 수 있습니다. 이때 작성자는 구현만 마치고, 전체 `./gradlew check`는 관리자가 샌드박스 밖에서 실행합니다.
- 경로에 공백과 한글이 있으므로 스크립트와 명령에서 경로를 항상 따옴표로 감쌉니다.
