package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.api.WeatherResponse
import com.example.ui.theme.WeatherTheme
import com.example.ui.viewmodel.WeatherUiState
import com.example.ui.viewmodel.WeatherViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val systemIsDarkMode by viewModel.isDarkMode.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val weatherState by viewModel.weatherUiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val networkError by viewModel.networkError.collectAsState()
    val selectedThemeIndex by viewModel.selectedThemeIndex.collectAsState()

    // Setup Fused Location Provider client
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Unified permission request launchers
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // States to handle location permission status
    var permissionStatusText by remember { mutableStateOf("") }
    
    // Callback helper to check permissions and synchronize weather
    val syncWithCurrentCoordinates: () -> Unit = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.syncWeather(context, location.latitude, location.longitude)
                        permissionStatusText = "Coords: ${String.format(Locale.getDefault(), "%.3f, %.3f", location.latitude, location.longitude)}"
                    } else {
                        // Fallback coordinate: London
                        viewModel.syncWeather(context, 51.5074, -0.1278)
                        permissionStatusText = "Location empty: loaded London fallback"
                    }
                }.addOnFailureListener {
                    // Fallback to London on failure
                    viewModel.syncWeather(context, 51.5074, -0.1278)
                    permissionStatusText = "Location failed: loaded London fallback"
                }
            } catch (e: SecurityException) {
                // If permission is revoked unexpectedly
                viewModel.syncWeather(context, 51.5074, -0.1278)
            }
        } else {
            // Initial load of standard cached coordinates or NYC fallback
            viewModel.syncWeather(context, 40.7128, -74.0060)
            permissionStatusText = "No GPS: loaded NYC fallback"
        }
    }

    // Permission launcher for location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        if (granted) {
            syncWithCurrentCoordinates()
            Toast.makeText(context, "Location synced successfully!", Toast.LENGTH_SHORT).show()
        } else {
            // Load fallback values
            viewModel.syncWeather(context, 40.7128, -74.0060)
            permissionStatusText = "Permission Denied: loaded NYC"
            Toast.makeText(context, "Permission Denied: displaying NYC", Toast.LENGTH_LONG).show()
        }
    }

    // Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(context, "Severe weather alerts active!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Muted severe dynamic alerts", Toast.LENGTH_SHORT).show()
        }
    }

    // Trigger initial check and setup on launch
    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        // Request notifications permission immediately on target devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (hasFine || hasCoarse) {
            syncWithCurrentCoordinates()
        } else {
            locationPermissionLauncher.launch(locationPermissions)
        }
    }

    val colors = currentTheme
    val gradientColors = if (systemIsDarkMode) colors.darkBackgroundGradients else colors.lightBackgroundGradients
    val primaryColor = if (systemIsDarkMode) colors.darkPrimary else colors.lightPrimary
    val surfaceColor = if (systemIsDarkMode) colors.darkSurface else colors.lightSurface
    val onSurfaceColor = if (systemIsDarkMode) colors.darkOnSurface else colors.lightOnSurface
    val accentColor = if (systemIsDarkMode) colors.darkAccent else colors.lightAccent

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // Aesthetic Top Wave Canvas
        AmbientCanvasDecoration(
            color = accentColor.copy(alpha = 0.08f),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = { WeatherBottomNavigation() },
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WEATHERFLOW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor,
                            letterSpacing = 1.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Current location marker",
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (val state = weatherState) {
                                    is WeatherUiState.Success -> state.cityName
                                    is WeatherUiState.Loading -> "Locating..."
                                    else -> "Disconnected"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                        }
                    }

                    // Top Action Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Light/Dark mode toggle
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(surfaceColor.copy(alpha = 0.6f))
                                .size(40.dp)
                                .testTag("dark_mode_toggle")
                        ) {
                            Icon(
                                imageVector = if (systemIsDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle theme mode type",
                                tint = primaryColor
                            )
                        }

                        // Manual synchronize button
                        IconButton(
                            onClick = { syncWithCurrentCoordinates() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(surfaceColor.copy(alpha = 0.6f))
                                .size(40.dp)
                                .testTag("manual_sync_button")
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    color = primaryColor,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync forecast details",
                                    tint = primaryColor
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Connection or cache status pill bar
                item {
                    StatusIndicatorBar(
                        state = weatherState,
                        networkError = networkError,
                        primaryColor = primaryColor,
                        surfaceColor = surfaceColor,
                        onSurfaceColor = onSurfaceColor,
                        permissionText = permissionStatusText,
                        onRequestPermission = {
                            locationPermissionLauncher.launch(locationPermissions)
                        }
                    )
                }

                // Current Weather UI presentation
                item {
                    CurrentWeatherSection(
                        state = weatherState,
                        primaryColor = primaryColor,
                        accentColor = accentColor,
                        onSurfaceColor = onSurfaceColor,
                        surfaceColor = surfaceColor
                    )
                }

                // Beautiful Weather Themes Selector (personalize experience)
                item {
                    ThemeSelectorSection(
                        surfaceColor = surfaceColor,
                        onSurfaceColor = onSurfaceColor,
                        primaryColor = primaryColor,
                        selectedIndex = selectedThemeIndex,
                        onThemeSelect = { viewModel.changeTheme(it) }
                    )
                }

                // Horizontal Hourly Forecast Timeline
                item {
                    HourlyForecastSection(
                        state = weatherState,
                        surfaceColor = surfaceColor,
                        primaryColor = primaryColor,
                        accentColor = accentColor,
                        onSurfaceColor = onSurfaceColor
                    )
                }

                // Vertical 7-Day Outlook Forecast
                item {
                    WeeklyForecastSection(
                        state = weatherState,
                        surfaceColor = surfaceColor,
                        onSurfaceColor = onSurfaceColor,
                        accentColor = accentColor,
                        primaryColor = primaryColor
                    )
                }

                // Quick Glance Widgets Banner Card
                item {
                    QuickGlancePromoCard()
                }

                // Urgent Severe Alert Simulation settings
                item {
                    SevereAlertSimulatorSection(
                        surfaceColor = surfaceColor,
                        onSurfaceColor = onSurfaceColor,
                        primaryColor = primaryColor,
                        accentColor = accentColor,
                        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        } else true,
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onTriggerAlert = { index ->
                            viewModel.triggerSimulatedSevereAlert(context, index)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Custom Canvas wave decoration producing subtle atmospheric ambient waves
 */
@Composable
fun AmbientCanvasDecoration(color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_wave_offset"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height * 0.4f
        val path = androidx.compose.ui.graphics.Path()
        
        path.moveTo(0f, 0f)
        path.lineTo(0f, centerY)
        
        for (x in 0..width.toInt() step 5) {
            val radians = Math.toRadians((x.toDouble() / width * 360) + waveOffset)
            val y = centerY + Math.sin(radians).toFloat() * 25.dp.toPx()
            path.lineTo(x.toFloat(), y)
        }
        
        path.lineTo(width, 0f)
        path.close()
        drawPath(path = path, color = color)
    }
}

@Composable
fun StatusIndicatorBar(
    state: WeatherUiState,
    networkError: String?,
    primaryColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    permissionText: String,
    onRequestPermission: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(surfaceColor.copy(alpha = 0.5f))
                .border(1.dp, primaryColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = if (state is WeatherUiState.Success && !state.isFromOfflineCache) {
                    Icons.Default.Cloud
                } else {
                    Icons.Default.Warning
                }
                
                val iconColor = if (state is WeatherUiState.Success && !state.isFromOfflineCache) {
                    Color(0xFF4CAF50)
                } else {
                    Color(0xFFFF9800)
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "Database sync status indicator icon",
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        state is WeatherUiState.Success && !state.isFromOfflineCache -> "Real-time Synced"
                        state is WeatherUiState.Success && state.isFromOfflineCache -> "Offline Local Cache"
                        state is WeatherUiState.Loading -> "Refreshing Forecast..."
                        else -> "No Data Synced"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceColor
                )
            }

            // Sync metadata
            if (state is WeatherUiState.Success) {
                val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                Text(
                    text = "As of ${sdf.format(Date(state.lastUpdated))}",
                    fontSize = 11.sp,
                    color = onSurfaceColor.copy(alpha = 0.6f)
                )
            } else {
                Text(
                    text = "Offline active",
                    fontSize = 11.sp,
                    color = onSurfaceColor.copy(alpha = 0.6f)
                )
            }
        }

        // Show offline warning message when network fails
        if (networkError != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF9800).copy(alpha = 0.15f))
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning outline indicator",
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sync failure: presenting offline cache",
                    fontSize = 11.sp,
                    color = Color(0xFFFFE082),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Mini bar for location accuracy trigger
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRequestPermission() }
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = permissionText.ifBlank { "Detecting coordinates..." },
                fontSize = 11.sp,
                color = onSurfaceColor.copy(alpha = 0.5f)
            )
            Text(
                text = "Pick Local Coordinates ↗",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }
    }
}

