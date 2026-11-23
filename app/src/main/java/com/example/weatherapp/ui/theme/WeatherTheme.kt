package com.example.weatherapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(primary = Color(0xFF2563EB), secondary = Color(0xFF60A5FA), background = Color(0xFFF8FAFC), surface = Color.White, onBackground = Color(0xFF1E293B), onSurface = Color(0xFF1E293B))
private val Dark = darkColorScheme(primary = Color(0xFF60A5FA), secondary = Color(0xFF93C5FD), background = Color(0xFF0F172A), surface = Color(0xFF193653))

@Composable fun WeatherTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Dark else Light, content = content)
}
