#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STATIC_DIR="$ROOT_DIR/src/main/resources/static"
PORT="${1:-8081}"

cd "$STATIC_DIR"
echo "Starting static UI preview on http://localhost:${PORT}/preview.html"
echo "Press Ctrl+C to stop."
python3 -m http.server "$PORT"
