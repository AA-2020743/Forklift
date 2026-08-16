package com.caloriecalc.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response shape of GET /api/v2/product/{barcode}.json */
@Serializable
data class ProductLookupResponse(
    @SerialName("status") val status: Int = 0,
    @SerialName("product") val product: ProductDto? = null
)

/** Response shape of the legacy GET /cgi/search.pl */
@Serializable
data class ProductSearchResponse(
    @SerialName("products") val products: List<ProductDto> = emptyList()
)

/**
 * Response shape of Search-a-licious (search.openfoodfacts.org), OFF's supported replacement
 * for the legacy CGI search. Same product objects, returned under `hits`.
 */
@Serializable
data class SearchALiciousResponse(
    @SerialName("hits") val hits: List<ProductDto> = emptyList(),
    @SerialName("count") val count: Int = 0
)

@Serializable
data class ProductDto(
    @SerialName("code") val code: String? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("brands") val brands: String? = null,
    @SerialName("serving_size") val servingSize: String? = null,
    // Frequently a bare number in the JSON despite reading like text ("30", 30, "30 g").
    @SerialName("serving_quantity")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val servingQuantity: Double? = null,
    @SerialName("nutriments") val nutriments: NutrimentsDto? = null
)

/**
 * Open Food Facts reports every `*_100g` nutriment field in grams (its documented convention),
 * including trace minerals/vitamins — [toFoodItem][com.caloriecalc.app.data.repository] converts
 * the tiny ones to mg/mcg for display.
 *
 * Every field goes through [FlexibleDoubleSerializer]: contributors enter these by hand and the
 * same field genuinely arrives as a number or a string depending on the product.
 */
@Serializable
data class NutrimentsDto(
    @SerialName("energy-kcal_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val energyKcal100g: Double? = null,
    /** Kilojoules, used to derive kcal when the kcal field itself is missing. */
    @SerialName("energy_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val energy100g: Double? = null,
    @SerialName("energy-kj_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val energyKj100g: Double? = null,
    @SerialName("proteins_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val proteins100g: Double? = null,
    @SerialName("fat_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val fat100g: Double? = null,
    @SerialName("carbohydrates_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val carbohydrates100g: Double? = null,
    @SerialName("fiber_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val fiber100g: Double? = null,
    @SerialName("sugars_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val sugars100g: Double? = null,
    @SerialName("saturated-fat_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val saturatedFat100g: Double? = null,
    @SerialName("sodium_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val sodium100g: Double? = null,
    @SerialName("potassium_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val potassium100g: Double? = null,
    @SerialName("calcium_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val calcium100g: Double? = null,
    @SerialName("iron_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val iron100g: Double? = null,
    @SerialName("vitamin-c_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val vitaminC100g: Double? = null,
    @SerialName("vitamin-d_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val vitaminD100g: Double? = null,
    @SerialName("vitamin-b12_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val vitaminB12100g: Double? = null,
    @SerialName("magnesium_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val magnesium100g: Double? = null,
    @SerialName("zinc_100g")
    @Serializable(with = FlexibleDoubleSerializer::class)
    val zinc100g: Double? = null
) {
    /**
     * Calories per 100g, falling back to a kilojoule conversion. Plenty of products carry only
     * kJ (it's the mandatory figure on EU labels); dropping those was quietly discarding a large
     * share of European search results.
     */
    val resolvedKcal100g: Double?
        get() = energyKcal100g ?: (energyKj100g ?: energy100g)?.let { it / 4.184 }
}
