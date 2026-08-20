#!/usr/bin/env bash
set -euo pipefail

DOMAIN="${1:-${DOMAIN:-}}"
if [[ -z "$DOMAIN" ]]; then
  echo "Usage: $0 rentflow.example.com" >&2
  exit 2
fi

echo "[1/7] DNS"
getent ahosts "$DOMAIN" | head -n 1
echo "[2/7] HTTP redirects to HTTPS"
curl --silent --show-error --head "http://$DOMAIN/app" | tr -d '\r' | rg -qi '^location: https://'
echo "[3/7] HTTPS application"
curl --fail --silent --show-error "https://$DOMAIN/app" | rg -q "RentFlow"
echo "[4/7] PWA manifest"
curl --fail --silent --show-error "https://$DOMAIN/manifest.webmanifest" | rg -q '"start_url"\s*:\s*"/app"'
echo "[5/7] Android Digital Asset Links"
curl --fail --silent --show-error "https://$DOMAIN/.well-known/assetlinks.json" | rg -q 'app.rentflow.mobile'
echo "[6/7] Privacy and account deletion"
curl --fail --silent --show-error "https://$DOMAIN/privacy.html" | rg -q "Политика конфиденциальности"
curl --fail --silent --show-error "https://$DOMAIN/account-deletion.html" | rg -q "Удаление аккаунта"
echo "[7/7] Security headers"
curl --fail --silent --show-error --head "https://$DOMAIN/app" | tr -d '\r' | rg -qi '^strict-transport-security:'
echo "Domain is ready for Android Internal testing: https://$DOMAIN/app"
