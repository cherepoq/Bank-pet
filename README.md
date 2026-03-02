# Bank Pet (Java Middle Interview Demo)

Полноценный демо-банк на Spring Boot для собеседований уровня Middle Java:
- клиент, счета, карты, транзакции;
- отдельный кошелёк цифрового рубля;
- AI Spending Guardian + LLM-анализ риска;
- NFC-платежи (через интеграционный gateway-слой);
- синхронизация истории из внешних банков (верхний интеграционный слой);
- REST API + Web UI (Thymeleaf);
- Docker + Render для публичного доступа.

## Что сделано по последнему фидбеку
- Убрана лишняя надпись про контраст.
- Настройки AI-фильтров сделаны более понятными (человеческие формулировки).
- Добавлены категории: `OZON`, `WB`, `ВКУСНЯШКИ`.
- Добавлены уровни ругательств/реакций агента: `SOFT`, `MEDIUM`, `HARD`.
- Вместо обычного “ручного платежа” в UI вынесен сценарий `NFC-оплаты` + кнопка подтягивания истории из других банков.

## Архитектура интеграций (важно)
### 1) NFC-оплата
- `NfcPaymentGateway` — изолированный интеграционный слой (anti-corruption layer).
- Сейчас в демо: mock-проверка токена устройства.
- В проде: подключение к токенизированной платежной шине/процессингу, 3DS/Device binding, антифрод.

### 2) Подтягивание истории из других банков
- `ExternalBankHistorySyncService` — верхний слой синхронизации истории.
- Сейчас в демо: mock-провайдер транзакций.
- В проде: Open Banking API, OAuth2 consent, пагинация, дедупликация, аудит.

### 3) Агент решений по тратам
- `SpendingGuardianAgent` принимает решение с учетом:
  - пользовательских фильтров,
  - категории,
  - суммы,
  - LLM-риск-оценки,
  - подтверждения пользователя.
- `LlmSpendingAdvisor` сейчас эвристический (как заглушка LLM).

## Какого агента подключать в реальности
Рекомендуемый вариант:
1. **Policy Agent (обязательный слой)** — deterministic правила + лимиты + категории + комплаенс.
2. **LLM Advisor (вспомогательный слой)** — объяснение риска и NLP-анализ назначения платежа.
3. Итоговое решение принимает Policy слой, а не LLM (чтобы было контролируемо и объяснимо).

Технически в Spring:
- `PolicyEngineService` + `LlmRiskService` + `DecisionOrchestratorService`.
- Логирование каждого решения + reason codes.

## Почему Spring может “не подниматься”
Если ошибка типа:
`Non-resolvable parent POM ... spring-boot-starter-parent ... 403 Forbidden`
— это не баг бизнес-логики, а проблема доступа к Maven Central.

Что делать:
1. Проверить доступ к `https://repo.maven.apache.org/maven2`.
2. Проверить proxy/VPN/корп.файрвол.
3. Настроить `~/.m2/settings.xml` (mirror/repo credentials).
4. `mvn -U clean package`.

## Цифровой рубль: как подключать по-взрослому
Сейчас: внутренний кошелек и операции `link` / `top-up`.

Прод-версия:
- адаптер `DigitalRubleProviderClient` (WebClient/Feign),
- idempotency keys,
- асинхронные статусы операций,
- reconciliation job,
- event log/audit trail,
- fallback/retry/circuit breaker.

## Деплой
### Быстро
- Render + Docker (`render.yaml` и `Dockerfile` уже есть).

### Нормальный production-путь
- VPS/Cloud + Docker Compose
- Nginx (TLS, reverse proxy)
- PostgreSQL
- Observability (Prometheus/Grafana, logs)

## Android / Play Market
- TWA (Trusted Web Activity) поверх веб-приложения.
- Детали: `android/README.md`.
