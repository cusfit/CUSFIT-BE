#!/usr/bin/env bash
set -euo pipefail
script_dir="$(cd "$(dirname "$0")" && pwd)"
exec python3 "$script_dir/role_boundary.py" "${1:?role}" "${2:?base-ref}"
