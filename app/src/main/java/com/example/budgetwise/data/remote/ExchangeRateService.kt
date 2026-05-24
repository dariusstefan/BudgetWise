package com.example.budgetwise.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateService {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("base") base: String = "EUR"
    ): ExchangeRateResponse
}
