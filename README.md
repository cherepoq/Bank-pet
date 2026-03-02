# Bank Pet (Java Middle Interview Demo)

Полноценный демо-банк на Spring Boot:
- свой банк + свои карты/счета клиентов;
- кошелёк цифрового рубля;
- AI Spending Guardian + LLM-анализ риска;
- NFC-платежи под капотом;
- синхронизация истории из других банков;
- REST API + Web UI (Thymeleaf).

## Самый простой запуск (рекомендуется)
### Вариант A — в Docker (1 команда)
```bash
./scripts/run_docker.sh
```
После запуска открыть `http://localhost:8080/app`.

### Вариант B — локально через Maven
```bash
./scripts/local_preview.sh
```

### Вариант C — публикация как Android (Google Play)
1. Развернуть сайт по HTTPS (например Render/Railway).
2. Сгенерировать TWA bundle:
   ```bash
   cd android/scripts
   ./build_twa.sh
   ```
3. Загрузить `.aab` сначала в **Internal testing**.

### Где хостить сайт
- **Render** — самый быстрый старт (уже есть `render.yaml`).
- **Railway** — аналогично просто, подходит для демо.
- **VPS (Timeweb/Selectel/Hetzner)** — если нужен полный контроль.

## Как проверить работоспособность до Play Market
### 1) Статический pre-release check
```bash
cd android/scripts
./pre_release_check.sh
```

### 2) Быстрый локальный сценарий (автоматический)
```bash
./scripts/local_preview.sh
```
Скрипт прогонит pre-release check, поднимет приложение, проверит `/app` и `manifest`.

### 3) Локальный запуск (вручную)
```bash
mvn spring-boot:run
```
Открыть:
- `http://localhost:8080/app`
- `http://localhost:8080/h2-console`

### 4) Ручной smoke test (обязательно)
- Проверить верхнюю строку: счет + карта + цифровой рубль.
- Проверить покупку по категориям и popup агента.
- Проверить кнопку отмены (сине-зеленый градиент), счетчики отказов и суммы.
- Проверить вкладку `Платежи` и синк истории.

### 5) Перед релизом в Play
- Развернуть staging HTTPS.
- Собрать TWA `.aab`.
- Загрузить сначала в **Internal testing**, не сразу в прод.

Детали: `android/README.md`.

## Безопасность
- PAN карт хранится как `encryptedPan`.
- Используется `CardDataProtectionService` (AES/GCM).
- Для production: секреты только в KMS/Secret Manager, ротация ключей, аудит.

## Реалистичность интеграций
- NFC/карты банков: реально через официальные платежные контуры и партнерские интеграции.
- Цифровой рубль: реально только при официальном API/регламенте и комплаенсе.


## Дальнейшие улучшения (план)
1. **Тесты перед релизом**
   - unit тесты для `SpendingGuardianAgent` (все профили + блокировки/подтверждения);
   - интеграционные тесты controller/service сценариев;
   - e2e smoke для UI (playwright).
2. **Релизная готовность**
   - staging окружение + pre_release_check.sh + internal testing в Play;
   - crash/analytics (Firebase Crashlytics + Analytics);
   - feature-flags на риск-политику.
3. **ИИ-агент (речь)**
   - синтез речи уже включен;
   - добавлено распознавание голосовых команд в popup ("оплатить"/"отмена");
   - следующий шаг — серверный ASR/NLU и персонализация по истории трат.
4. **Безопасность**
   - ключи шифрования только в KMS/Secret Manager;
   - ротация ключей и журнал аудита всех критичных решений.
