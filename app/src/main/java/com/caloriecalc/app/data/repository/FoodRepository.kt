package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.FoodDao
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.local.entity.FoodSource
import com.caloriecalc.app.data.local.entity.Micronutrients
import com.caloriecalc.app.data.remote.NutrimentsDto
import com.caloriecalc.app.data.remote.OpenFoodFactsApi
import com.caloriecalc.app.data.remote.ProductDto
import com.caloriecalc.app.domain.HydrationCalculator
import com.caloriecalc.app.domain.MicronutrientEstimator
import kotlinx.coroutines.flow.Flow

/** Result of an online search — distinguishes "nothing matched" from "the search itself failed". */
sealed interface SearchOutcome {
    data class Success(val foods: List<FoodItem>) : SearchOutcome
    data class Failure(val message: String) : SearchOutcome
}

class FoodRepository(
    private val foodDao: FoodDao,
    private val api: OpenFoodFactsApi
) {
    fun searchLocal(query: String): Flow<List<FoodItem>> = foodDao.search(query)
    fun recentlyUsed(): Flow<List<FoodItem>> = foodDao.getRecentlyUsed()
    fun frequentlyUsed(): Flow<List<FoodItem>> = foodDao.getFrequentlyUsed()
    fun favorites(): Flow<List<FoodItem>> = foodDao.getFavorites()

    suspend fun getById(id: Long): FoodItem? = foodDao.getById(id)
    fun observeById(id: Long): Flow<FoodItem?> = foodDao.observeById(id)

    suspend fun setFavorite(id: Long, isFavorite: Boolean) = foodDao.setFavorite(id, isFavorite)
    /** Removes a food from add-food lists while preserving all historical meal entries that reference it. */
    suspend fun removeSavedFood(id: Long) = foodDao.archive(id)

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

    /**
     * Searches Open Food Facts by name.
     *
     * Tries Search-a-licious first (OFF's current search service) and falls back to the legacy
     * CGI endpoint, which still answers sometimes but is deprecated and aggressively
     * rate-limited. Failures are reported rather than swallowed: previously every error —
     * offline, HTTP 429, a malformed product — collapsed into an empty list, so a broken search
     * was indistinguishable from a genuine no-match.
     */
    suspend fun searchOnline(query: String): SearchOutcome {
        val modern = runCatching {
            api.searchProducts(query = query).hits.mapNotNull { it.toFoodItem(FoodSource.SEARCH) }
        }
        modern.getOrNull()?.let { if (it.isNotEmpty()) return SearchOutcome.Success(it) }

        val legacy = runCatching {
            api.searchProductsLegacy(query).products.mapNotNull { it.toFoodItem(FoodSource.SEARCH) }
        }
        legacy.getOrNull()?.let { return SearchOutcome.Success(it) }

        val error = legacy.exceptionOrNull() ?: modern.exceptionOrNull()
        return SearchOutcome.Failure(error.toUserMessage())
    }

    private fun Throwable?.toUserMessage(): String = when {
        this == null -> "Search failed for an unknown reason."
        this is java.net.UnknownHostException -> "No internet connection."
        this is java.net.SocketTimeoutException -> "Open Food Facts didn't respond in time — try again."
        this is retrofit2.HttpException && code() == 429 ->
            "Open Food Facts is rate-limiting requests right now — try again shortly."
        this is retrofit2.HttpException -> "Open Food Facts returned an error (HTTP ${code()})."
        else -> message ?: "Search failed."
    }

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
        micronutrients: Micronutrients = Micronutrients(),
        /** Null falls back to a keyword guess from the name, so drinks hydrate without extra typing. */
        waterContentPercent: Double? = null
    ): FoodItem {
        // Nothing supplied? Fall back to a reference profile for the name. Without this, every
        // hand-entered food contributes nothing and the Micronutrients screen reads all zeroes.
        val resolvedMicros = if (MicronutrientEstimator.isEmpty(micronutrients)) {
            MicronutrientEstimator.guessPer100g(name) ?: micronutrients
        } else {
            micronutrients
        }
        val food = FoodItem(
            name = name,
            brand = brand,
            caloriesPer100g = caloriesPer100g,
            proteinPer100g = proteinPer100g,
            fatPer100g = fatPer100g,
            carbsPer100g = carbsPer100g,
            micronutrients = resolvedMicros,
            waterContentPercent = waterContentPercent ?: HydrationCalculator.guessWaterContentPercent(name),
            servingSizeGrams = servingSizeGrams,
            servingName = servingName,
            source = FoodSource.MANUAL
        )
        validateFoodValues(food)
        val id = foodDao.insert(food)
        return food.copy(id = id)
    }

    suspend fun updateFood(food: FoodItem) {
        validateFoodValues(food)
        foodDao.update(food)
    }

    private fun validateFoodValues(food: FoodItem) {
        require(food.name.isNotBlank()) { "Food name must not be blank" }
        require(food.caloriesPer100g.isFinite() && food.caloriesPer100g >= 0.0)
        require(food.proteinPer100g.isFinite() && food.proteinPer100g >= 0.0)
        require(food.fatPer100g.isFinite() && food.fatPer100g >= 0.0)
        require(food.carbsPer100g.isFinite() && food.carbsPer100g >= 0.0)
        require(food.micronutrients.allNonNegative())
        require(food.waterContentPercent == null ||
            (food.waterContentPercent.isFinite() && food.waterContentPercent in 0.0..100.0))
        require(food.servingSizeGrams == null ||
            (food.servingSizeGrams.isFinite() && food.servingSizeGrams > 0.0))
    }
}

/**
 * Requires a name and a calorie figure; a product with neither can't be logged usefully.
 * Calories fall back to a kilojoule conversion, since kJ is the mandatory figure on EU labels
 * and plenty of products carry only that — requiring kcal outright was silently discarding a
 * large share of European search results.
 */
private fun ProductDto.toFoodItem(source: FoodSource): FoodItem? {
    val calories = nutriments?.resolvedKcal100g ?: return null
    val resolvedName = productName?.takeIf { it.isNotBlank() } ?: return null
    return FoodItem(
        barcode = code,
        name = resolvedName,
        brand = brands?.takeIf { it.isNotBlank() },
        waterContentPercent = HydrationCalculator.guessWaterContentPercent(resolvedName),
        caloriesPer100g = calories,
        proteinPer100g = nutriments.proteins100g ?: 0.0,
        fatPer100g = nutriments.fat100g ?: 0.0,
        carbsPer100g = nutriments.carbohydrates100g ?: 0.0,
        micronutrients = nutriments.toMicronutrients(),
        servingSizeGrams = servingQuantity?.takeIf { it > 0 },
        servingName = servingSize?.takeIf { it.isNotBlank() },
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
