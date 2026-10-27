#!/usr/bin/env bash
set -euo pipefail
project_dir="${CLAUDE_PROJECT_DIR:-$(pwd)}"
exec python3 "$project_dir/scripts/role_boundary.py" hook
