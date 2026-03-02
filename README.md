# Bank Pet (Java Middle Interview Demo)

Полноценный демо-банк на Spring Boot для собеседований уровня Middle Java:
- клиент, счета, карты, транзакции;
- отдельный кошелёк цифрового рубля;
- AI Spending Guardian (анти-импульсивный агент при платеже);
- REST API + Web UI (Thymeleaf);
- H2 in-memory БД (без поднятия внешних зависимостей);
- готовность к деплою в интернет через Docker + Render.

## Технологии
- Java 21
- Spring Boot 3 (Web, Data JPA, Validation, Thymeleaf)
- H2 Database
- Maven
- Docker / Render

## Архитектура
- `controller` — REST и web endpoints
- `service` — бизнес-логика + AI guardian
- `repository` — доступ к данным
- `entity` — JPA-модели и связи
- `dto` — объекты запроса/ответа
- `config/DemoDataInitializer` — стартовые данные

## Связи в БД
- `Client 1..* Account`
- `Account 1..* Card`
- `Client 1..1 DigitalRubleWallet`
- `Client 1..* PaymentTransaction`

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
4. После деплоя получить публичный URL вида `https://bank-pet.onrender.com/app`.

Либо deploy как Docker Web Service вручную (Dockerfile уже есть).
