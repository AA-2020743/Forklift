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
    val isArchived: Boolean = false
)
