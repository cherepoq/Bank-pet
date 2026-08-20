#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT_DIR"

echo "[1/6] Checking mandatory play files"
test -f android/README.md
test -f android/playstore/assetlinks.json.template
test -f android/playstore/store-listing-checklist.md
test -f android/scripts/build_twa.sh
test -f android/scripts/.env.twa.example

echo "[2/6] Checking web manifest fields"
rg '"start_url"\s*:\s*"/app"' src/main/resources/static/manifest.webmanifest >/dev/null
rg '"scope"\s*:\s*"/"' src/main/resources/static/manifest.webmanifest >/dev/null
rg '"display"\s*:\s*"standalone"' src/main/resources/static/manifest.webmanifest >/dev/null

echo "[3/6] Checking service worker exists"
test -f src/main/resources/static/sw.js

echo "[4/6] Checking style and template presence"
test -f src/main/resources/static/css/style.css
test -f src/main/resources/templates/dashboard.html
test "$(sed -n '1p' src/main/resources/static/css/style.css)" = ':root {'
rg '^\.app-header \{' src/main/resources/static/css/style.css >/dev/null
rg '^\.bottom-nav \{' src/main/resources/static/css/style.css >/dev/null
rg 'css/style.css\?v=' src/main/resources/templates/dashboard.html >/dev/null
test -f src/main/resources/static/privacy.html
test -f src/main/resources/static/account-deletion.html
rg 'RentFlow' android/scripts/.env.twa.example >/dev/null

echo "[5/6] Syntax checks"
bash -n android/scripts/build_twa.sh
bash -n android/scripts/pre_release_check.sh
bash -n scripts/domain_check.sh

echo "[6/6] Git status summary"
git status --short

echo "Pre-release static checks passed."
