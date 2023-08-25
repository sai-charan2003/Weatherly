package com.example.weatherly

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface apiinterface {
    @GET("forecast.json")
    fun getWeatherData(
        @Query("key") key:String,
        @Query("q") loc:String,
        @Query("aqi") aqi:String="no"
    ): Call<weatherdata>
}