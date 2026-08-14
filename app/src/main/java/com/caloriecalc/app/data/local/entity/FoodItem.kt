package com.caloriecalc.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A food/product, either scanned via barcode (Open Food Facts), found by search,
 * or entered manually. Macro fields are always stored per 100g so any serving
 * size can be derived from them.
 */
@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String? = null,
    val name: String,
    val brand: String? = null,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
    /** Per-100g micronutrients, when known. */
    @Embedded val micronutrients: Micronutrients = Micronutrients(),
    /** Grams represented by one "serving", if the product defines one (e.g. from Open Food Facts). */
    val servingSizeGrams: Double? = null,
    val servingName: String? = null,
    val source: FoodSource = FoodSource.MANUAL,
    val isFavorite: Boolean = false,
    val useCount: Int = 0,
    val lastUsedAtEpochMillis: Long? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

enum class FoodSource { BARCODE_SCAN, SEARCH, MANUAL, AI_ESTIMATE }
