package com.caloriecalc.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Real, hand-written migrations for every schema change shipped so far. Column order never
 * matters for Room's schema validation (it compares column/FK/index sets, not CREATE TABLE
 * text), so every statement below spells out explicit column lists rather than relying on
 * position. Table recreation (copy into a new table, drop the old one, rename) is used
 * wherever SQLite can't do the change in place (column rename/removal, type change); plain
 * `ALTER TABLE ... ADD COLUMN` is used for pure additions, which works on every Android
 * version this app supports (minSdk 26) without relying on SQLite's newer RENAME COLUMN.
 */

/**
 * v1 -> v2: meal logging moves from a fixed [com.caloriecalc.app.domain.MealType] enum column
 * to a user-manageable `meal_slots` table (see MealSlot.kt), and protein target moves from a
 * single g/kg number to a min/max range.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `meal_slots` (" +
                "`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, " +
                "`isBuiltIn` INTEGER NOT NULL, `isArchived` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        // Seed the same six built-in slots a fresh install gets (MealSlotSeedData.defaults),
        // so migrated users end up with identical meal names/order to a new install.
        db.execSQL("INSERT INTO meal_slots (name, sortOrder, isBuiltIn, isArchived) VALUES ('Breakfast', 0, 1, 0)")
        db.execSQL("INSERT INTO meal_slots (name, sortOrder, isBuiltIn, isArchived) VALUES ('Pre-workout', 1, 1, 0)")
        db.execSQL("INSERT INTO meal_slots (name, sortOrder, isBuiltIn, isArchived) VALUES ('Lunch', 2, 1, 0)")
        db.execSQL("INSERT INTO meal_slots (name, sortOrder, isBuiltIn, isArchived) VALUES ('Post-workout', 3, 1, 0)")
        db.execSQL("INSERT INTO meal_slots (name, sortOrder, isBuiltIn, isArchived) VALUES ('Dinner', 4, 1, 0)")
        db.execSQL("INSERT INTO meal_slots (name, sortOrder, isBuiltIn, isArchived) VALUES ('Snack', 5, 1, 0)")

        db.execSQL(
            "CREATE TABLE `meal_entries_new` (" +
                "`id` INTEGER NOT NULL, `foodItemId` INTEGER NOT NULL, `mealSlotId` INTEGER NOT NULL, " +
                "`epochDay` INTEGER NOT NULL, `loggedAtEpochMillis` INTEGER NOT NULL, `grams` REAL NOT NULL, " +
                "`calories` REAL NOT NULL, `protein` REAL NOT NULL, `fat` REAL NOT NULL, `carbs` REAL NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`foodItemId`) REFERENCES `food_items`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, " +
                "FOREIGN KEY(`mealSlotId`) REFERENCES `meal_slots`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)"
        )
        db.execSQL(
            "INSERT INTO meal_entries_new (id, foodItemId, mealSlotId, epochDay, loggedAtEpochMillis, " +
                "grams, calories, protein, fat, carbs) " +
                "SELECT me.id, me.foodItemId, ms.id, me.epochDay, me.loggedAtEpochMillis, " +
                "me.grams, me.calories, me.protein, me.fat, me.carbs " +
                "FROM meal_entries me JOIN meal_slots ms ON ms.name = CASE me.mealType " +
                "WHEN 'BREAKFAST' THEN 'Breakfast' " +
                "WHEN 'LUNCH' THEN 'Lunch' " +
                "WHEN 'DINNER' THEN 'Dinner' " +
                "WHEN 'SNACK' THEN 'Snack' " +
                "WHEN 'PRE_WORKOUT' THEN 'Pre-workout' " +
                "WHEN 'POST_WORKOUT' THEN 'Post-workout' END"
        )
        db.execSQL("DROP TABLE meal_entries")
        db.execSQL("ALTER TABLE meal_entries_new RENAME TO meal_entries")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_foodItemId` ON `meal_entries` (`foodItemId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_epochDay` ON `meal_entries` (`epochDay`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_entries_mealSlotId` ON `meal_entries` (`mealSlotId`)")

        db.execSQL(
            "CREATE TABLE `user_profile_new` (" +
                "`id` INTEGER NOT NULL, `bodyWeightKg` REAL NOT NULL, `heightCm` REAL NOT NULL, " +
                "`age` INTEGER NOT NULL, `sex` TEXT NOT NULL, `activityLevel` TEXT NOT NULL, `goal` TEXT NOT NULL, " +
                "`proteinMinGramsPerKg` REAL NOT NULL, `proteinMaxGramsPerKg` REAL NOT NULL, " +
                "`fatPercentMin` REAL NOT NULL, `fatPercentMax` REAL NOT NULL, `manualCalorieTarget` INTEGER, " +
                "`weightReminderEnabled` INTEGER NOT NULL, `weightReminderHour` INTEGER NOT NULL, " +
                "`weightReminderMinute` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        // Old single proteinGramsPerKg becomes the range floor; ceiling is at least the app's
        // new default (2.4) so an existing custom value never collapses into an inverted range.
        db.execSQL(
            "INSERT INTO user_profile_new (id, bodyWeightKg, heightCm, age, sex, activityLevel, goal, " +
                "proteinMinGramsPerKg, proteinMaxGramsPerKg, fatPercentMin, fatPercentMax, " +
                "manualCalorieTarget, weightReminderEnabled, weightReminderHour, weightReminderMinute) " +
                "SELECT id, bodyWeightKg, heightCm, age, sex, activityLevel, goal, " +
                "proteinGramsPerKg, MAX(proteinGramsPerKg, 2.4), fatPercentMin, fatPercentMax, " +
                "manualCalorieTarget, weightReminderEnabled, weightReminderHour, weightReminderMinute " +
                "FROM user_profile"
        )
        db.execSQL("DROP TABLE user_profile")
        db.execSQL("ALTER TABLE user_profile_new RENAME TO user_profile")
    }
}

/**
 * v2 -> v3: adds micronutrient columns to food_items/meal_entries, workout templates, and
 * body measurements. Every new column here is nullable, so plain ADD COLUMN is safe.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        for (table in listOf("food_items", "meal_entries")) {
            db.execSQL("ALTER TABLE $table ADD COLUMN `fiberGrams` REAL")
            db.execSQL("ALTER TABLE $table ADD COLUMN `sugarGrams` REAL")
            db.execSQL("ALTER TABLE $table ADD COLUMN `saturatedFatGrams` REAL")
            db.execSQL("ALTER TABLE $table ADD COLUMN `sodiumMg` REAL")
            db.execSQL("ALTER TABLE $table ADD COLUMN `potassiumMg` REAL")
            db.execSQL("ALTER TABLE $table ADD COLUMN `calciumMg` REAL")
            db.execSQL("ALTER TABLE $table ADD COLUMN `ironMg` REAL")
            db.execSQL("ALTER TABLE $table ADD COLUMN `vitaminCMg` REAL")
            db.execSQL("ALTER TABLE $table ADD COLUMN `vitaminDMcg` REAL")
            db.execSQL("ALTER TABLE $table ADD COLUMN `vitaminB12Mcg` REAL")
            db.execSQL("ALTER TABLE $table ADD COLUMN `magnesiumMg` REAL")
            db.execSQL("ALTER TABLE $table ADD COLUMN `zincMg` REAL")
        }

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `workout_templates` (" +
                "`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `workout_template_exercises` (" +
                "`id` INTEGER NOT NULL, `templateId` INTEGER NOT NULL, `exerciseId` INTEGER NOT NULL, " +
                "`orderIndex` INTEGER NOT NULL, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`templateId`) REFERENCES `workout_templates`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_workout_template_exercises_templateId` " +
                "ON `workout_template_exercises` (`templateId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_workout_template_exercises_exerciseId` " +
                "ON `workout_template_exercises` (`exerciseId`)"
        )

        db.execSQL(
            "ALTER TABLE workout_sessions ADD COLUMN `templateId` INTEGER " +
                "REFERENCES `workout_templates`(`id`) ON DELETE SET NULL"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_templateId` ON `workout_sessions` (`templateId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `body_measurements` (" +
                "`id` INTEGER NOT NULL, `epochDay` INTEGER NOT NULL, `waistCm` REAL, `chestCm` REAL, " +
                "`armsCm` REAL, `thighsCm` REAL, `hipsCm` REAL, `neckCm` REAL, " +
                "`loggedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_body_measurements_epochDay` ON `body_measurements` (`epochDay`)"
        )
    }
}

/**
 * v3 -> v4: fat target moves from %-of-calories to g/kg bodyweight. The two units aren't
 * losslessly convertible without re-running the app's BMR/TDEE calculation, so this resets
 * the fat range to the new app defaults (0.6-1.0 g/kg) while preserving every other profile
 * field exactly. Users who'd customized their fat % can re-adjust it once in Profile.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE `user_profile_new` (" +
                "`id` INTEGER NOT NULL, `bodyWeightKg` REAL NOT NULL, `heightCm` REAL NOT NULL, " +
                "`age` INTEGER NOT NULL, `sex` TEXT NOT NULL, `activityLevel` TEXT NOT NULL, `goal` TEXT NOT NULL, " +
                "`proteinMinGramsPerKg` REAL NOT NULL, `proteinMaxGramsPerKg` REAL NOT NULL, " +
                "`fatMinGramsPerKg` REAL NOT NULL, `fatMaxGramsPerKg` REAL NOT NULL, `manualCalorieTarget` INTEGER, " +
                "`weightReminderEnabled` INTEGER NOT NULL, `weightReminderHour` INTEGER NOT NULL, " +
                "`weightReminderMinute` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "INSERT INTO user_profile_new (id, bodyWeightKg, heightCm, age, sex, activityLevel, goal, " +
                "proteinMinGramsPerKg, proteinMaxGramsPerKg, fatMinGramsPerKg, fatMaxGramsPerKg, " +
                "manualCalorieTarget, weightReminderEnabled, weightReminderHour, weightReminderMinute) " +
                "SELECT id, bodyWeightKg, heightCm, age, sex, activityLevel, goal, " +
                "proteinMinGramsPerKg, proteinMaxGramsPerKg, 0.6, 1.0, " +
                "manualCalorieTarget, weightReminderEnabled, weightReminderHour, weightReminderMinute " +
                "FROM user_profile"
        )
        db.execSQL("DROP TABLE user_profile")
        db.execSQL("ALTER TABLE user_profile_new RENAME TO user_profile")
    }
}

/**
 * v4 -> v5: adds day-activity tracking. `endedAtEpochMillis` on workout_sessions is a plain
 * nullable addition (safe ADD COLUMN); `activity_logs` is a brand new table.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE workout_sessions ADD COLUMN `endedAtEpochMillis` INTEGER")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `activity_logs` (" +
                "`id` INTEGER NOT NULL, `epochDay` INTEGER NOT NULL, `type` TEXT NOT NULL, " +
                "`durationMinutes` INTEGER NOT NULL, `steps` INTEGER, `caloriesBurned` INTEGER NOT NULL, " +
                "`loggedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_logs_epochDay` ON `activity_logs` (`epochDay`)")
    }
}

/** v5 -> v6: adds daily water tracking, a brand new table. */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `water_logs` (" +
                "`id` INTEGER NOT NULL, `epochDay` INTEGER NOT NULL, `amountMl` INTEGER NOT NULL, " +
                "`updatedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_water_logs_epochDay` ON `water_logs` (`epochDay`)")
    }
}