@Composable
fun CurrentWeatherSection(
    state: WeatherUiState,
    primaryColor: Color,
    accentColor: Color,
    onSurfaceColor: Color,
    surfaceColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state) {
            is WeatherUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .height(240.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }
            is WeatherUiState.Success -> {
                val current = state.weather.current
                val code = current?.weatherCode ?: 0
                val (condText, icon) = getWeatherDetails(code)
                val dailyMax = state.weather.daily?.temperaturesMax?.firstOrNull() ?: 0.0
                val dailyMin = state.weather.daily?.temperaturesMin?.firstOrNull() ?: 0.0

                // Check severe alert conditions
                val isSevere = code in listOf(95, 96, 99) || (current?.windSpeed ?: 0.0) > 40.0 || (state.weather.daily?.precipitationSum?.firstOrNull() ?: 0.0) > 15.0

                Spacer(modifier = Modifier.height(6.dp))

                // Premium Gradient Container card (Royal purple theme from Design HTML)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("current_weather_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF381E72), Color(0xFF4F378B))
                                )
                            )
                            .padding(vertical = 24.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Severe Alert Overlay Badge
                        if (isSevere) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0xFFFFB4AB))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "SEVERE ALERT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF690005),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Weather animation illustration Canvas
                            Box(
                                modifier = Modifier.size(90.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                WeatherCodeAnimationCanvas(code = code, accentColor = Color(0xFFD0BCFF))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Giant temperature display (e.g. 72°)
                            Text(
                                text = String.format(Locale.getDefault(), "%.0f°", current?.temperature ?: 0.0),
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.White,
                                modifier = Modifier.testTag("current_temp_display"),
                                letterSpacing = (-2).sp
                            )

                            // Weather condition text (e.g., Thunderstorms)
                            Text(
                                text = condText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // High/Low Feels Like details
                            Text(
                                text = String.format(
                                    Locale.getDefault(),
                                    "H: %.0f°  L: %.0f°  •  Feels like %.0f°",
                                    dailyMax,
                                    dailyMin,
                                    current?.apparentTemperature ?: 0.0
                                ),
                                fontSize = 12.sp,
                                color = Color(0xFFD0BCFF).copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Beautiful grid-cols-3 arrangement of metric item cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricItem(
                        icon = Icons.Default.Air,
                        title = "Wind",
                        value = "${current?.windSpeed ?: 0.0} km/h",
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.weight(1f)
                    )
                    MetricItem(
                        icon = Icons.Default.Water,
                        title = "Humidity",
                        value = "${current?.humidity ?: 0.0}%",
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.weight(1f)
                    )
                    MetricItem(
                        icon = Icons.Default.Umbrella,
                        title = "Rain Index",
                        value = "${state.weather.daily?.precipitationSum?.firstOrNull() ?: 0.0} mm",
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            is WeatherUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(surfaceColor.copy(alpha = 0.4f))
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning error icon",
                            tint = Color.Red,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Forecast Unavailable Offline",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.message,
                            fontSize = 12.sp,
                            color = onSurfaceColor.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(
    icon: ImageVector,
    title: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$title info icon decoration",
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCAC4D0),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ThemeSelectorSection(
    surfaceColor: Color,
    onSurfaceColor: Color,
    primaryColor: Color,
    selectedIndex: Int,
    onThemeSelect: (Int) -> Unit
) {
    val themes = WeatherTheme.entries

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "PERSONALIZE WEATHER THEMES",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = onSurfaceColor.copy(alpha = 0.7f),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(surfaceColor.copy(alpha = 0.4f))
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(themes) { index, theme ->
                val isSelected = index == selectedIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onThemeSelect(index) }
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(theme.darkPrimary, theme.darkSecondary)
                                )
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active theme confirmation checkmark",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = theme.nameString,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) primaryColor else onSurfaceColor.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun HourlyForecastSection(
    state: WeatherUiState,
    surfaceColor: Color,
    primaryColor: Color,
    accentColor: Color,
    onSurfaceColor: Color
) {
    if (state !is WeatherUiState.Success) return

    val hourly = state.weather.hourly ?: return
    val limit = minOf(24, hourly.time.size)

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
            border = BorderStroke(1.dp, Color(0xFF49454F))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HOURLY FORECAST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCAC4D0),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Synced 2m ago",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD0BCFF)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(limit) { index ->
                        val timeRaw = hourly.time[index]
                        val hourLabel = formatIsoTimeToHour(timeRaw)
                        val temp = hourly.temperatures[index]
                        val code = hourly.weatherCodes[index]
                        val (summary, icon) = getWeatherDetails(code)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = hourLabel,
                                fontSize = 11.sp,
                                color = Color(0xFFCAC4D0),
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Icon(
                                imageVector = icon,
                                contentDescription = "$summary snapshot",
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.0f°", temp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyForecastSection(
    state: WeatherUiState,
    surfaceColor: Color,
    onSurfaceColor: Color,
    accentColor: Color,
    primaryColor: Color
) {
    if (state !is WeatherUiState.Success) return

    val daily = state.weather.daily ?: return
    val size = daily.time.size

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "7-DAY OUTLOOK ALMANAC",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = onSurfaceColor.copy(alpha = 0.7f),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(surfaceColor.copy(alpha = 0.5f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (i in 0 until size) {
                val dateRaw = daily.time[i]
                val dayLabel = formatIsoTimeToDayName(dateRaw, i)
                val code = daily.weatherCodes[i]
                val max = daily.temperaturesMax[i]
                val min = daily.temperaturesMin[i]
                val (summary, icon) = getWeatherDetails(code)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Day title and weather condition indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "$summary status icon decoration",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dayLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurfaceColor
                        )
                    }

                    // Condition descriptive text summary
                    Text(
                        text = summary,
                        fontSize = 11.sp,
                        color = primaryColor,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )

                    // Max Min indicators
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.0f°", min),
                            fontSize = 13.sp,
                            color = onSurfaceColor.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(onSurfaceColor.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .fillMaxHeight()
                                    .align(Alignment.Center)
                                    .background(accentColor)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.0f°", max),
                            fontSize = 13.sp,
                            color = onSurfaceColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (i < size - 1) {
                    Divider(color = onSurfaceColor.copy(alpha = 0.08f), thickness = 0.8.dp)
                }
            }
        }
    }
}

@Composable
fun SevereAlertSimulatorSection(
    surfaceColor: Color,
    onSurfaceColor: Color,
    primaryColor: Color,
    accentColor: Color,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onTriggerAlert: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "ALERT PUSH NOTIFICATION SIMULATOR",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = onSurfaceColor.copy(alpha = 0.7f),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(surfaceColor.copy(alpha = 0.5f))
                .border(1.dp, Color(0xFFFF5722).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notification alert indicator",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Severe Alerts simulator",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                }

                if (!hasNotificationPermission) {
                    TextButton(onClick = onRequestNotificationPermission) {
                        Text(
                            text = "Grant Permission",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Permission Active",
                            fontSize = 9.sp,
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Simulate hazardous incoming alerts instantly in the top system drawer. Click to test rendering and urgent alert delivery flows.",
                fontSize = 11.sp,
                color = onSurfaceColor.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onTriggerAlert(0) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("simulate_wind_button"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("⚠️ Fire Extreme Wind Advisory Warning", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { onTriggerAlert(1) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2185B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("simulate_flood_button"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("🌊 Fire Torrential Flash Flood Warning", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Custom Canvas vector code drawings that render rotating suns, cloudy shapes, and dripping raindrops based on conditions.
 */
@Composable
fun WeatherCodeAnimationCanvas(code: Int, accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_canvas_anim")
    
    val angleRotation by infiniteTransition.animateFloat(
        initialValue =  0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle_rotation"
    )

    val cloudPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width * 0.22f

        when (code) {
            0 -> {
                // Shiny Sun
                drawCircle(
                    color = accentColor,
                    radius = radius,
                    center = center
                )
                // Sun rays drawing
                for (i in 0 until 8) {
                    val angleDeg = (i * 45) + angleRotation
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val startX = center.x + Math.cos(angleRad).toFloat() * (radius + 10f)
                    val startY = center.y + Math.sin(angleRad).toFloat() * (radius + 10f)
                    val endX = center.x + Math.cos(angleRad).toFloat() * (radius + 28f)
                    val endY = center.y + Math.sin(angleRad).toFloat() * (radius + 28f)
                    
                    drawLine(
                        color = accentColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 6.dp.toPx()
                    )
                }
            }
            1, 2, 3 -> {
                // Partly Cloudy Sun plus Cloud
                drawCircle(
                    color = accentColor,
                    radius = radius * 0.8f,
                    center = Offset(center.x + 15.dp.toPx(), center.y - 12.dp.toPx())
                )
                
                // Cloud shape
                val cloudPath = androidx.compose.ui.graphics.Path().apply {
                    val cw = center.x
                    val ch = center.y + 10.dp.toPx()
                    moveTo(cw - 30.dp.toPx(), ch + 10.dp.toPx())
                    cubicTo(
                        cw - 40.dp.toPx(), ch - 15.dp.toPx(),
                        cw - 10.dp.toPx(), ch - 25.dp.toPx(),
                        cw, ch - 10.dp.toPx()
                    )
                    cubicTo(cw + 10.dp.toPx(), ch - 30.dp.toPx(), cw + 40.dp.toPx(), ch - 10.dp.toPx(), cw + 35.dp.toPx(), ch + 10.dp.toPx())
                    lineTo(cw - 30.dp.toPx(), ch + 10.dp.toPx())
                    close()
                }
                drawPath(
                    path = cloudPath,
                    color = Color.White.copy(alpha = 0.85f * cloudPulse)
                )
            }
            51, 53, 55, 61, 63, 65, 80, 81, 82 -> {
                // Heavy Raining Cloud
                val cloudPath = androidx.compose.ui.graphics.Path().apply {
                    val cw = center.x
                    val ch = center.y - 10.dp.toPx()
                    moveTo(cw - 35.dp.toPx(), ch + 10.dp.toPx())
                    cubicTo(cw - 45.dp.toPx(), ch - 15.dp.toPx(), cw - 15.dp.toPx(), ch - 25.dp.toPx(), cw, ch - 10.dp.toPx())
                    cubicTo(cw + 10.dp.toPx(), ch - 30.dp.toPx(), cw + 40.dp.toPx(), ch - 10.dp.toPx(), cw + 35.dp.toPx(), ch + 10.dp.toPx())
                    lineTo(cw - 35.dp.toPx(), ch + 10.dp.toPx())
                    close()
                }
                drawPath(path = cloudPath, color = Color.White.copy(alpha = 0.4f))
                
                // Falling rain drops
                for (i in 0 until 3) {
                    val dropX = center.x - 20.dp.toPx() + (i * 20.dp.toPx())
                    val dropY = center.y + 15.dp.toPx() + (Math.sin(((angleRotation * 5) + i).toDouble()).toFloat() * 10f)
                    
                    drawLine(
                        color = accentColor,
                        start = Offset(dropX, dropY),
                        end = Offset(dropX - 4.dp.toPx(), dropY + 12.dp.toPx()),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
            95, 96, 99 -> {
                // Storm Blitzy
                drawCircle(
                    color = Color.DarkGray,
                    radius = radius * 1.1f,
                    center = center
                )
                // Lightning Blitz shape
                val flashPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x + 5.dp.toPx(), center.y - 25.dp.toPx())
                    lineTo(center.x - 15.dp.toPx(), center.y + 2.dp.toPx())
                    lineTo(center.x + 2.dp.toPx(), center.y + 2.dp.toPx())
                    lineTo(center.x - 8.dp.toPx(), center.y + 28.dp.toPx())
                    lineTo(center.x + 20.dp.toPx(), center.y + 0.dp.toPx())
                    lineTo(center.x + 5.dp.toPx(), center.y + 0.dp.toPx())
                    close()
                }
                drawPath(path = flashPath, color = accentColor)
            }
            else -> {
                // General Cloud with outline
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = radius * 1.2f,
                    center = center
                )
                drawCircle(
                    color = accentColor.copy(alpha = 0.8f),
                    radius = radius * 1.0f,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }
    }
}

/**
 * Text utility parsing string times like "2026-05-26T15:00" to "3:00 PM"
 */
fun formatIsoTimeToHour(isoTime: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
        val formatter = SimpleDateFormat("h a", Locale.US)
        val date = parser.parse(isoTime)
        if (date != null) formatter.format(date) else ""
    } catch (e: Exception) {
         ""
    }
}

/**
 * Text utility parsing string times like "2026-05-26" to "Tomorrow" or "Tuesday"
 */
fun formatIsoTimeToDayName(isoDate: String, index: Int): String {
    if (index == 0) return "Today"
    if (index == 1) return "Tomorrow"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("EEEE", Locale.US)
        val date = parser.parse(isoDate)
        if (date != null) formatter.format(date) else ""
    } catch (e: Exception) {
        ""
    }
}

fun getWeatherDetails(code: Int): Pair<String, ImageVector> {
    return when (code) {
        0 -> "Clear Sky" to Icons.Default.WbSunny
        1, 2, 3 -> "Partly Cloudy" to Icons.Default.Cloud
        45, 48 -> "Foggy Mist" to Icons.Default.Warning
        51, 53, 55 -> "Light Rain" to Icons.Default.Water
        61, 63 -> "Showers" to Icons.Default.Water
        65 -> "Heavy Rain" to Icons.Default.Warning
        71, 73, 75 -> "Snowfall" to Icons.Default.Warning
        80, 81, 82 -> "Rain Showers" to Icons.Default.Warning
        95, 96, 99 -> "Thunderstorms" to Icons.Default.Warning
        else -> "Mild Conditions" to Icons.Default.Cloud
    }
}

@Composable
fun QuickGlancePromoCard() {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable {
                Toast.makeText(context, "Place the WeatherFlow widget on your Home screen for quick updates!", Toast.LENGTH_LONG).show()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF49454F))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFD0BCFF))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = "Widget icon decoration",
                    tint = Color(0xFF381E72),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Quick Glance Widget",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Tap to learn how to add this view to your home screen.",
                    fontSize = 11.sp,
                    color = Color(0xFFCAC4D0)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Chevron navigation indicator",
                tint = Color(0xFFCAC4D0),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun WeatherBottomNavigation(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    Surface(
        color = Color(0xFF1C1B1F),
        border = BorderStroke(1.dp, Color(0xFF49454F)),
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home option (Active)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        Toast.makeText(context, "Home forecast view active", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFFE8DEF8))
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home menu navigation key",
                        tint = Color(0xFF1D192B),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Home",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6E1E5)
                )
            }

            // 10-Day option
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        Toast.makeText(context, "10-Day forecast outlook synchronized!", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "10 day calendar forecast list icon",
                    tint = Color(0xFFCAC4D0),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "10-Day",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFCAC4D0)
                )
            }

            // Alerts option
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        Toast.makeText(context, "Severe environmental hazard monitoring is active.", Toast.LENGTH_LONG).show()
                    }
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Incoming weather alert inbox key",
                    tint = Color(0xFFCAC4D0),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Alerts",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFCAC4D0)
                )
            }

            // Profile option
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        Toast.makeText(context, "Personal settings and dynamic themes loaded.", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User profile account key",
                    tint = Color(0xFFCAC4D0),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Profile",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFCAC4D0)
                )
            }
        }
    }
}
