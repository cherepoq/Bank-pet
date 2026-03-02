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
3. Или подними приложение локально вручную:
   ```bash
   mvn spring-boot:run
   ```
4. Пройди ручной smoke-check в UI:
   - открыть `/app`;
   - создать покупку по каждой ключевой категории;
   - проверить popup агента (emoji + текст + кнопки);
   - проверить вкладку `Платежи` и счетчики отказов/суммы;
   - проверить цифровой рубль (link + topup).
5. Разверни staging по HTTPS.
6. Собери TWA и загрузи в **Internal testing** в Play Console.
7. Пройди тестовый прогон на реальном Android-устройстве.

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
