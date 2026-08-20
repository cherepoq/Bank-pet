# RentFlow MVP

Мобильное PWA для управления арендой. Интерфейс построен вокруг ежедневных сценариев арендодателя и арендатора: сроки, платежи, документы и видеофиксация состояния объекта.

## Что работает в MVP

- переключение ролей «Арендодатель» / «Арендатор»;
- обзор ближайшего платежа и обратный отсчёт;
- долгосрочные и посуточные объекты;
- добавление аренды с сохранением данных на устройстве;
- отметка платежа;
- загрузка и поиск файлов;
- раздел видеоосмотров;
- адаптивный интерфейс и установка как PWA.

## Запуск

### Быстрый просмотр только фронтенда

Для этого варианта не нужны Java, Maven или Docker:

```bash
./scripts/static_preview.sh
```

Откройте `http://localhost:4173`. Другой порт можно указать так: `PORT=3000 ./scripts/static_preview.sh`.

### Docker

```bash
./scripts/run_docker.sh
```

Откройте `http://localhost:8080/app`.

### Maven

```bash
./scripts/local_preview.sh
```

## Публичный preview через GitHub Pages

В репозитории настроен workflow `.github/workflows/pages.yml`. После попадания изменений в ветку `main`:

1. Откройте **Settings → Pages** в GitHub.
2. В поле **Source** выберите **GitHub Actions**.
3. Откройте **Actions → Deploy RentFlow preview to GitHub Pages** и дождитесь зелёного статуса.
4. Ссылка появится на странице deployment и будет иметь вид `https://<username>.github.io/<repository>/`.

Workflow также можно запустить вручную кнопкой **Run workflow**. Относительные пути ресурсов позволяют preview работать как в корне домена, так и в подпапке GitHub Pages.

## Архитектура

Существующий runtime использует Spring Boot 3 и Java 21. Клиент MVP находится в `src/main/resources/templates/dashboard.html`, стили — в `static/css`, сценарии — в `static/js`.

Целевая production-архитектура может быть перенесена на ASP.NET Core без изменения UX-контрактов. Для production-релиза потребуются PostgreSQL, объектное хранилище, OIDC/OAuth, серверная авторизация по ролям, KMS для ключей, шифрование файлов и аудит операций. Текущий MVP не выдаёт браузерное `localStorage` за защищённое серверное хранилище.

## Android

PWA можно упаковать в Trusted Web Activity. Инструкции находятся в [`android/README.md`](android/README.md).
