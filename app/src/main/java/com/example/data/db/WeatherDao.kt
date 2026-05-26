package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE id = 1 LIMIT 1")
    fun getWeatherCache(): Flow<WeatherCache?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherCache(weatherCache: WeatherCache)

    @Query("DELETE FROM weather_cache WHERE id = 1")
    suspend fun clearWeatherCache()
}
