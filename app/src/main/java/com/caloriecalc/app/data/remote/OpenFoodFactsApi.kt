package com.caloriecalc.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Open Food Facts (openfoodfacts.org) — free, open, no API key required.
 * See https://openfoodfacts.github.io/openfoodfacts-server/api/ for terms of use.
 */
interface OpenFoodFactsApi {

    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = "code,product_name,brands,serving_size,serving_quantity,nutriments"
    ): ProductLookupResponse

    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") searchTerms: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("fields") fields: String = "code,product_name,brands,serving_size,serving_quantity,nutriments"
    ): ProductSearchResponse

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.org/"
    }
}
