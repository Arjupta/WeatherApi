package com.arjupta.weatherapi.network

import com.arjupta.weatherapi.network.pojo.RawWeatherData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    companion object{
        val client: WeatherService = Api.retrofit.create(WeatherService::class.java)
    }
    @GET("weather/")
    fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String = "096a672a5fa690552ce584d14bea9999"
    ): Call<RawWeatherData?>

    @GET("weather/")
    suspend fun getWeatherData(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String = "096a672a5fa690552ce584d14bea9999"
    ): retrofit2.Response<RawWeatherData>
}