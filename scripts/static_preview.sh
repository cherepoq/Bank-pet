#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PORT="${PORT:-4173}"
PREVIEW_DIR="$ROOT_DIR/.preview"

rm -rf "$PREVIEW_DIR"
mkdir -p "$PREVIEW_DIR"
cp "$ROOT_DIR/src/main/resources/templates/dashboard.html" "$PREVIEW_DIR/index.html"
cp -R "$ROOT_DIR/src/main/resources/static/." "$PREVIEW_DIR/"

echo "RentFlow preview: http://localhost:$PORT"
echo "Press Ctrl+C to stop."
cd "$PREVIEW_DIR"
exec python3 -m http.server "$PORT"
