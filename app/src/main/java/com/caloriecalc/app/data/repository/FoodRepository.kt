package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.FoodDao
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.local.entity.FoodSource
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

    /** Looks up a scanned barcode locally first, then falls back to Open Food Facts. */
    suspend fun lookupBarcode(barcode: String): FoodItem? {
        foodDao.getByBarcode(barcode)?.let { return it }
        return runCatching { api.getProduct(barcode) }
            .getOrNull()
            ?.takeIf { it.status == 1 }
            ?.product
            ?.toFoodItem(FoodSource.BARCODE_SCAN)
            ?.let { food ->
                val id = foodDao.insert(food)
                food.copy(id = id)
            }
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
        servingName: String?
    ): FoodItem {
        val food = FoodItem(
            name = name,
            brand = brand,
            caloriesPer100g = caloriesPer100g,
            proteinPer100g = proteinPer100g,
            fatPer100g = fatPer100g,
            carbsPer100g = carbsPer100g,
            servingSizeGrams = servingSizeGrams,
            servingName = servingName,
            source = FoodSource.MANUAL
        )
        val id = foodDao.insert(food)
        return food.copy(id = id)
    }
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
        servingSizeGrams = servingQuantity?.toDoubleOrNull(),
        servingName = servingSize,
        source = source
    )
}
