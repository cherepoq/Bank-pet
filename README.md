# Bank Pet (Java Middle Interview Demo)

Полноценный демо-банк на Spring Boot для собеседований уровня Middle Java:
- клиент, счета, карты, транзакции;
- отдельный кошелёк цифрового рубля;
- AI Spending Guardian (анти-импульсивный агент при платеже);
- REST API + Web UI (Thymeleaf);
- PWA-подготовка (manifest + service worker) для мобильного UX;
- Docker + Render для публичного доступа в интернете.

## Технологии
- Java 21
- Spring Boot 3 (Web, Data JPA, Validation, Thymeleaf)
- H2 Database
- Maven
- Docker / Render

## Что посадить в прод
Рекомендуемый вариант для демо и портфолио:
1. **Render (Docker Web Service)** — быстрое развертывание и публичная ссылка.
2. Для production-уровня: **Yandex Cloud / AWS / Hetzner VPS + Docker + Nginx + PostgreSQL**.

`render.yaml` уже добавлен, можно деплоить как Blueprint.

## Архитектура
- `controller` — REST и web endpoints
- `service` — бизнес-логика + AI guardian
- `repository` — доступ к данным
- `entity` — JPA-модели и связи
- `dto` — объекты запроса/ответа
- `config/DemoDataInitializer` — стартовые данные

## AI Spending Guardian
Агент работает перед оплатой и:
1. **блокирует** запрещённые категории (`BETTING`, `SCAM`, `GAMBLING`);
2. для рискованных категорий (`GAMES`, `ALCOHOL`, `LUXURY`, `CRYPTO`) и крупных сумм просит подтверждение;
3. показывает сценарий "Да/Нет" в UI перед списанием.

Настройка в `application.yml`:
```yaml
app:
  guardian:
    blocked-categories: BETTING,SCAM,GAMBLING
    risky-categories: GAMES,ALCOHOL,LUXURY,CRYPTO
```

## Адаптив и UX
- Неоновая тема с градиентом **розовый → жёлтый**.
- Переключение светлой/тёмной темы.
- Адаптивная сетка для мобилок/планшетов/десктопа.
- PWA: `manifest.webmanifest` + `sw.js` + офлайн-кеш базовых ресурсов.

## Версия для Android и Play Market
### Вариант 1 (рекомендуется): TWA (Trusted Web Activity)
1. Развернуть сайт по HTTPS (например, Render).
2. Подтвердить домен через `assetlinks.json`.
3. Сгенерировать Android-обёртку через Bubblewrap.
4. Собрать `.aab` и загрузить в Google Play Console.

### Вариант 2: WebView-приложение
Сделать нативную обёртку Android (Kotlin) с `WebView` на URL вашего сайта. Быстрее, но хуже UX и SEO/интеграций, чем TWA.

## Быстрый запуск локально
```bash
mvn spring-boot:run
```

Web UI:
- `http://localhost:8080/app`

## REST примеры
`POST /api/v1/clients/{clientId}/payments`
```json
{
  "title": "Покупка PS5",
  "amount": 79990,
  "category": "GAMES",
  "confirmedByUser": false
}
```

Статусы ответа:
- `APPROVED`
- `REJECTED`
- `NEEDS_CONFIRMATION`

## Деплой в интернет (Render)
1. Запушить репозиторий в GitHub.
2. На render.com: **New +** → **Blueprint**.
3. Указать репозиторий, Render использует `render.yaml`.
4. Получить публичный URL вида `https://bank-pet.onrender.com/app`.
