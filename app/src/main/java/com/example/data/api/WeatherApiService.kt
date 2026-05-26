package com.example.data.api

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,weather_code,wind_speed_10m",
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,weather_code",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/"

        fun create(): WeatherApiService {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(WeatherApiService::class.java)
        }
    }
}
