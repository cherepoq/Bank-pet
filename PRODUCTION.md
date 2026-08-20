# RentFlow — путь до Google Play production

## Уже автоматизировано

- CI: frontend syntax, Android pre-release checks, backend tests и production JAR.
- PostgreSQL и persistent encrypted media volume в Docker Compose.
- Серверная загрузка, скачивание и удаление медиа.
- Проверка MIME/signature, SHA-256, AES-256-GCM, квоты и ограничения размера.
- FFmpeg H.264/AAC pipeline для видео.
- TWA package id, manifest, asset links template, privacy draft и store checklist.
- Caddy HTTPS reverse proxy, динамический Digital Asset Links endpoint и domain readiness check.
- Encrypted media format RFV2 связывает шифротекст с владельцем, типом и UUID через authenticated additional data.

## Release gates

### Gate 1 — staging

- [ ] Выбрать HTTPS-домен и production hosting.
- [ ] Подключить managed PostgreSQL, S3-compatible object storage и backup.
- [ ] Заменить shared media token на OIDC/JWT и серверную проверку участников договора.
- [ ] Подключить KMS и ротацию media encryption keys.
- [ ] Добавить malware scanner и асинхронную очередь FFmpeg.
- [ ] Настроить monitoring, structured logs, error tracking и alerts.

### Gate 2 — Android Internal testing

- [ ] Создать приложение `app.rentflow.mobile` в Play Console.
- [ ] Создать upload key и сохранить его только в защищённом CI environment.
- [ ] Опубликовать `/.well-known/assetlinks.json` с release SHA-256.
- [ ] Собрать подписанный `.aab` через Bubblewrap.
- [ ] Загрузить `.aab`, privacy URL, screenshots, icon и feature graphic.
- [ ] Заполнить Data safety, content rating, target audience и account deletion URL.

### Gate 3 — Closed testing

- [ ] Проверить регистрацию, восстановление доступа и удаление аккаунта.
- [ ] Проверить загрузку PDF/JPG/MP4 на слабой сети и продолжение после сбоя.
- [ ] Проверить доступ арендатора и арендодателя к общим чекам.
- [ ] Провести restore drill базы и файлов.
- [ ] Исправить crash/ANR и accessibility blockers.

### Gate 4 — Production

- [ ] Подписать юридически корректные privacy policy и terms.
- [ ] Провести security review и dependency scan.
- [ ] Выпустить staged rollout 10%, затем 25%, 50% и 100%.
- [ ] На каждом этапе контролировать crash-free users, ANR, API errors и upload failures.

Платежи через RentFlow не входят в первый production-релиз. MVP хранит только ручные записи и чеки; банковские данные и реквизиты карт не принимаются.
