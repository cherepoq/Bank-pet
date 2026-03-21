#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

printf '
[1/5] Static pre-release checks
'
./android/scripts/pre_release_check.sh

printf '
[2/5] Trying full app preview (Spring Boot)
'
LOG_FILE="$(mktemp)"
mvn spring-boot:run >"$LOG_FILE" 2>&1 &
APP_PID=$!
SERVER_PID=""
cleanup() {
  if [[ -n "${SERVER_PID:-}" ]] && ps -p "$SERVER_PID" >/dev/null 2>&1; then
    kill "$SERVER_PID" || true
  fi
  if ps -p "$APP_PID" >/dev/null 2>&1; then
    kill "$APP_PID" || true
  fi
}
trap cleanup EXIT

printf '
[3/5] Waiting for app on http://localhost:8080/app
'
for i in {1..20}; do
  if curl -fsS http://localhost:8080/app >/dev/null 2>&1; then
    echo "App is up."
    printf '
[4/5] Smoke HTTP checks
'
    curl -fsS http://localhost:8080/app | rg -q "Bank Pet"
    curl -fsS http://localhost:8080/manifest.webmanifest | rg -q '"name"'
    printf '
[5/5] Ready
'
    echo "Open: http://localhost:8080/app"
    wait "$APP_PID"
    exit 0
  fi

  if ! ps -p "$APP_PID" >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

printf '
[4/5] Full app preview unavailable, switching to static UI preview fallback
'
if rg -q "403|Non-resolvable parent POM|Could not transfer artifact" "$LOG_FILE"; then
  echo "Reason: Maven dependencies are unavailable in this environment."
else
  echo "Reason: Spring app did not start successfully."
fi

printf '
[5/5] Starting static preview at http://localhost:8081/preview.html
'
cd "$ROOT_DIR/src/main/resources/static"
python3 -m http.server 8081 &
SERVER_PID=$!
wait "$SERVER_PID"
