package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    activeTheme: WeatherTheme = WeatherTheme.OCEAN_SLATE,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = activeTheme.darkPrimary,
            onPrimary = activeTheme.darkOnPrimary,
            secondary = activeTheme.darkSecondary,
            background = activeTheme.darkBackgroundGradients.first(),
            surface = activeTheme.darkSurface,
            onSurface = activeTheme.darkOnSurface
        )
    } else {
        lightColorScheme(
            primary = activeTheme.lightPrimary,
            onPrimary = activeTheme.lightOnPrimary,
            secondary = activeTheme.lightSecondary,
            background = activeTheme.lightBackgroundGradients.first(),
            surface = activeTheme.lightSurface,
            onSurface = activeTheme.lightOnSurface
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
