# Forklift

Forklift is a local-first Android app that brings nutrition, hydration, activity,
lifting, and body-weight trends into one place. It combines detailed daily logging
with practical feedback: macro ranges instead of single-number targets,
muscle-coverage suggestions, protein-spacing reminders, and an estimate of actual
maintenance calories from weight and intake history.

## Screenshots

These Pixel emulator captures use deterministic demo data rather than real user data.

| Daily dashboard | Micronutrient detail |
| --- | --- |
| ![Forklift calorie and macro dashboard](https://raw.githubusercontent.com/AA-2020743/Forklift/main/docs/images/dashboard.png?v=20260818-demo) | ![Forklift micronutrient progress](https://raw.githubusercontent.com/AA-2020743/Forklift/main/docs/images/micronutrients.png?v=20260818-demo) |

| Meal overview | Shared meal time and entries |
| --- | --- |
| ![Forklift hydration and meal overview](https://raw.githubusercontent.com/AA-2020743/Forklift/main/docs/images/meals.png?v=20260818-demo) | ![Forklift shared meal time and food entries](https://raw.githubusercontent.com/AA-2020743/Forklift/main/docs/images/meal-log.png?v=20260818-demo) |

| Food logging options | Lifting coverage and history |
| --- | --- |
| ![Forklift recent foods and logging options](https://raw.githubusercontent.com/AA-2020743/Forklift/main/docs/images/add-food.png?v=20260818-demo) | ![Forklift muscle coverage suggestions](https://raw.githubusercontent.com/AA-2020743/Forklift/main/docs/images/lifting.png?v=20260818-demo) |

| Weight and maintenance insight | Profile and reminders |
| --- | --- |
| ![Forklift weight trend and maintenance estimate](https://raw.githubusercontent.com/AA-2020743/Forklift/main/docs/images/weight.png?v=20260818-demo) | ![Forklift profile reminders and AI settings](https://raw.githubusercontent.com/AA-2020743/Forklift/main/docs/images/profile.png?v=20260818-demo) |

## Features

### Daily nutrition dashboard

- Browse previous days or open the date picker to review and correct historical logs.
- See calories consumed, remaining, or over target in a daily progress ring.
- Track protein, fat, and carbohydrates against personalized minimum and maximum
  ranges; tap any macro for a ranked per-food contribution breakdown.
- Review fiber, sugar, saturated fat, sodium, potassium, calcium, iron, vitamins C,
  D, and B12, magnesium, and zinc against adult reference values.
- Open 7-, 14-, or 30-day views for calorie and macro averages, protein history,
  water intake, and logging completeness.

### Flexible food logging

- Search the on-device food library or explicitly query the crowd-sourced
  [Open Food Facts](https://openfoodfacts.org) catalog.
- Scan UPC/EAN barcodes with CameraX and ML Kit; cached products are reused before
  another network lookup.
- Browse recent, frequent, and favorite foods for fast repeat logging.
- Enter quantities in grams or servings with a live calorie and macro preview, then
  edit the amount or remove the entry later.
- Add foods manually with optional serving details, water percentage, and 12
  micronutrient fields. Recognized whole foods can start with editable reference
  estimates.
- Quick-add known calories and optional macros for restaurant meals or rough entries.
- Build reusable protein shakes from powder and liquid ingredients, or recipes from
  multiple saved foods with live batch and per-100 g totals.
- Optionally estimate a meal from a camera or gallery photo through Gemini, then
  review, edit, remove, and batch-log the detected foods before saving.

### Meals, templates, and timing

- Start with Breakfast, Pre-workout, Lunch, Post-workout, Dinner, and Snack; add
  custom meal slots, choose their typical times, archive them, or restore them.
- Keep one shared consumed-at time for every food in a meal/day and correct it from
  the meal screen when needed.
- Copy the previous day's equivalent meal into an empty slot.
- Save a logged meal as a named template and apply all of its foods and quantities
  to another meal in one action.
- Configure protein-spacing nudges using a qualifying dose, maximum gap, and
  wake/sleep window. Meal times provide context for the reminder.

### Hydration and activity

- Calculate a hydration target from body weight and activity level.
- Quick-log plain water in 250 ml, 500 ml, or 1 L increments and correct past days.
- See plain water separately from estimated hydration contributed by logged food and
  drinks.
- Log walking, running, hiking, cycling, swimming, boxing, sports, yoga, HIIT, or a
  custom activity with duration, optional steps, and estimated or manual calories.
- Include completed lifting sessions in the day's activity summary with duration and
  estimated energy use.

### Lifting and workout analysis

- Start a blank session or use a reusable workout template.
- Search and filter a seeded library of 57 exercises across 11 muscle groups, with
  fine-grained targets such as chest regions, deltoid heads, biceps/triceps heads,
  calves, core, and back regions.
- Record weight x reps sets, duplicate or delete sets, and compare against the most
  recent sets for the same exercise.
- Run 60-, 90-, or 120-second rest timers with add-time, skip, and vibration controls.
- Browse completed sessions and per-exercise history, including a heaviest-weight
  chart.
- Get conservative 14-day coverage suggestions for undertrained sub-muscles, with
  candidate exercises that can fill each gap.

### Weight, measurements, and feedback

- Log one weigh-in per day and edit or remove entries from the 28-day history.
- Compare raw scale readings with an exponentially smoothed trend and typical daily
  water-weight noise.
- View calorie intake beside weekly weight change and estimate actual maintenance
  calories from available food and weight logs.
- Receive goal-aware guidance and optionally apply the suggested 200 kcal/day target
  adjustment.
- Record waist, chest, arms, thighs, hips, and neck measurements and keep the latest
  summary alongside weight history.

### Personal targets, reminders, and privacy

- Configure weight, height, age, sex, activity level, and goals for loss,
  maintenance, gain, or recomposition.
- Preview targets instantly using Mifflin-St Jeor BMR, activity-adjusted TDEE,
  goal-based calories, editable protein/fat ranges, and residual carbohydrates.
- Override the calculated calorie target when following a separate plan.
- Schedule a daily weight reminder that stays quiet after a weigh-in, plus
  protein-spacing nudges that pause during the configured sleep window.
- Store the optional Gemini key with Android Keystore-backed encrypted preferences.
- Keep core profile, food, meal, hydration, workout, weight, and measurement data in
  Room so normal tracking remains available offline. No Forklift account is needed.
- Follow the device's light or dark theme automatically.

## Architecture

Forklift is a single-module Kotlin app with Jetpack Compose UI and a small,
hand-written `AppContainer` instead of a DI framework.

```text
data/local/      Room entities, DAOs, database, and migrations
data/remote/     Open Food Facts and Gemini Retrofit APIs
data/repository/ Repositories mediating local storage and network access
data/seed/       Starter meal slots and exercise library
domain/          Nutrition, hydration, coverage, and trend calculations
scanner/         CameraX and ML Kit barcode scanning
reminder/        WorkManager weight and protein notifications
di/              Manual dependency container
ui/              Compose screens and ViewModels grouped by feature
```

Network access is limited to Open Food Facts searches/barcode lookups and optional
Gemini photo analysis. Online foods selected by the user are cached locally for
later reuse. Room migrations preserve supported on-device data across upgrades.

## Building

Requirements:

- JDK 17.
- Android Studio or the Gradle wrapper.
- Android SDK 34.
- Internet access to Google's Maven and Maven Central for dependency resolution.

```bash
./gradlew assembleDebug
```

Key versions are pinned in `gradle/libs.versions.toml`:

- Kotlin 2.0.20, Android Gradle Plugin 8.4.2, Compose BOM 2024.09.02.
- Room 2.6.1, Navigation Compose 2.7.7, WorkManager 2.9.0.
- CameraX 1.3.4, ML Kit barcode scanning 17.3.0.
- Retrofit 2.11.0 and kotlinx.serialization.

## Permissions

- `CAMERA`: requested when scanning a barcode or taking a meal photo.
- `POST_NOTIFICATIONS` on Android 13+: requested for weight and protein reminders.
- `VIBRATE`: used for rest-timer feedback and notifications; no runtime prompt.

## Notes and limitations

- Nutrition ranges, hydration values, activity calories, and micronutrient references
  are estimates for guidance, not medical advice.
- Open Food Facts is crowd-sourced, so product coverage and accuracy vary by region.
- Gemini photo analysis requires internet and a user-supplied API key; selected images
  are sent to Google and the returned nutrition values should be reviewed.
- The maintenance-calorie model is a directional 28-day estimate and depends on
  consistent food and weight logging.
- Exercise calories are displayed but do not increase the daily calorie budget.
- The app currently uses metric units and has no app-managed cloud sync or export.
  Android-managed backup/restore may include the local database.
