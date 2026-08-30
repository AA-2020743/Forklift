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
    /** Total sugars, including naturally occurring sugars; not used for daily-limit alerts. */
    val sugarGrams: Double? = null,
    /** Added/free sugars when the source distinguishes them from total sugars. */
    val addedSugarGrams: Double? = null,
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
        addedSugarGrams = addedSugarGrams?.times(factor),
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

    fun allNonNegative(): Boolean = listOf(
        fiberGrams, sugarGrams, addedSugarGrams, saturatedFatGrams, sodiumMg, potassiumMg, calciumMg,
        ironMg, vitaminCMg, vitaminDMcg, vitaminB12Mcg, magnesiumMg, zincMg
    ).all { it == null || (it.isFinite() && it >= 0.0) }

    /**
     * Combines two absolute (already-scaled) micronutrient snapshots, e.g. when totaling a
     * recipe's ingredients. A field stays null only when neither side reports it — an
     * ingredient that's simply silent on, say, iron shouldn't erase a known amount from
     * another ingredient.
     */
    operator fun plus(other: Micronutrients): Micronutrients = Micronutrients(
        fiberGrams = sumOrNull(fiberGrams, other.fiberGrams),
        sugarGrams = sumOrNull(sugarGrams, other.sugarGrams),
        addedSugarGrams = sumOrNull(addedSugarGrams, other.addedSugarGrams),
        saturatedFatGrams = sumOrNull(saturatedFatGrams, other.saturatedFatGrams),
        sodiumMg = sumOrNull(sodiumMg, other.sodiumMg),
        potassiumMg = sumOrNull(potassiumMg, other.potassiumMg),
        calciumMg = sumOrNull(calciumMg, other.calciumMg),
        ironMg = sumOrNull(ironMg, other.ironMg),
        vitaminCMg = sumOrNull(vitaminCMg, other.vitaminCMg),
        vitaminDMcg = sumOrNull(vitaminDMcg, other.vitaminDMcg),
        vitaminB12Mcg = sumOrNull(vitaminB12Mcg, other.vitaminB12Mcg),
        magnesiumMg = sumOrNull(magnesiumMg, other.magnesiumMg),
        zincMg = sumOrNull(zincMg, other.zincMg)
    )

    private fun sumOrNull(a: Double?, b: Double?): Double? =
        if (a == null && b == null) null else (a ?: 0.0) + (b ?: 0.0)
}

/**
 * EU labels generally report sugars without an added-sugar line. Until a better source is
 * available, treat that reported sugar value as added sugar while keeping total sugar inclusive.
 */
fun Micronutrients.withAddedSugarAssumedFromTotal(): Micronutrients {
    val assumedAdded = addedSugarGrams ?: sugarGrams
    val inclusiveTotal = when {
        sugarGrams == null -> assumedAdded
        assumedAdded == null -> sugarGrams
        else -> maxOf(sugarGrams, assumedAdded)
    }
    return copy(sugarGrams = inclusiveTotal, addedSugarGrams = assumedAdded)
}
