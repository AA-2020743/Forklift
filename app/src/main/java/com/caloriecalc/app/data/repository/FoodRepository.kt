package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.FoodDao
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.local.entity.FoodSource
import com.caloriecalc.app.data.local.entity.Micronutrients
import com.caloriecalc.app.data.remote.NutrimentsDto
import com.caloriecalc.app.data.remote.OpenFoodFactsApi
import com.caloriecalc.app.data.remote.ProductDto
import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val foodDao: FoodDao,
    private val api: OpenFoodFactsApi
) {
    fun searchLocal(query: String): Flow<List<FoodItem>> = foodDao.search(query)
    fun recentlyUsed(): Flow<List<FoodItem>> = foodDao.getRecentlyUsed()
    fun frequentlyUsed(): Flow<List<FoodItem>> = foodDao.getFrequentlyUsed()
    fun favorites(): Flow<List<FoodItem>> = foodDao.getFavorites()

    suspend fun getById(id: Long): FoodItem? = foodDao.getById(id)

    suspend fun setFavorite(id: Long, isFavorite: Boolean) = foodDao.setFavorite(id, isFavorite)

    /**
     * Looks up a scanned barcode locally first, then falls back to Open Food Facts.
     *
     * Tries a couple of equivalent barcode encodings — scanners sometimes return a 12-digit
     * UPC-A code for a product Open Food Facts only has under its 13-digit EAN-13 form (a
     * leading zero), or vice versa — before giving up. Coverage still varies a lot by region
     * since it's a crowdsourced database; a genuine miss just means the product hasn't been
     * contributed yet, and manual entry (remembered from then on) is the fallback.
     */
    suspend fun lookupBarcode(barcode: String): FoodItem? {
        val variants = barcodeVariants(barcode)

        for (code in variants) {
            foodDao.getByBarcode(code)?.let { return it }
        }

        for (code in variants) {
            val food = runCatching { api.getProduct(code) }
                .getOrNull()
                ?.takeIf { it.status == 1 }
                ?.product
                ?.toFoodItem(FoodSource.BARCODE_SCAN)
            if (food != null) {
                val id = foodDao.insert(food)
                return food.copy(id = id)
            }
        }
        return null
    }

    private fun barcodeVariants(barcode: String): List<String> {
        val trimmed = barcode.trim()
        val variants = linkedSetOf(trimmed)
        if (trimmed.length == 12) variants.add("0$trimmed")
        if (trimmed.length == 13 && trimmed.startsWith("0")) variants.add(trimmed.substring(1))
        return variants.toList()
    }

    suspend fun searchOnline(query: String): List<FoodItem> =
        runCatching { api.searchProducts(query) }
            .getOrNull()
            ?.products
            ?.mapNotNull { it.toFoodItem(FoodSource.SEARCH) }
            .orEmpty()

    /** Persists a food (from an online search result or manual entry) and marks it used. */
    suspend fun saveAndUse(food: FoodItem): FoodItem {
        val existing = food.barcode?.takeIf { it.isNotBlank() }?.let { foodDao.getByBarcode(it) }
        val id = existing?.id ?: foodDao.insert(food)
        foodDao.markUsed(id, System.currentTimeMillis())
        return (existing ?: food).copy(id = id)
    }

    suspend fun markUsed(id: Long) = foodDao.markUsed(id, System.currentTimeMillis())

    suspend fun createManualFood(
        name: String,
        brand: String?,
        caloriesPer100g: Double,
        proteinPer100g: Double,
        fatPer100g: Double,
        carbsPer100g: Double,
        servingSizeGrams: Double?,
        servingName: String?,
        micronutrients: Micronutrients = Micronutrients()
    ): FoodItem {
        val food = FoodItem(
            name = name,
            brand = brand,
            caloriesPer100g = caloriesPer100g,
            proteinPer100g = proteinPer100g,
            fatPer100g = fatPer100g,
            carbsPer100g = carbsPer100g,
            micronutrients = micronutrients,
            servingSizeGrams = servingSizeGrams,
            servingName = servingName,
            source = FoodSource.MANUAL
        )
        val id = foodDao.insert(food)
        return food.copy(id = id)
    }

    suspend fun updateFood(food: FoodItem) = foodDao.update(food)
}

private fun ProductDto.toFoodItem(source: FoodSource): FoodItem? {
    val calories = nutriments?.energyKcal100g ?: return null
    return FoodItem(
        barcode = code,
        name = productName?.takeIf { it.isNotBlank() } ?: "Unnamed product",
        brand = brands,
        caloriesPer100g = calories,
        proteinPer100g = nutriments.proteins100g ?: 0.0,
        fatPer100g = nutriments.fat100g ?: 0.0,
        carbsPer100g = nutriments.carbohydrates100g ?: 0.0,
        micronutrients = nutriments.toMicronutrients(),
        servingSizeGrams = servingQuantity?.toDoubleOrNull(),
        servingName = servingSize,
        source = source
    )
}

/** Open Food Facts reports every nutriment in grams; convert the trace ones to mg/mcg. */
private fun NutrimentsDto.toMicronutrients(): Micronutrients = Micronutrients(
    fiberGrams = fiber100g,
    sugarGrams = sugars100g,
    saturatedFatGrams = saturatedFat100g,
    sodiumMg = sodium100g?.times(1000),
    potassiumMg = potassium100g?.times(1000),
    calciumMg = calcium100g?.times(1000),
    ironMg = iron100g?.times(1000),
    vitaminCMg = vitaminC100g?.times(1000),
    vitaminDMcg = vitaminD100g?.times(1_000_000),
    vitaminB12Mcg = vitaminB12100g?.times(1_000_000),
    magnesiumMg = magnesium100g?.times(1000),
    zincMg = zinc100g?.times(1000)
)
