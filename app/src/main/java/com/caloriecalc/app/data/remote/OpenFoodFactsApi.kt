package com.caloriecalc.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Open Food Facts (openfoodfacts.org) — free, open, no API key required.
 * See https://openfoodfacts.github.io/openfoodfacts-server/api/ for terms of use.
 */
interface OpenFoodFactsApi {

    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = PRODUCT_FIELDS
    ): ProductLookupResponse

    /**
     * Search-a-licious, OFF's current search service. The legacy CGI endpoint is deprecated and
     * heavily rate-limited, so this is tried first; [searchProductsLegacy] remains as a fallback.
     * Absolute URL because it lives on a different host from the rest of the API.
     */
    @GET
    suspend fun searchProducts(
        @Url url: String = SEARCH_URL,
        @Query("q") query: String,
        @Query("page_size") pageSize: Int = 20,
        @Query("fields") fields: String = PRODUCT_FIELDS
    ): SearchALiciousResponse

    @GET("cgi/search.pl")
    suspend fun searchProductsLegacy(
        @Query("search_terms") searchTerms: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("fields") fields: String = PRODUCT_FIELDS
    ): ProductSearchResponse

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.org/"
        const val SEARCH_URL = "https://search.openfoodfacts.org/search"
        const val PRODUCT_FIELDS =
            "code,product_name,brands,serving_size,serving_quantity,nutriments"
    }
}
