package com.caloriecalc.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-managed meal slot (e.g. Breakfast, Pre-workout). Replaces a fixed enum so users
 * can add or remove meals. Removal archives rather than deletes the row, so historical
 * [MealEntry] rows logged under it stay intact and queryable.
 */
@Entity(tableName = "meal_slots")
data class MealSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sortOrder: Int,
    val isBuiltIn: Boolean = false,
    val isArchived: Boolean = false,
    /**
     * Wall-clock time this meal is usually eaten. New slots default to the time of day they
     * were created (you tend to add "Second lunch" while eating it) and are freely adjustable.
     * Used to anchor protein-spacing reminders; null means "no set time, don't remind for it".
     */
    val targetHour: Int? = null,
    val targetMinute: Int? = null,
    /** Whether a missed-meal nudge may fire for this slot. */
    val remindersEnabled: Boolean = true
) {
    val hasTargetTime: Boolean get() = targetHour != null && targetMinute != null

    /** Minutes since midnight, for ordering and gap maths. Null when no time is set. */
    val targetMinuteOfDay: Int?
        get() = targetHour?.let { h -> targetMinute?.let { m -> h * 60 + m } }
}
