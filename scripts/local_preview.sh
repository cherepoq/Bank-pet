#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

printf '\n[1/4] Static pre-release checks\n'
./android/scripts/pre_release_check.sh

printf '\n[2/4] Starting app (mvn spring-boot:run)\n'
mvn spring-boot:run &
APP_PID=$!
cleanup() {
  if ps -p "$APP_PID" >/dev/null 2>&1; then
    kill "$APP_PID" || true
  fi
}
trap cleanup EXIT

printf '\n[3/4] Waiting for app on http://localhost:8080/app\n'
for i in {1..60}; do
  if curl -fsS http://localhost:8080/app >/dev/null 2>&1; then
    echo "App is up."
    break
  fi
  sleep 2
  if [[ "$i" == "60" ]]; then
    echo "App did not start in time" >&2
    exit 1
  fi
done

printf '\n[4/4] Smoke HTTP checks\n'
curl -fsS http://localhost:8080/app | rg -q "RentFlow"
curl -fsS http://localhost:8080/manifest.webmanifest | rg -q '"name"'
echo "Smoke checks passed. Open: http://localhost:8080/app"

wait "$APP_PID"
