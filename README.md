# Bank Pet (Java Middle Interview Demo)

Полноценный демо-банк на Spring Boot:
- клиент, счета, карты, транзакции;
- кошелёк цифрового рубля;
- AI Spending Guardian + LLM-анализ риска;
- NFC-платежи под капотом (без отдельного NFC-экрана);
- синхронизация истории из других банков;
- REST API + Web UI (Thymeleaf).

## Что обновлено
- Реакция агента теперь как **всплывающее окно** с эмодзи по центру, ругающимся текстом, субтитрами и озвучкой через `speechSynthesis` на телефоне.
- Ввод для пользователя сделан естественнее: покупка через **выпадающий список категорий** без ручного ввода текста.
- Добавлены категории: `Betting`, `OZON`, `WB`, `Casino`, `Вкусняшки`.
- Добавлена вкладка **Платежи** с историей и счётчиком, сколько раз пользователь отказался от импульсивной траты.
- NFC не показывается как отдельная функция в UI, но используется под капотом в обработке оплаты.

## Реально ли NFC под капотом в банке?
Да, это реалистично:
1. Пользователь нажимает обычную кнопку «Оплатить».
2. Под капотом клиентское приложение передает device/payment token.
3. Бэкенд идёт в NFC/payment gateway (tokenized auth).
4. Дальше policy+LLM проверка и решение.

Т.е. UI может быть “обычным”, а NFC-цепочка — инфраструктурной.

## Архитектурные блоки
- `NfcPaymentGateway` — адаптер NFC-процессинга.
- `ExternalBankHistorySyncService` — верхний слой импорта истории (open banking).
- `SpendingGuardianAgent` — policy решение (`SOFT/MEDIUM/HARD`).
- `LlmSpendingAdvisor` — вспомогательная LLM-оценка риска (сейчас эвристический stub).

## API
- `POST /api/v1/clients/{clientId}/payments` — обычная оплата.
- `POST /api/v1/clients/{clientId}/payments/nfc` — NFC backend flow.
- `POST /api/v1/clients/{clientId}/history/sync` — подтянуть историю из внешних банков.
- `GET/PUT /api/v1/clients/{clientId}/spending-filters` — настройки фильтров.

## Почему Spring мог не стартовать
Если видишь `Non-resolvable parent POM ... 403 Forbidden`, то это доступ к Maven Central, а не ошибка бизнес-кода.

Проверка:
- доступ к `https://repo.maven.apache.org/maven2`
- proxy/VPN/firewall
- `~/.m2/settings.xml`
- `mvn -U clean package`
