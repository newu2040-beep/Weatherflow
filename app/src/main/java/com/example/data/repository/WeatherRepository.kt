package com.example.data.repository

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.example.data.api.WeatherApiService
import com.example.data.api.WeatherResponse
import com.example.data.db.WeatherCache
import com.example.data.db.WeatherDao
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale

class WeatherRepository(
    private val weatherDao: WeatherDao,
    private val apiService: WeatherApiService
) {
    val cachedWeather: Flow<WeatherCache?> = weatherDao.getWeatherCache()
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val weatherResponseAdapter = moshi.adapter(WeatherResponse::class.java)

    /**
     * Parsing JSON back to WeatherResponse helper
     */
    fun parseCachedResponse(json: String): WeatherResponse? {
        return try {
            weatherResponseAdapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Resolves current city/town name from coordinates using Android Geocoder
     */
    private suspend fun getCityNameFromCoordinates(context: Context, latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) {
            return@withContext String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
        }
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: address.adminArea ?: address.countryName
                if (!city.isNullOrBlank()) {
                    return@withContext city
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
    }

    /**
     * Fetches current coordinates' weather from API, reverse-geocodes city, and caches in Room.
     */
    suspend fun fetchAndCacheWeather(
        context: Context,
        latitude: Double,
        longitude: Double
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        try {
            // Fetch live API forecast
            val response = apiService.getForecast(latitude, longitude)
            
            // Resolve local name
            val cityName = getCityNameFromCoordinates(context, latitude, longitude)
            
            // Serialize
            val jsonString = weatherResponseAdapter.toJson(response)
            
            // Cache in Room
            val cache = WeatherCache(
                id = 1,
                latitude = latitude,
                longitude = longitude,
                cityName = cityName,
                weatherJson = jsonString,
                lastUpdated = System.currentTimeMillis()
            )
            weatherDao.insertWeatherCache(cache)
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun clearCache() {
        weatherDao.clearWeatherCache()
    }
}
