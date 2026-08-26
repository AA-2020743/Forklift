package com.caloriecalc.app.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.caloriecalc.app.data.local.ApiKeyStore
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.local.entity.FoodSource
import com.caloriecalc.app.data.local.entity.Micronutrients
import com.caloriecalc.app.data.remote.EstimatedFoodItemDto
import com.caloriecalc.app.data.remote.GeminiApi
import com.caloriecalc.app.data.remote.GeminiContent
import com.caloriecalc.app.data.remote.GeminiGenerateContentRequest
import com.caloriecalc.app.data.remote.GeminiInlineData
import com.caloriecalc.app.data.remote.GeminiPart
import com.caloriecalc.app.data.remote.MealEstimateResponse
import java.io.ByteArrayOutputStream
import kotlinx.serialization.json.Json
import retrofit2.HttpException

private const val PROMPT = """
You are a nutrition estimation assistant. Look at the photo of a meal and identify each
distinct food item on it. For each item, estimate a realistic portion size in grams as eaten,
and its nutrition PER 100 GRAMS (not per portion).

Respond with ONLY valid JSON, no markdown formatting, matching exactly this shape:
{
  "items": [
    {
      "name": "string, short and specific, e.g. \"Grilled chicken breast\"",
      "estimatedGrams": number,
      "caloriesPer100g": number,
      "proteinPer100g": number,
      "fatPer100g": number,
      "carbsPer100g": number,
      "fiberPer100g": number or null,
      "sugarPer100g": number or null,
      "saturatedFatPer100g": number or null,
      "sodiumMgPer100g": number or null,
      "potassiumMgPer100g": number or null,
      "calciumMgPer100g": number or null,
      "ironMgPer100g": number or null,
      "vitaminCMgPer100g": number or null,
      "vitaminDMcgPer100g": number or null,
      "vitaminB12McgPer100g": number or null,
      "magnesiumMgPer100g": number or null,
      "zincMgPer100g": number or null
    }
  ]
}

If you can't identify any food, return {"items": []}. These are estimates for a personal
tracking app, not medical advice — give your best reasonable guess rather than refusing.
"""

class PhotoEstimationRepository(
    private val geminiApi: GeminiApi,
    private val apiKeyStore: ApiKeyStore
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun hasApiKey(): Boolean = apiKeyStore.getGeminiApiKey() != null

    suspend fun estimateMeal(bitmap: Bitmap): Result<List<EstimatedFoodItemDto>> {
        val apiKey = apiKeyStore.getGeminiApiKey()
            ?: return Result.failure(IllegalStateException("No Gemini API key set — add one in Profile settings."))

        return try {
            val request = GeminiGenerateContentRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = PROMPT),
                            GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = bitmap.toBase64Jpeg()))
                        )
                    )
                )
            )
            val response = geminiApi.generateContent(GeminiApi.DEFAULT_MODEL, apiKey, request)
            val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull { it.text != null }?.text
                ?: return Result.failure(IllegalStateException("Gemini returned no result — try again."))
            val parsed = json.decodeFromString<MealEstimateResponse>(text.trim())
            Result.success(parsed.items)
        } catch (e: HttpException) {
            val message = when (e.code()) {
                404 -> "Gemini model is unavailable. Update the app and try again."
                400, 401, 403 -> "Gemini rejected the request. Check your API key and Gemini API access."
                else -> "Gemini request failed (${e.code()}). Try again."
            }
            Result.failure(IllegalStateException(message, e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Bitmap.toBase64Jpeg(): String {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}

fun EstimatedFoodItemDto.toFoodItem(): FoodItem = FoodItem(
    name = name,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g,
    fatPer100g = fatPer100g,
    carbsPer100g = carbsPer100g,
    micronutrients = Micronutrients(
        fiberGrams = fiberPer100g,
        sugarGrams = sugarPer100g,
        saturatedFatGrams = saturatedFatPer100g,
        sodiumMg = sodiumMgPer100g,
        potassiumMg = potassiumMgPer100g,
        calciumMg = calciumMgPer100g,
        ironMg = ironMgPer100g,
        vitaminCMg = vitaminCMgPer100g,
        vitaminDMcg = vitaminDMcgPer100g,
        vitaminB12Mcg = vitaminB12McgPer100g,
        magnesiumMg = magnesiumMgPer100g,
        zincMg = zincMgPer100g
    ),
    source = FoodSource.AI_ESTIMATE
)
