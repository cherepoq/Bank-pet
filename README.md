# Bank Pet (Java Middle Interview Demo)

Демо-банкинг на Spring Boot, который можно показывать на собесе:
- клиент, счета, карты, транзакции;
- отдельный кошелёк цифрового рубля;
- REST API + Web UI (Thymeleaf);
- H2 in-memory БД (без поднятия внешних зависимостей).

## Технологии
- Java 21
- Spring Boot 3 (Web, Data JPA, Validation, Thymeleaf)
- H2 Database
- Maven

## Архитектура
- `controller` — REST и web endpoints
- `service` — бизнес-логика
- `repository` — доступ к данным
- `entity` — JPA-модели и связи
- `dto` — объекты ответа
- `config/DemoDataInitializer` — стартовые данные

## Связи в БД
- `Client 1..* Account`
- `Account 1..* Card`
- `Client 1..1 DigitalRubleWallet`
- `Client 1..* PaymentTransaction`

## Быстрый запуск
```bash
mvn spring-boot:run
```

Web UI:
- `http://localhost:8080/app`

Пример REST:
1) Получить demo client id:
```bash
curl http://localhost:8080/app
```
(или через H2 console `http://localhost:8080/h2-console`)

2) Дашборд:
```bash
curl "http://localhost:8080/api/v1/clients/{clientId}/dashboard"
```

3) Привязать цифровой рубль:
```bash
curl -X POST "http://localhost:8080/api/v1/clients/{clientId}/digital-ruble/link"
```

4) Пополнить цифровой рубль:
```bash
curl -X POST "http://localhost:8080/api/v1/clients/{clientId}/digital-ruble/top-up?amount=500"
```
