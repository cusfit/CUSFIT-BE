#!/usr/bin/env bash
# 파이프라인 단계가 끝난 뒤 역할 경계를 diff로 검사한다. Codex처럼 Claude 훅이 적용되지 않는 도구에도 똑같이 동작한다.
# 사용법: scripts/check-role-boundary.sh <writer|verifier> <base-ref>
#   base-ref 이후 변경(커밋 + 작업 트리 + untracked)을 모두 검사한다.
set -euo pipefail

role="${1:?역할(writer|verifier)을 지정하세요}"
base="${2:?base ref를 지정하세요}"

cd "$(git rev-parse --show-toplevel)"

changed="$(
	{
		git diff --name-only "$base"
		git ls-files --others --exclude-standard
	} | sort -u
)"

case "$role" in
	writer) forbidden_pattern='^src/test/' ;;
	verifier) forbidden_pattern='^src/main/' ;;
	*)
		echo "알 수 없는 역할: $role" >&2
		exit 2
		;;
esac

violations="$(printf '%s\n' "$changed" | grep -E "$forbidden_pattern" || true)"

if [[ -n "$violations" ]]; then
	echo "역할 경계 위반 ($role): 다음 파일은 이 역할이 수정할 수 없습니다." >&2
	printf '  %s\n' $violations >&2
	exit 1
fi

echo "역할 경계 OK ($role). 변경 파일:"
printf '%s\n' "$changed" | sed '/^$/d; s/^/  /'
