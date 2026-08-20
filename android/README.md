# RentFlow для Google Play

Первый Android-релиз использует Trusted Web Activity. Это позволяет выпустить текущий PWA как `.aab`, а позже заменить оболочку на .NET MAUI без изменения серверного API.

## Подготовка

1. Разверните production по HTTPS и проверьте `/app`, `/manifest.webmanifest`, `/privacy.html` и `/api/media`.
2. Опубликуйте `assetlinks.json` по адресу `https://<domain>/.well-known/assetlinks.json`.
3. Установите Bubblewrap: `npm i -g @bubblewrap/cli`.
4. Скопируйте `android/scripts/.env.twa.example` в `.env.twa` и заполните домен.
5. Выполните `android/scripts/pre_release_check.sh`.
6. Выполните `cd android/scripts && ./build_twa.sh`.

Полученный `.aab` сначала загружается в Internal testing, затем в Closed testing. Перед production заполните Data safety, privacy policy, content rating и store listing checklist.

## Ограничения первого релиза

TWA требует доступного HTTPS-сервера. Загрузка видео выполняется сервером и может продолжаться только пока web-клиент активен. Для гарантированной фоновой загрузки, Android Keystore, push и камеры следующим этапом предусмотрен клиент .NET MAUI.
