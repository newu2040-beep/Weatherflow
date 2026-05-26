package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.api.WeatherApiService
import com.example.data.db.AppDatabase
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.WeatherRepository
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite caching database and OpenMeteo API Service
        val database = AppDatabase.getDatabase(applicationContext)
        val apiService = WeatherApiService.create()
        val weatherRepository = WeatherRepository(database.weatherDao(), apiService)
        val preferencesRepository = PreferencesRepository(applicationContext)

        // Instantiate Weather ViewModel
        val viewModel: WeatherViewModel by viewModels {
            WeatherViewModel.Factory(weatherRepository, preferencesRepository)
        }

        setContent {
            val systemIsDarkMode by viewModel.isDarkMode.collectAsState()
            val activeTheme by viewModel.currentTheme.collectAsState()

            MyApplicationTheme(
                darkTheme = systemIsDarkMode,
                activeTheme = activeTheme
            ) {
                WeatherScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
