#!/usr/bin/env bash
# PreToolUse 훅: .work/role 에 기록된 현재 파이프라인 역할에 따라 Claude의 파일 수정 경로를 제한한다.
# .work/role 이 없으면 파이프라인 밖의 평소 작업이므로 모두 허용한다. (docs/AGENT_WORKFLOW.md)
set -euo pipefail

input="$(cat)"
project_dir="${CLAUDE_PROJECT_DIR:-$(pwd)}"
role_file="$project_dir/.work/role"

[[ -f "$role_file" ]] || exit 0
role="$(tr -d '[:space:]' < "$role_file")"

file_path="$(printf '%s' "$input" | python3 -c 'import json,sys; d=json.load(sys.stdin).get("tool_input",{}); print(d.get("file_path") or d.get("notebook_path") or "")')"
[[ -n "$file_path" ]] || exit 0

case "$file_path" in
	"$project_dir"/*) rel="${file_path#"$project_dir"/}" ;;
	/*) rel="$file_path" ;;
	*) rel="$file_path" ;;
esac

allowed=false
case "$role" in
	verifier)
		case "$rel" in
			src/test/*|.work/*) allowed=true ;;
		esac
		;;
	manager)
		case "$rel" in
			.work/*|docs/*|.claude/*|*.md) allowed=true ;;
		esac
		;;
	writer)
		# 작성자는 Codex라 이 훅의 대상이 아니다. writer 단계에서 Claude가 파일을 고치는 것은 막는다.
		case "$rel" in
			.work/*) allowed=true ;;
		esac
		;;
	*)
		echo "role-guard: 알 수 없는 역할 '$role' (.work/role)" >&2
		exit 2
		;;
esac

# 프로젝트 밖(스크래치패드, 플랜 파일 등)은 역할과 무관하게 허용한다.
if [[ "$rel" == /* ]]; then
	allowed=true
fi

if [[ "$allowed" != true ]]; then
	echo "role-guard: 현재 역할 '$role'은(는) '$rel'을(를) 수정할 수 없습니다. 허용 경로는 docs/AGENT_WORKFLOW.md 참고." >&2
	exit 2
fi
exit 0
