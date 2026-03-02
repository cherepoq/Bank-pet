#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "[1/3] Static checks"
./android/scripts/pre_release_check.sh

echo "[2/3] Starting Bank Pet in Docker"
docker compose up --build -d

echo "[3/3] Done"
echo "Open UI: http://localhost:8080/app"
echo "Stop: docker compose down"
