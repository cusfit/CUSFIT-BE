#!/usr/bin/env bash
# 작성자(Codex)를 비대화형으로 실행한다. (docs/AGENT_WORKFLOW.md 3단계)
# 사용법: scripts/run-writer.sh <task-file> [추가 지시]
#   예: scripts/run-writer.sh docs/tasks/user.md "리뷰 반려 사유: .work/user/review.md 참고"
set -euo pipefail

task_file="${1:?작업 스펙 파일 경로를 지정하세요 (예: docs/tasks/user.md)}"
extra="${2:-}"

repo="$(git rev-parse --show-toplevel)"
task_name="$(basename "$task_file" .md)"
out_dir="$repo/.work/$task_name"
mkdir -p "$out_dir"

prompt="너는 이 저장소의 작성자(writer)다. 먼저 AGENTS.md를 읽고 그 규칙을 따른다.
작업 스펙: $task_file
이 스펙에 해당하는 src/test의 테스트가 이미 커밋되어 있다. 테스트를 읽고, 테스트를 통과시키는 구현을 src/main에 작성하라.
src/test는 절대 수정하지 마라. git commit도 하지 마라.
가능하면 ./gradlew test --tests 로 관련 테스트를 실행해 확인하라. 샌드박스 때문에 Gradle이나 Docker(Testcontainers)를 실행할 수 없으면 그 사실을 보고하고 구현만 마쳐라.
${extra:+추가 지시: $extra}"

codex exec \
	-s workspace-write \
	-C "$repo" \
	-o "$out_dir/writer-result.md" \
	"$prompt"

echo "작성자 결과: $out_dir/writer-result.md"
