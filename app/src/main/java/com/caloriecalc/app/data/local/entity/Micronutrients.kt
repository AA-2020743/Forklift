package com.caloriecalc.app.data.local.entity

/**
 * A bundle of commonly-tracked micronutrients. Every field is independently nullable since
 * source data (Open Food Facts, manual entry, AI photo estimates) rarely has all of them.
 *
 * Used in two contexts with the same shape: on [FoodItem] the values are per 100g; on
 * [MealEntry] they're an absolute snapshot for the grams actually logged (see [scaledBy]).
 */
data class Micronutrients(
    val fiberGrams: Double? = null,
    val sugarGrams: Double? = null,
    val saturatedFatGrams: Double? = null,
    val sodiumMg: Double? = null,
    val potassiumMg: Double? = null,
    val calciumMg: Double? = null,
    val ironMg: Double? = null,
    val vitaminCMg: Double? = null,
    val vitaminDMcg: Double? = null,
    val vitaminB12Mcg: Double? = null,
    val magnesiumMg: Double? = null,
    val zincMg: Double? = null
) {
    fun scaledBy(factor: Double): Micronutrients = Micronutrients(
        fiberGrams = fiberGrams?.times(factor),
        sugarGrams = sugarGrams?.times(factor),
        saturatedFatGrams = saturatedFatGrams?.times(factor),
        sodiumMg = sodiumMg?.times(factor),
        potassiumMg = potassiumMg?.times(factor),
        calciumMg = calciumMg?.times(factor),
        ironMg = ironMg?.times(factor),
        vitaminCMg = vitaminCMg?.times(factor),
        vitaminDMcg = vitaminDMcg?.times(factor),
        vitaminB12Mcg = vitaminB12Mcg?.times(factor),
        magnesiumMg = magnesiumMg?.times(factor),
        zincMg = zincMg?.times(factor)
    )
}
