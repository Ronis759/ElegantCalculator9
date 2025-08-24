# ElegantCalculator (Android, Kotlin + Jetpack Compose)

Быстрый красивый калькулятор с градиентным фоном и крупными клавишами.

## Сборка APK
1. Установи Android Studio (Arctic Fox+), открой проект папкой.
2. Дай студии докачать зависимости (Gradle).
3. Для сборки дебаг-APK:
   - В меню: **Build → Build Bundle(s) / APK(s) → Build APK(s)**, или
   - В терминале: `./gradlew :app:assembleDebug`
4. Готовый файл появится в `app/build/outputs/apk/debug/app-debug.apk`.
5. Для релиза: `./gradlew :app:assembleRelease` (понадобится ключ для подписи).

## Примечание
- minSdk 24, target/compileSdk 35.
- UI — Jetpack Compose + Material 3.
- Поддерживаются операции: `+ - × ÷ %` и десятичные числа, кнопки C, ⌫, =.
---

## Сборка APK в облаке (без установки Android Studio)
1. Создай пустой репозиторий на GitHub и залей туда всё содержимое папки проекта.
2. Открой вкладку **Actions** → появится workflow **Build Android APK**.
3. Запусти **Run workflow** или просто сделай push — сборка начнётся автоматически.
4. По завершении открой **Actions → последний ран** и скачай артефакт **ElegantCalculator-debug-apk** — внутри `app-debug.apk`.

> В workflow используется Java 17 и Gradle 8.7. Wrapper не обязателен — action сам подтянет Gradle.
