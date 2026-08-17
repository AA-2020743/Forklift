package com.caloriecalc.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** The consumed-at time shared by all entries in one meal on one day. */
@Entity(
    tableName = "meal_times",
    primaryKeys = ["epochDay", "mealSlotId"],
    foreignKeys = [
        ForeignKey(
            entity = MealSlot::class,
            parentColumns = ["id"],
            childColumns = ["mealSlotId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("mealSlotId")]
)
data class MealTime(
    val epochDay: Long,
    val mealSlotId: Long,
    val consumedAtEpochMillis: Long
)
