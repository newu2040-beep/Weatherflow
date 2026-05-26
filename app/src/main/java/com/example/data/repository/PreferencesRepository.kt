package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "weather_flow_prefs")

class PreferencesRepository(private val context: Context) {
    companion object {
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val STYLED_THEME = intPreferencesKey("styled_theme")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_DARK_MODE] ?: true // default to dark theme for visual premium feel
    }

    val selectedTheme: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[STYLED_THEME] ?: 0 // default to Dynamic/Classic
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_DARK_MODE] = enabled
        }
    }

    suspend fun setTheme(themeIndex: Int) {
        context.dataStore.edit { prefs ->
            prefs[STYLED_THEME] = themeIndex
        }
    }
}
