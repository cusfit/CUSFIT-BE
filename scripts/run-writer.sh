#!/usr/bin/env bash
# 관리자 공간에서 등록된 작업 ID로 실행한다. 작업별로 동시에 실행할 수 있다.
set -euo pipefail
script_dir="$(cd "$(dirname "$0")" && pwd)"
exec python3 "$script_dir/pipeline.py" run-writer "${1:?등록된 작업 ID를 지정하세요}" --extra "${2:-}"
