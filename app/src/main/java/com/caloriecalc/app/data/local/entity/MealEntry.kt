package com.caloriecalc.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.caloriecalc.app.domain.MealType

/**
 * A logged instance of a food eaten at a given quantity, under a given meal slot and day.
 * Macro values are snapshotted at log time so edits to [FoodItem] later don't rewrite history.
 */
@Entity(
    tableName = "meal_entries",
    foreignKeys = [
        ForeignKey(
            entity = FoodItem::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("foodItemId"), Index("epochDay"), Index("mealType")]
)
data class MealEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val foodItemId: Long,
    val mealType: MealType,
    /** Day this entry belongs to, as an epoch-day (days since 1970-01-01), independent of time zone drift. */
    val epochDay: Long,
    val loggedAtEpochMillis: Long,
    val grams: Double,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)
