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

@Serializable
data class NutrimentsDto(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerialName("proteins_100g") val proteins100g: Double? = null,
    @SerialName("fat_100g") val fat100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydrates100g: Double? = null
)
