package com.example.ui.viewmodel

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.WeatherResponse
import com.example.data.db.WeatherCache
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.WeatherRepository
import com.example.ui.notification.WeatherNotificationManager
import com.example.ui.theme.WeatherTheme
import com.example.ui.widget.WeatherWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(
        val weather: WeatherResponse,
        val cityName: String,
        val lastUpdated: Long,
        val isFromOfflineCache: Boolean
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _networkError = MutableStateFlow<String?>(null)
    val networkError: StateFlow<String?> = _networkError.asStateFlow()

    // Observable flows for settings
    val isDarkMode: StateFlow<Boolean> = preferencesRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val selectedThemeIndex: StateFlow<Int> = preferencesRepository.selectedTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentTheme: StateFlow<WeatherTheme> = selectedThemeIndex
        .combine(preferencesRepository.selectedTheme) { index, _ ->
            WeatherTheme.fromOrdinal(index)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeatherTheme.OCEAN_SLATE)

    // Primary UI state combining Room Cache and API status
    val weatherUiState: StateFlow<WeatherUiState> = weatherRepository.cachedWeather
        .combine(_networkError) { cache, error ->
            if (cache != null) {
                val parsed = weatherRepository.parseCachedResponse(cache.weatherJson)
                if (parsed != null) {
                    WeatherUiState.Success(
                        weather = parsed,
                        cityName = cache.cityName,
                        lastUpdated = cache.lastUpdated,
                        isFromOfflineCache = error != null
                    )
                } else {
                    WeatherUiState.Error("Failed to parse cached weather")
                }
            } else if (error != null) {
                WeatherUiState.Error(error)
            } else {
                WeatherUiState.Loading
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeatherUiState.Loading)

    /**
     * Toggles App Light/Dark Mode
     */
    fun toggleDarkMode() {
        viewModelScope.launch {
            preferencesRepository.setDarkMode(!isDarkMode.value)
        }
    }

    /**
     * Changes custom weather display theme
     */
    fun changeTheme(index: Int) {
        viewModelScope.launch {
            preferencesRepository.setTheme(index)
        }
    }

    /**
     * Perform immediate sync of weather for the given location coordinate
     */
    fun syncWeather(context: Context, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isRefreshing.value = true
            _networkError.value = null
            
            val result = weatherRepository.fetchAndCacheWeather(context, latitude, longitude)
            
            if (result.isSuccess) {
                _networkError.value = null
                // Check if the fresh data contains potential severe parameters and fire alerts
                val response = result.getOrNull()
                if (response != null) {
                    checkAndTriggerSevereAlert(context, response)
                }
                updateWidget(context)
            } else {
                _networkError.value = result.exceptionOrNull()?.message ?: "Check your internet connection"
            }
            _isRefreshing.value = false
        }
    }

    /**
     * Inspects fetched weather for hazardous parameters (e.g., wind > 30 km/h or extreme storms)
     */
    private fun checkAndTriggerSevereAlert(context: Context, weather: WeatherResponse) {
        val wind = weather.current?.windSpeed ?: 0.0
        val rain = weather.current?.rain ?: 0.0
        val code = weather.current?.weatherCode ?: 0
        
        val notifier = WeatherNotificationManager(context)
        
        when {
            code in listOf(95, 96, 99) -> {
                notifier.showSevereAlert(
                    "Thunderstorm warning",
                    "A localized severe thunderstorm is occurring with gusty winds and rain. Stay indoors!"
                )
            }
            wind > 40.0 -> {
                notifier.showSevereAlert(
                    "Severe Wind Advisory",
                    "Dangerous localized winds detected at ${wind} km/h. Secure loose outdoor objects!"
                )
            }
            rain > 15.0 -> {
                notifier.showSevereAlert(
                    "Heavy Precipitation Alert",
                    "Intense downpour active. Heavy rain risks minor localized flooding."
                )
            }
        }
    }

    /**
     * Forces immediate delivery of a simulated severe push alert for showcase / safety verification
     */
    fun triggerSimulatedSevereAlert(context: Context, customAlertIndex: Int) {
        val notifier = WeatherNotificationManager(context)
        when (customAlertIndex) {
            0 -> notifier.showSevereAlert(
                "⚠️ EXTREME WEATHER WARNING",
                "High Wind Warning: Expect violent localized wind gusts reaching up to 65 km/h. Secure all patio furniture and move inland."
            )
            1 -> notifier.showSevereAlert(
                "⚡ FLASH FLOOD ADVISORY",
                "Flash Flood Watch in effect. Expected severe rainfall of 35mm/hr. Avoid standard underpasses and low-lying trails!"
            )
            else -> notifier.showSevereAlert(
                "🌪️ SEVERE THUNDERSTORM ALERT",
                "Severe Thunderstorm with Hail spotted moving through adjacent areas. High risk of power interruptions."
            )
        }
    }

    /**
     * Updates homescreen widget with fresh cached data
     */
    private fun updateWidget(context: Context) {
        val intent = Intent(context, WeatherWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, WeatherWidgetProvider::class.java)
            )
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }

    class Factory(
        private val weatherRepository: WeatherRepository,
        private val preferencesRepository: PreferencesRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WeatherViewModel(weatherRepository, preferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
