# Bank Pet (Java Middle Interview Demo)

Полноценный демо-банк на Spring Boot для собеседований уровня Middle Java:
- клиент, счета, карты, транзакции;
- отдельный кошелёк цифрового рубля;
- AI Spending Guardian + LLM-анализ риска по назначению платежа;
- гибкие пользовательские фильтры трат;
- REST API + Web UI (Thymeleaf);
- Docker + Render для публичного доступа в интернете.

## Что улучшено сейчас
- Контрастность текста увеличена.
- Фон сделан **темнее сверху и светлее снизу** (как просили).
- Добавлена форма настройки фильтров трат прямо в UI.
- LLM-агент включается/выключается в фильтрах.

## Почему Spring мог не подниматься у тебя
В этой среде `mvn test`/`mvn spring-boot:run` падает не из-за кода, а из-за сети к Maven Central:
- ошибка: `Non-resolvable parent POM ... spring-boot-starter-parent ... 403 Forbidden`.

То есть зависимости Spring Boot не скачиваются. На локальной машине обычно лечится так:
1. Проверить доступ к `https://repo.maven.apache.org/maven2`.
2. Отключить корпоративный proxy/VPN, или правильно настроить `~/.m2/settings.xml`.
3. Очистить кэш и скачать заново: `mvn -U clean package`.
4. Если нужно, зеркалировать репозиторий через Nexus/Artifactory.

## Как реализовано подключение цифрового рубля
В демо-проекте это внутренний кошелёк `DigitalRubleWallet`, связанный 1:1 с клиентом:
1. endpoint `/digital-ruble/link` выставляет `linked=true`;
2. `/digital-ruble/top-up` переводит деньги с обычного рублёвого счёта на кошелёк;
3. транзакция пишется в историю как отдельная операция.

Для реальной интеграции с внешним провайдером/ЦБ:
- добавляется адаптер (Spring `@Service` + `WebClient`) к внешнему API;
- вводятся статусы синхронизации, idempotency keys, retries и аудит;
- хранится внешний wallet/account id и статусы операций.

## LLM-агент и фильтры трат
Фильтры теперь на клиента:
- `llmAgentEnabled` — включить/выключить LLM-анализ;
- `hardBlockEnabled` — жёстко блокировать запрещённые категории;
- `confirmationThreshold` — порог обязательного подтверждения;
- `blockedCategoriesCsv`, `riskyCategoriesCsv` — кастомные категории.

API:
- `GET /api/v1/clients/{clientId}/spending-filters`
- `PUT /api/v1/clients/{clientId}/spending-filters`

## На что посадить сайт (сервер)
### Быстрый и простой путь
- **Render + Docker** (уже готово: `Dockerfile`, `render.yaml`).

### Ближе к production
- VPS (Hetzner/Timeweb/Yandex Cloud) + Docker Compose
- Nginx (TLS/HTTPS, reverse proxy)
- PostgreSQL вместо H2
- CI/CD (GitHub Actions)

## Версия для Android / Play Market
Рекомендуется **TWA (Trusted Web Activity)**:
1. Развернуть сайт по HTTPS.
2. Настроить `assetlinks.json`.
3. Bubblewrap -> сборка `.aab`.
4. Публикация в Google Play Console.

См. подробнее: `android/README.md`.
