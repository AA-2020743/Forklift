package com.caloriecalc.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    @SerialName("inline_data") val inlineData: GeminiInlineData? = null
)

@Serializable
data class GeminiInlineData(
    @SerialName("mime_type") val mimeType: String,
    val data: String
)

@Serializable
data class GeminiGenerationConfig(
    @SerialName("response_mime_type") val responseMimeType: String = "application/json"
)

@Serializable
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

/** The JSON payload we ask Gemini to return inside its response text (one entry per detected food). */
@Serializable
data class MealEstimateResponse(
    val items: List<EstimatedFoodItemDto> = emptyList()
)

@Serializable
data class EstimatedFoodItemDto(
    val name: String,
    val estimatedGrams: Double,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
    val fiberPer100g: Double? = null,
    val sugarPer100g: Double? = null,
    val addedSugarPer100g: Double? = null,
    val saturatedFatPer100g: Double? = null,
    val sodiumMgPer100g: Double? = null,
    val potassiumMgPer100g: Double? = null,
    val calciumMgPer100g: Double? = null,
    val ironMgPer100g: Double? = null,
    val vitaminCMgPer100g: Double? = null,
    val vitaminDMcgPer100g: Double? = null,
    val vitaminB12McgPer100g: Double? = null,
    val magnesiumMgPer100g: Double? = null,
    val zincMgPer100g: Double? = null
)
