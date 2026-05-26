package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    @Json(name = "current") val current: CurrentWeather?,
    @Json(name = "hourly") val hourly: HourlyForecast?,
    @Json(name = "daily") val daily: DailyForecast?
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    @Json(name = "temperature_2m") val temperature: Double,
    @Json(name = "relative_humidity_2m") val humidity: Double,
    @Json(name = "apparent_temperature") val apparentTemperature: Double,
    val precipitation: Double,
    val rain: Double,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "wind_speed_10m") val windSpeed: Double
)

@JsonClass(generateAdapter = true)
data class HourlyForecast(
    val time: List<String>,
    @Json(name = "temperature_2m") val temperatures: List<Double>,
    @Json(name = "relative_humidity_2m") val humidities: List<Double>,
    @Json(name = "weather_code") val weatherCodes: List<Int>
)

@JsonClass(generateAdapter = true)
data class DailyForecast(
    val time: List<String>,
    @Json(name = "weather_code") val weatherCodes: List<Int>,
    @Json(name = "temperature_2m_max") val temperaturesMax: List<Double>,
    @Json(name = "temperature_2m_min") val temperaturesMin: List<Double>,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>
)
