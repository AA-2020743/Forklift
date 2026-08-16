package com.caloriecalc.app.data.remote

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkClient {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * OFF's API guidelines require a User-Agent naming the app, its version and a contact URL,
     * and they actively block requests that don't identify themselves properly — a placeholder
     * contact is treated the same as none.
     */
    private class UserAgentInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .build()
            return chain.proceed(request)
        }
    }

    private const val USER_AGENT =
        "Forklift/1.0 (Android; +https://github.com/AA-2020743/CalorieCalc)"

    fun createOpenFoodFactsApi(): OpenFoodFactsApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor())
            .addInterceptor(loggingInterceptor)
            // OFF search can be slow under load; the defaults time out before it answers.
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(OpenFoodFactsApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(OpenFoodFactsApi::class.java)
    }
}
