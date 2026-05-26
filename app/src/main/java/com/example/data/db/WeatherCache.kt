package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCache(
    @PrimaryKey val id: Int = 1,
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val weatherJson: String,
    val lastUpdated: Long
)
