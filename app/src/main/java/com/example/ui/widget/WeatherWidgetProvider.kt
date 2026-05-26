package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.api.WeatherResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherWidgetProvider : AppWidgetProvider() {

    private val jobScope = CoroutineScope(Dispatchers.IO)
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val weatherAdapter = moshi.adapter(WeatherResponse::class.java)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        
        jobScope.launch {
            // Retrieve latest cache from database
            val db = AppDatabase.getDatabase(context)
            val latestCache = db.weatherDao().getWeatherCache().firstOrNull()
            
            for (widgetId in appWidgetIds) {
                updateWidgetView(context, appWidgetManager, widgetId, latestCache)
            }
        }
    }

    private fun updateWidgetView(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        cache: com.example.data.db.WeatherCache?
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget_layout)

        if (cache != null) {
            views.setTextViewText(R.id.widget_location, cache.cityName)
            
            try {
                val weather = weatherAdapter.fromJson(cache.weatherJson)
                val temp = weather?.current?.temperature
                val code = weather?.current?.weatherCode ?: 0
                
                if (temp != null) {
                    views.setTextViewText(R.id.widget_temp, String.format(Locale.getDefault(), "%.1f°C", temp))
                } else {
                    views.setTextViewText(R.id.widget_temp, "--°C")
                }
                
                views.setTextViewText(R.id.widget_condition, mapWeatherCode(code))
                
                val sdf = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault())
                val timeStr = "Synced: " + sdf.format(Date(cache.lastUpdated))
                views.setTextViewText(R.id.widget_sync_time, timeStr)
                
            } catch (e: Exception) {
                views.setTextViewText(R.id.widget_temp, "Error")
                views.setTextViewText(R.id.widget_condition, "Cache Corrupted")
            }
        } else {
            views.setTextViewText(R.id.widget_location, "No weather data")
            views.setTextViewText(R.id.widget_temp, "--°C")
            views.setTextViewText(R.id.widget_condition, "Open app to sync")
            views.setTextViewText(R.id.widget_sync_time, "Not synced")
        }

        // Tap to open main activity
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_location, pendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun mapWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Fog"
            51, 53, 55 -> "Light Drizzle"
            61, 63 -> "Light Rain"
            65 -> "Heavy Rain"
            71, 73, 75 -> "Snowfall"
            80, 81, 82 -> "Rain Showers"
            95, 96, 99 -> "Thunderstorm!"
            else -> "Mild Conditions"
        }
    }
}
