# CalorieCalc

An Android app for tracking calories/macros and lifting sessions together, with
visual thresholds for protein/fat, muscle-coverage exercise suggestions, and a
weight-vs-intake correlation view.

## Features

- **Barcode scanning** (CameraX + ML Kit) looked up against the free
  [Open Food Facts](https://openfoodfacts.org) database — no API key required.
- **Recent / frequent / favorite foods** remembered locally so repeat logging is
  one tap, plus manual entry and text search (local + online).
- **Grams or servings** quantity entry, per food item.
- **Meals**: Breakfast, Lunch, Dinner, Snack, Pre-workout, Post-workout.
- **Macro thresholds**: protein target defaults to 2.3 g/kg body weight; fat has
  a configurable safe floor/ceiling (default 20-35% of calories); carbs fill
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
singleton Room row); the only network calls are barcode/name lookups against
Open Food Facts.

## Building

Requires Android Studio (or a `gradlew` invocation) with normal internet
access to Google's Maven (`dl.google.com`) and Maven Central, JDK 17, and an
Android SDK with API 34 installed.

```
./gradlew assembleDebug
```

> **Note:** this project was generated in a sandboxed environment whose
> network policy blocks `dl.google.com`, so the Android Gradle Plugin and
> AndroidX/Google Maven artifacts could not be resolved there and the build
> could not be run or verified end-to-end in that sandbox. The code was
> written carefully and reviewed by hand, but you should do a normal build in
> Android Studio (which will surface real compiler diagnostics) before relying
> on it — treat the first build as a review step, not a formality.

### Key versions pinned in `gradle/libs.versions.toml`

- Kotlin 1.9.24, AGP 8.4.2, Compose BOM 2024.06.00
- Room 2.6.1, Navigation Compose 2.7.7, WorkManager 2.9.0
- CameraX 1.3.4, ML Kit barcode-scanning 17.3.0
- Retrofit 2.11.0 + kotlinx.serialization

## Permissions

- `CAMERA` — requested contextually when you open the barcode scanner.
- `POST_NOTIFICATIONS` (Android 13+) — requested at first launch, for the
  weight reminder.

## Notes / possible follow-ups

- Meal slots are a fixed enum (Breakfast/Lunch/Dinner/Snack/Pre-/Post-workout)
  rather than user-customizable — straightforward to extend if needed.
- The weight-vs-calorie correlation is a simple linear trend over a rolling
  28-day window; it's meant as a directional sanity check, not a precise
  metabolic model.
- No cloud sync/backup beyond Android's local `allowBackup` — everything
  lives in the on-device Room database.
