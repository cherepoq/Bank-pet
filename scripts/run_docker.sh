#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -f .env ]]; then
  umask 077
  cat > .env <<EOF
DATABASE_PASSWORD=$(openssl rand -hex 24)
MEDIA_ENCRYPTION_KEY=$(openssl rand -base64 48 | tr -d '\n')
MEDIA_API_TOKEN=$(openssl rand -hex 32)
ANDROID_CERT_SHA256=REPLACE_WITH_RELEASE_KEY_SHA256
EOF
  echo "Created local .env with persistent development secrets."
fi

echo "[1/3] Static checks"
./android/scripts/pre_release_check.sh

echo "[2/3] Starting RentFlow in Docker"
docker compose up --build -d

echo "[3/3] Done"
echo "Open UI: http://localhost:8080/app"
echo "Stop: docker compose down"
