package com.example.data.model

import com.squareup.moshi.Json

data class OpenMeteoResponse(
    @Json(name = "hourly") val hourly: OpenMeteoHourly? = null
)

data class OpenMeteoHourly(
    @Json(name = "time") val time: List<String> = emptyList(),
    @Json(name = "temperature_2m") val temperature2m: List<Double> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @Json(name = "is_day") val isDay: List<Int> = emptyList()
)

enum class WeatherCondition(val labelTr: String) {
    CLEAR_DAY("Açık"),
    CLEAR_NIGHT("Açık"),
    PARTLY_CLOUDY("Parçalı Bulutlu"),
    CLOUDY("Bulutlu"),
    FOGGY("Sisli"),
    RAINY("Yağmurlu"),
    SNOWY("Karlı"),
    THUNDERSTORM("Fırtınalı")
}

data class PrayerWeatherInfo(
    val condition: WeatherCondition,
    val temperatureCelsius: Int,
    val isDay: Boolean
)
