package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueDeep,
    onPrimaryContainer = Color.White,
    secondary = BlueSecondary,
    onSecondary = Navy950,
    secondaryContainer = Navy800,
    onSecondaryContainer = BlueSecondary,
    tertiary = StatusPurple,
    onTertiary = Color.White,
    background = Navy950,
    onBackground = Color(0xFFF1F5F9),
    surface = Navy900,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Navy800,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Navy700,
    error = StatusDanger,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = BlueDeep,
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = StatusPurple,
    onTertiary = Color.White,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Color(0xFF64748B),
    outline = Slate200,
    error = StatusDanger,
    onError = Color.White
)

@Composable
fun Focus4Theme(
    themeMode: String = "system",
    lightThemeColor: String = "#2563EB",
    darkThemeColor: String = "#7C3AED",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemInDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> {
            val primary = ThemeEngine.parseColor(darkThemeColor, fallback = Color(0xFF3B82F6))
            ThemeEngine.buildDarkColorScheme(primary)
        }
        else -> {
            val primary = ThemeEngine.parseColor(lightThemeColor, fallback = Color(0xFF2563EB))
            ThemeEngine.buildLightColorScheme(primary)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
