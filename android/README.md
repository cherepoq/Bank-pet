# Android wrapper plan (Play Market)

Этот проект готовится к публикации в Google Play через **TWA** поверх сайта.

## Шаги
1. Разверните backend+web по HTTPS (Render URL).
2. Настройте `assetlinks.json` на домене.
3. Установите Bubblewrap (`npm i -g @bubblewrap/cli`).
4. Выполните:
   ```bash
   bubblewrap init --manifest https://<your-domain>/manifest.webmanifest
   bubblewrap build
   ```
5. Подпишите `.aab` и загрузите в Google Play Console.

## Почему TWA
- один код интерфейса для Web + Android;
- быстрее выкладка обновлений;
- нативное отображение и иконка приложения в лаунчере.
