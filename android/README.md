# Android / Google Play plan for Bank Pet

Публикация идёт через **TWA (Trusted Web Activity)** — это быстрый и практичный путь для web-first приложения.

## Как убедиться, что всё работает до загрузки в Play
1. Запусти локальный pre-release check:
   ```bash
   cd android/scripts
   ./pre_release_check.sh
   ```
2. Быстрый автоматический прогон (рекомендуется):
   ```bash
   ./scripts/local_preview.sh
   ```
   Если backend не стартует в текущем окружении, скрипт сам переключится на static UI preview.
3. Только UI preview (без backend):
   ```bash
   ./scripts/ui_preview.sh
   ```
4. Или подними приложение локально вручную:
   ```bash
   mvn spring-boot:run
   ```
5. Пройди ручной smoke-check в UI:
   - открыть `/app`;
   - создать покупку по каждой ключевой категории;
   - проверить popup агента (emoji + текст + кнопки);
   - проверить вкладку `Платежи` и счетчики отказов/суммы;
   - проверить цифровой рубль (link + topup).
6. Разверни staging по HTTPS.
7. Собери TWA и загрузи в **Internal testing** в Play Console.
8. Пройди тестовый прогон на реальном Android-устройстве.

## Быстрый старт сборки TWA
1. Подготовь `assetlinks.json` по шаблону `android/playstore/assetlinks.json.template`.
2. Установи Bubblewrap:
   ```bash
   npm i -g @bubblewrap/cli
   ```
3. Заполни env:
   ```bash
   cp android/scripts/.env.twa.example android/scripts/.env.twa
   ```
4. Собери обертку:
   ```bash
   cd android/scripts
   ./build_twa.sh
   ```

## Что получится
- Android bundle (`.aab`) для загрузки в Google Play.
- Чеклист публикации: `android/playstore/store-listing-checklist.md`.


## Откуда брать URL для Play/TWA
Перед сборкой TWA нужен публичный HTTPS URL приложения. Самые простые варианты:
- Render (рекомендуется, есть `render.yaml`),
- Railway,
- любой VPS с Nginx + Docker.

После деплоя подставьте этот URL в `android/scripts/.env.twa`.
