#!/usr/bin/env bash
set -euo pipefail

if ! command -v bubblewrap >/dev/null 2>&1; then
  echo "bubblewrap not found. Install with: npm i -g @bubblewrap/cli"
  exit 1
fi

if [ ! -f .env.twa ]; then
  echo "Create android/scripts/.env.twa from .env.twa.example"
  exit 1
fi

source .env.twa

bubblewrap init \
  --manifest "${WEB_MANIFEST_URL}" \
  --directory "${OUTPUT_DIR}" \
  --packageId "${PACKAGE_ID}" \
  --name "${APP_NAME}" \
  --launcherName "${LAUNCHER_NAME}" \
  --host "${HOST}" \
  --startUrl "${START_URL}"

bubblewrap build --directory "${OUTPUT_DIR}"

echo "AAB/APK generated in ${OUTPUT_DIR}"
