package com.arjupta.weatherapi.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Api {
    private const val baseUrl = "https://api.openweathermap.org/data/2.5/"
    private val gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create()
    val retrofit: Retrofit = Retrofit.Builder().baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(gson)).build()
}