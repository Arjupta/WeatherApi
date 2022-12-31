package com.arjupta.weatherapi.domain.model

data class WeatherData(
    val lat: Double,
    val lon: Double,
    val date: String,
    val status: String,
    val temperature: Int,
    val feels: Int,
    val tempMin: Int,
    val tempMax: Int
)
