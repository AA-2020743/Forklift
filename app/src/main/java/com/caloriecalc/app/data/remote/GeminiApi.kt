package com.caloriecalc.app.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Google Gemini API (generativelanguage.googleapis.com) — the user supplies their own free-tier
 * API key from Google AI Studio; see https://ai.google.dev/gemini-api/docs.
 */
interface GeminiApi {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateContentRequest
    ): GeminiGenerateContentResponse

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/"
        // 2.0 Flash is no longer available for new API requests and returns HTTP 404.
        const val DEFAULT_MODEL = "gemini-2.5-flash"
    }
}
