package com.abdurakhmanov.ridez.data.source.remote

import com.abdurakhmanov.ridez.data.response.CurrentAddress
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocoderApiService {
    @GET("getAddress")
    suspend fun getAddress(
        @Query("apikey") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double?
    ): Response<CurrentAddress?>
}