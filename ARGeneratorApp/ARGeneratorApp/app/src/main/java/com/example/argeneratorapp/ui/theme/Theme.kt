package com.example.argeneratorapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Define your color palette
private val Green = Color(0xFF4CAF50) // Primary color
private val Yellow = Color(0xFFFFEB3B) // Secondary color
private val LightGreen = Color(0xFFF0F4C3) // Light background

private val DarkColorScheme = darkColorScheme(
    primary = Green,
    secondary = Yellow,
    tertiary = Color(0xFFE91E63) // Add a tertiary color if needed
)

private val LightColorScheme = lightColorScheme(
    primary = Green,
    secondary = Yellow,
    tertiary = Color(0xFFE91E63), // Add a tertiary color if needed
    background = LightGreen, // Light background color
    surface = Color.White, // Surface color
    onPrimary = Color.White, // Text color on primary buttons
    onSecondary = Color.Black, // Text color on secondary buttons
    onBackground = Color.Black, // Text color on background
    onSurface = Color.Black // Text color on surfaces
)

@Composable
fun ARGeneratorAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
