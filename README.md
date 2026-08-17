# Forklift

An Android app for tracking calories/macros and lifting sessions together, with
visual thresholds for protein/fat, muscle-coverage exercise suggestions, and a
weight-vs-intake correlation view.

## Screenshots

| Dashboard | Shared meal time | Food logging | Profile and reminders |
| --- | --- | --- | --- |
| ![Forklift dashboard](https://raw.githubusercontent.com/AA-2020743/Forklift/main/docs/images/dashboard.png?v=d92c6c9) | ![Shared meal time control](https://raw.githubusercontent.com/AA-2020743/Forklift/main/docs/images/meal-time.png?v=d92c6c9) | ![Food logging screen](https://raw.githubusercontent.com/AA-2020743/Forklift/main/docs/images/add-food.png?v=d92c6c9) | ![Profile and reminder settings](https://raw.githubusercontent.com/AA-2020743/Forklift/main/docs/images/profile.png?v=d92c6c9) |

The screenshots show the responsive Compose UI on a Pixel emulator, including the
dashboard, one shared time for a meal/day, food logging, and profile controls.

## Features

- **Barcode scanning** (CameraX + ML Kit) looked up against the free
  [Open Food Facts](https://openfoodfacts.org) database — no API key required.
- **Recent / frequent / favorite foods** remembered locally so repeat logging is
  one tap, plus manual entry and text search (local + online).
- **Grams or servings** quantity entry, per food item.
- **Meals**: built-in and user-managed meal slots, with a shared consumed-at time for each meal/day.
- **Protein spacing reminders**: configurable protein dose/gap and waking window, anchored to
  actual meal times.
- **Hydration tracking**: plain water plus estimated hydration from logged foods and drinks.
- **Macro thresholds**: protein defaults to a 1.6-2.4 g/kg body-weight range; fat has
  a configurable safe floor/ceiling; carbs fill
  whatever calorie budget is left. The dashboard color-codes each macro
  (red = below threshold, amber = approaching, green = on target, blue = over).
- **Lifting tracker**: sessions → exercises → sets (weight × reps), with an
  exercise library tagged down to the sub-muscle level (e.g. biceps long vs.
  short head, all three triceps heads, upper/mid/lower chest). A weekly
  coverage report flags undertrained sub-groups and suggests specific
  exercises to fill the gap.
- **Weight tracking** with a daily reminder notification (WorkManager) and a
  trend view that estimates your *actual* maintenance calories from logged
  weight + intake, comparing it to your calorie target and suggesting
  adjustments.

## Architecture

Single-module Kotlin app, Jetpack Compose UI, no DI framework (a small
hand-rolled `AppContainer`, see `di/`) — appropriate for a project this size.

```
data/local/      Room entities, DAOs, database
data/remote/     Open Food Facts Retrofit API + DTOs
data/repository/ Repositories mediating local DB + network
data/seed/       Starter exercise library
domain/          Pure Kotlin logic: nutrition targets, macro thresholds,
                 muscle-coverage analysis, weight/calorie correlation
scanner/         CameraX + ML Kit barcode scanning
reminder/        WorkManager daily weight reminder + notifications
di/              Manual dependency container
ui/              Compose screens + ViewModels, one package per feature area
```

Everything is local-first (Room + DataStore-free — settings live in a
singleton Room row); network calls are barcode/name lookups against Open Food Facts
and optional photo estimation through the user's Gemini API key.

## Building

Requires Android Studio (or a `gradlew` invocation) with normal internet
access to Google's Maven (`dl.google.com`) and Maven Central, JDK 17, and an
Android SDK with API 34 installed.

```
./gradlew assembleDebug
```

The current workspace is verified with `compileDebugKotlin`, `lintDebug`, and
the JVM unit-test task. Run `assembleDebug` before distributing a release APK.

### Key versions pinned in `gradle/libs.versions.toml`

- Kotlin 2.0.20, AGP 8.4.2, Compose BOM 2024.09.02
- Room 2.6.1, Navigation Compose 2.7.7, WorkManager 2.9.0
- CameraX 1.3.4, ML Kit barcode-scanning 17.3.0
- Retrofit 2.11.0 + kotlinx.serialization

## Permissions

- `CAMERA` — requested contextually when you open the barcode scanner.
- `POST_NOTIFICATIONS` (Android 13+) — requested contextually, for weight and
  protein reminders.

## Notes / possible follow-ups

- Meal slots include the built-in meals and can be added, archived, and restored.
- The weight-vs-calorie correlation is a simple linear trend over a rolling
  28-day window; it's meant as a directional sanity check, not a precise
  metabolic model.
- No cloud sync/backup beyond Android's local `allowBackup` — everything
  lives in the on-device Room database.