/** v6 -> v7: adds reusable meal templates ("my usual breakfast"), two brand new tables. */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `meal_templates` (" +
                "`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `meal_template_items` (" +
                "`id` INTEGER NOT NULL, `templateId` INTEGER NOT NULL, `foodItemId` INTEGER NOT NULL, " +
                "`grams` REAL NOT NULL, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`templateId`) REFERENCES `meal_templates`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`foodItemId`) REFERENCES `food_items`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_template_items_templateId` ON `meal_template_items` (`templateId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_template_items_foodItemId` ON `meal_template_items` (`foodItemId`)")
    }
}

/**
 * v7 -> v8: hydration from food/drink, meal target times, and protein-spacing/sleep settings.
 * All additive. Existing meal slots get sensible default times by name so protein-gap reminders
 * work immediately rather than waiting for the user to set each one; unrecognised names stay
 * null (= no set time, no reminder) instead of being given a made-up one.
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE food_items ADD COLUMN `waterContentPercent` REAL")
        db.execSQL("ALTER TABLE meal_entries ADD COLUMN `hydrationMl` REAL NOT NULL DEFAULT 0")

        db.execSQL("ALTER TABLE meal_slots ADD COLUMN `targetHour` INTEGER")
        db.execSQL("ALTER TABLE meal_slots ADD COLUMN `targetMinute` INTEGER")
        db.execSQL("ALTER TABLE meal_slots ADD COLUMN `remindersEnabled` INTEGER NOT NULL DEFAULT 1")
        val defaultTimes = mapOf(
            "Breakfast" to 8,
            "Pre-workout" to 11,
            "Lunch" to 13,
            "Post-workout" to 16,
            "Dinner" to 19,
            "Snack" to 21
        )
        defaultTimes.forEach { (name, hour) ->
            db.execSQL("UPDATE meal_slots SET targetHour = $hour, targetMinute = 0 WHERE name = '$name'")
        }

        db.execSQL("ALTER TABLE user_profile ADD COLUMN `proteinReminderEnabled` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN `proteinGapHours` INTEGER NOT NULL DEFAULT 4")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN `proteinDoseGrams` REAL NOT NULL DEFAULT 15.0")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN `wakeHour` INTEGER NOT NULL DEFAULT 7")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN `wakeMinute` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN `sleepHour` INTEGER NOT NULL DEFAULT 23")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN `sleepMinute` INTEGER NOT NULL DEFAULT 0")
    }
}

val ALL_MIGRATIONS: Array<Migration> = arrayOf(
    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8
)
