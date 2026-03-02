# Bank Pet (Java Middle Interview Demo)

Полноценный демо-банк на Spring Boot:
- клиент, счета, карты, транзакции;
- кошелёк цифрового рубля;
- AI Spending Guardian + LLM-анализ риска;
- NFC-платежи под капотом (без отдельного NFC-экрана);
- синхронизация истории из других банков;
- REST API + Web UI (Thymeleaf).

## Что обновлено
- Реакция агента: popup по центру, эмодзи, ругающийся текст, субтитры и озвучка через `speechSynthesis`.
- Добавлено боковое меню (sidebar) с предварительными настройками характера агента:
  - `Сбалансированный`
  - `Дружелюбный`
  - `Строгий`
- Категории покупок через выпадающий список: `Betting`, `OZON`, `WB`, `Casino`, `Вкусняшки`.
- Вкладка `Платежи`: история + два счетчика защиты:
  - сколько раз пользователь отказался от импульсивной покупки;
  - сколько денег сэкономлено на отменённых тратах.
- Кнопка отказа в popup сделана с сине-зелёным градиентом.

## Реально ли NFC под капотом в банке?
Да, это реалистично:
1. Пользователь нажимает обычную кнопку «Оплатить».
2. Под капотом клиентское приложение передает device/payment token.
3. Бэкенд идёт в NFC/payment gateway (tokenized auth).
4. Далее — policy+LLM проверка и решение.

## Архитектурные блоки
- `NfcPaymentGateway` — адаптер NFC-процессинга.
- `ExternalBankHistorySyncService` — верхний слой импорта истории (open banking).
- `SpendingGuardianAgent` — policy решение (`SOFT/MEDIUM/HARD`) + профиль характера.
- `LlmSpendingAdvisor` — вспомогательная LLM-оценка риска (эвристический stub).

## API
- `POST /api/v1/clients/{clientId}/payments`
- `POST /api/v1/clients/{clientId}/payments/nfc`
- `POST /api/v1/clients/{clientId}/history/sync`
- `GET/PUT /api/v1/clients/{clientId}/spending-filters`

## Почему Spring может не стартовать
Если есть `Non-resolvable parent POM ... 403 Forbidden`, это доступ к Maven Central, а не ошибка бизнес-кода.
