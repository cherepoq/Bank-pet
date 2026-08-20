# Как разрешать конфликты RentFlow без поломки интерфейса

Не нажимайте **Accept current changes** для всех файлов. Слова current/incoming зависят от направления merge: в GitHub текущей может оказаться старая `main`, а локально при `git merge origin/main` текущей является feature-ветка.

## Безопасный порядок

```bash
git fetch origin
git switch <ваша-feature-ветка>
git merge origin/main
```

Разрешайте каждый конфликт по смыслу. Для frontend итог обязательно должен содержать:

- `style.css`, первая строка которого — `:root {`;
- полные блоки `.app-header`, `.hero-card`, `.bottom-nav`;
- version query в ссылке `css/style.css?v=...` внутри `dashboard.html`;
- актуальный cache id `rentflow-v10` или новее в `sw.js`.

Если в feature-ветке находится новый RentFlow UI, а в `main` — старый сломанный CSS, при локальном merge оставьте версии feature-ветки:

```bash
git checkout --ours src/main/resources/static/css/style.css
git checkout --ours src/main/resources/templates/dashboard.html
git checkout --ours src/main/resources/static/js/theme.js
git checkout --ours src/main/resources/static/sw.js
git add src/main/resources/static
```

Команда `--ours` верна только когда вы уже на feature-ветке и выполняете `git merge origin/main`. Не копируйте её в другой сценарий автоматически.

## Проверка до commit

```bash
! rg '^(<<<<<<<|=======|>>>>>>>)' . \
  --glob '!target/**' --glob '!.git/**'
test "$(sed -n '1p' src/main/resources/static/css/style.css)" = ':root {'
rg '^\.app-header \{' src/main/resources/static/css/style.css
rg '^\.bottom-nav \{' src/main/resources/static/css/style.css
node --check src/main/resources/static/js/theme.js
node --check src/main/resources/static/sw.js
./android/scripts/pre_release_check.sh
./scripts/static_preview.sh
```

Откройте `http://localhost:4173` до отправки merge. Только после визуальной проверки:

```bash
git commit
git push
```

## Проверка после GitHub Pages deployment

Откройте:

```text
<pages-url>/revision.txt
<pages-url>/build-info.txt
<pages-url>/css/style.css
```

Первая строка опубликованного CSS должна быть `:root {`. Если workflow зелёный, но SHA в `revision.txt` старый, был запущен workflow не из той ветки. Если workflow падает на `Assemble static preview`, конфликт снова вернул неполный CSS — такой deployment теперь намеренно блокируется.
