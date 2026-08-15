package com.caloriecalc.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A named, reusable set of foods+grams a user saved from a logged meal — "my usual breakfast". */
@Entity(tableName = "meal_templates")
data class MealTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

/**
 * One ingredient of a [MealTemplate]. Deliberately stores only foodItemId + grams, not a
 * macro snapshot — applying a template recomputes macros from the food's current per-100g
 * values, so editing a saved food (e.g. correcting its calories) is reflected the next time
 * any template using it is applied.
 */
@Entity(
    tableName = "meal_template_items",
    foreignKeys = [
        ForeignKey(
            entity = MealTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FoodItem::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("templateId"), Index("foodItemId")]
)
data class MealTemplateItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val templateId: Long,
    val foodItemId: Long,
    val grams: Double
)
