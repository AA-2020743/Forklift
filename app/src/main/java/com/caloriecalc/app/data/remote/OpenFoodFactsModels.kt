package com.caloriecalc.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response shape of GET /api/v2/product/{barcode}.json */
@Serializable
data class ProductLookupResponse(
    @SerialName("status") val status: Int = 0,
    @SerialName("product") val product: ProductDto? = null
)

/** Response shape of GET /cgi/search.pl */
@Serializable
data class ProductSearchResponse(
    @SerialName("products") val products: List<ProductDto> = emptyList()
)

@Serializable
data class ProductDto(
    @SerialName("code") val code: String? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("brands") val brands: String? = null,
    @SerialName("serving_size") val servingSize: String? = null,
    @SerialName("serving_quantity") val servingQuantity: String? = null,
    @SerialName("nutriments") val nutriments: NutrimentsDto? = null
)

/**
 * Open Food Facts reports every `*_100g` nutriment field in grams (its documented convention),
 * including trace minerals/vitamins — [toFoodItem][com.caloriecalc.app.data.repository] converts
 * the tiny ones to mg/mcg for display.
 */
@Serializable
data class NutrimentsDto(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerialName("proteins_100g") val proteins100g: Double? = null,
    @SerialName("fat_100g") val fat100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydrates100g: Double? = null,
    @SerialName("fiber_100g") val fiber100g: Double? = null,
    @SerialName("sugars_100g") val sugars100g: Double? = null,
    @SerialName("saturated-fat_100g") val saturatedFat100g: Double? = null,
    @SerialName("sodium_100g") val sodium100g: Double? = null,
    @SerialName("potassium_100g") val potassium100g: Double? = null,
    @SerialName("calcium_100g") val calcium100g: Double? = null,
    @SerialName("iron_100g") val iron100g: Double? = null,
    @SerialName("vitamin-c_100g") val vitaminC100g: Double? = null,
    @SerialName("vitamin-d_100g") val vitaminD100g: Double? = null,
    @SerialName("vitamin-b12_100g") val vitaminB12100g: Double? = null,
    @SerialName("magnesium_100g") val magnesium100g: Double? = null,
    @SerialName("zinc_100g") val zinc100g: Double? = null
)
