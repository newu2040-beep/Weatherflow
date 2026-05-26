package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class WeatherTheme(
    val nameString: String,
    // Dark mode configurations
    val darkPrimary: Color,
    val darkOnPrimary: Color,
    val darkSecondary: Color,
    val darkBackgroundGradients: List<Color>,
    val darkSurface: Color,
    val darkAccent: Color,
    val darkOnSurface: Color,
    
    // Light mode configurations
    val lightPrimary: Color,
    val lightOnPrimary: Color,
    val lightSecondary: Color,
    val lightBackgroundGradients: List<Color>,
    val lightSurface: Color,
    val lightAccent: Color,
    val lightOnSurface: Color
) {
    OCEAN_SLATE(
        nameString = "Professional Polish",
        darkPrimary = Color(0xFFD0BCFF),
        darkOnPrimary = Color(0xFF381E72),
        darkSecondary = Color(0xFF49454F),
        darkBackgroundGradients = listOf(Color(0xFF121212), Color(0xFF181622)),
        darkSurface = Color(0xFF1C1B1F),
        darkAccent = Color(0xFFD0BCFF),
        darkOnSurface = Color(0xFFE6E1E5),
        
        lightPrimary = Color(0xFF6750A4),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightSecondary = Color(0xFF625B71),
        lightBackgroundGradients = listOf(Color(0xFFF7F2FA), Color(0xFFECE6F0)),
        lightSurface = Color(0xFFECE6F0),
        lightAccent = Color(0xFF6750A4),
        lightOnSurface = Color(0xFF1C1B1F)
    ),
    CLEAR_SKY(
        nameString = "Sky Blue",
        darkPrimary = Color(0xFF6ABFFF),
        darkOnPrimary = Color(0xFF002244),
        darkSecondary = Color(0xFF003F7A),
        darkBackgroundGradients = listOf(Color(0xFF060D19), Color(0xFF0E1A2B)),
        darkSurface = Color(0xFF16273F),
        darkAccent = Color(0xFF90D5FF),
        darkOnSurface = Color(0xFFECEFF1),
        
        lightPrimary = Color(0xFF0066CC),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightSecondary = Color(0xFF4A90E2),
        lightBackgroundGradients = listOf(Color(0xFFE6F2FF), Color(0xFFBFE0FF)),
        lightSurface = Color(0xFFD9ECFF),
        lightAccent = Color(0xFF003F7A),
        lightOnSurface = Color(0xFF001F3F)
    ),
    COZY_SUNSET(
        nameString = "Amber Sunset",
        darkPrimary = Color(0xFFFF9E22),
        darkOnPrimary = Color(0xFF301500),
        darkSecondary = Color(0xFFFFB300),
        darkBackgroundGradients = listOf(Color(0xFF170900), Color(0xFF2E1300)),
        darkSurface = Color(0xFF3F1D02),
        darkAccent = Color(0xFFFFF09F),
        darkOnSurface = Color(0xFFFFF1E6),
        
        lightPrimary = Color(0xFFE65100),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightSecondary = Color(0xFFF57C00),
        lightBackgroundGradients = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
        lightSurface = Color(0xFFFFE0B2),
        lightAccent = Color(0xFFBF360C),
        lightOnSurface = Color(0xFF3E1E00)
    ),
    FOREST_FOLIAGE(
        nameString = "Forest Mist",
        darkPrimary = Color(0xFF81C784),
        darkOnPrimary = Color(0xFF0A200B),
        darkSecondary = Color(0xFF4CAF50),
        darkBackgroundGradients = listOf(Color(0xFF060E07), Color(0xFF122314)),
        darkSurface = Color(0xFF1B351E),
        darkAccent = Color(0xFFB1F2B5),
        darkOnSurface = Color(0xFFE8F5E9),
        
        lightPrimary = Color(0xFF2E7D32),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightSecondary = Color(0xFF4CAF50),
        lightBackgroundGradients = listOf(Color(0xFFE8F5E9), Color(0xFFCDEDCE)),
        lightSurface = Color(0xFFC8E6C9),
        lightAccent = Color(0xFF1B5E20),
        lightOnSurface = Color(0xFF0A200B)
    ),
    MIDNIGHT_AURORA(
        nameString = "Midnight Aurora",
        darkPrimary = Color(0xFFC792EA),
        darkOnPrimary = Color(0xFF1F0038),
        darkSecondary = Color(0xFF9C42F5),
        darkBackgroundGradients = listOf(Color(0xFF070014), Color(0xFF1A0135)),
        darkSurface = Color(0xFF29004F),
        darkAccent = Color(0xFF00E6FF),
        darkOnSurface = Color(0xFFECE4FA),
        
        lightPrimary = Color(0xFF6C10B0),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightSecondary = Color(0xFF9C42F5),
        lightBackgroundGradients = listOf(Color(0xFFF7EFFF), Color(0xFFECD9FA)),
        lightSurface = Color(0xFFE5CCFA),
        lightAccent = Color(0xFF32005A),
        lightOnSurface = Color(0xFF250041)
    ),
    CYBERPUNK_NEON(
        nameString = "Cyberpunk Neon",
        darkPrimary = Color(0xFFFF2A85),
        darkOnPrimary = Color(0xFF0F051D),
        darkSecondary = Color(0xFF00F5FF),
        darkBackgroundGradients = listOf(Color(0xFF0D021A), Color(0xFF1D003B)),
        darkSurface = Color(0xFF150D2A),
        darkAccent = Color(0xFFBD00FF),
        darkOnSurface = Color(0xFFFFF0F5),
        
        lightPrimary = Color(0xFFC7006E),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightSecondary = Color(0xFF007A87),
        lightBackgroundGradients = listOf(Color(0xFFFFF2FA), Color(0xFFF0DCEF)),
        lightSurface = Color(0xFFF9EAF7),
        lightAccent = Color(0xFF8B008B),
        lightOnSurface = Color(0xFF190620)
    ),
    NORDIC_MINIMAL(
        nameString = "Nordic Ice",
        darkPrimary = Color(0xFFAED8F2),
        darkOnPrimary = Color(0xFF1B2A32),
        darkSecondary = Color(0xFF5F7582),
        darkBackgroundGradients = listOf(Color(0xFF0E1317), Color(0xFF182026)),
        darkSurface = Color(0xFF222B33),
        darkAccent = Color(0xFFE2F3FC),
        darkOnSurface = Color(0xFFECEFF1),
        
        lightPrimary = Color(0xFF2D3E4A),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightSecondary = Color(0xFF6B8A9E),
        lightBackgroundGradients = listOf(Color(0xFFF5F7F8), Color(0xFFE5E9EC)),
        lightSurface = Color(0xFFDFE4E8),
        lightAccent = Color(0xFF1A384D),
        lightOnSurface = Color(0xFF111E26)
    ),
    DESERT_OASIS(
        nameString = "Desert Oasis",
        darkPrimary = Color(0xFFF4B266),
        darkOnPrimary = Color(0xFF421D00),
        darkSecondary = Color(0xFF8EA08C),
        darkBackgroundGradients = listOf(Color(0xFF17110C), Color(0xFF281D14)),
        darkSurface = Color(0xFF382A1E),
        darkAccent = Color(0xFFE2725B),
        darkOnSurface = Color(0xFFF7EFE8),
        
        lightPrimary = Color(0xFFC05621),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightSecondary = Color(0xFF5A654D),
        lightBackgroundGradients = listOf(Color(0xFFFCF9F5), Color(0xFFF3EDE2)),
        lightSurface = Color(0xFFEADFCF),
        lightAccent = Color(0xFF9C4221),
        lightOnSurface = Color(0xFF2A1C0B)
    ),
    CHAMPAGNE_GOLD(
        nameString = "Champagne Gold",
        darkPrimary = Color(0xFFE6C79C),
        darkOnPrimary = Color(0xFF3A2300),
        darkSecondary = Color(0xFFB59367),
        darkBackgroundGradients = listOf(Color(0xFF120C16), Color(0xFF1E1324)),
        darkSurface = Color(0xFF2A1D30),
        darkAccent = Color(0xFFFFF4D6),
        darkOnSurface = Color(0xFFF6F0F8),
        
        lightPrimary = Color(0xFF8B6B40),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightSecondary = Color(0xFFCFB38C),
        lightBackgroundGradients = listOf(Color(0xFFFAF6F0), Color(0xFFF0E7D8)),
        lightSurface = Color(0xFFE9DEC8),
        lightAccent = Color(0xFF543E1F),
        lightOnSurface = Color(0xFF231A0B)
    ),
    CRIMSON_HORIZON(
        nameString = "Crimson Horizon",
        darkPrimary = Color(0xFFFF5252),
        darkOnPrimary = Color(0xFF230006),
        darkSecondary = Color(0xFFFF8A80),
        darkBackgroundGradients = listOf(Color(0xFF0F0B0C), Color(0xFF1D1315)),
        darkSurface = Color(0xFF291B1D),
        darkAccent = Color(0xFFFF3D00),
        darkOnSurface = Color(0xFFFBEBEB),
        
        lightPrimary = Color(0xFFD32F2F),
        lightOnPrimary = Color(0xFFFFFFFF),
        lightSecondary = Color(0xFFE57373),
        lightBackgroundGradients = listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2)),
        lightSurface = Color(0xFFEF9A9A),
        lightAccent = Color(0xFFB71C1C),
        lightOnSurface = Color(0xFF270608)
    );

    companion object {
        fun fromOrdinal(ordinal: Int): WeatherTheme {
            return entries.getOrElse(ordinal) { OCEAN_SLATE }
        }
    }
}
