package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

data class ThemePreset(
    val id: String,
    val name: String,
    val description: String,
    val lightPrimaryHex: String,
    val darkPrimaryHex: String
)

object ThemeCatalog {
    val PRESETS = listOf(
        ThemePreset(
            id = "FOCUS4",
            name = "FOCUS4 Original",
            description = "Azul universitario vibrante y nítido",
            lightPrimaryHex = "#2563EB",
            darkPrimaryHex = "#3B82F6"
        ),
        ThemePreset(
            id = "OCEAN",
            name = "Ocean",
            description = "Turquesa y cian de profundidad marina",
            lightPrimaryHex = "#0284C7",
            darkPrimaryHex = "#38BDF8"
        ),
        ThemePreset(
            id = "FOREST",
            name = "Forest",
            description = "Esmeralda y verde botánico relajante",
            lightPrimaryHex = "#059669",
            darkPrimaryHex = "#34D399"
        ),
        ThemePreset(
            id = "MIDNIGHT",
            name = "Midnight",
            description = "Índigo profundo y azul medianoche",
            lightPrimaryHex = "#4338CA",
            darkPrimaryHex = "#818CF8"
        ),
        ThemePreset(
            id = "SAKURA",
            name = "Sakura",
            description = "Rosa cerezo y carmín elegante",
            lightPrimaryHex = "#E11D48",
            darkPrimaryHex = "#FB7185"
        ),
        ThemePreset(
            id = "SUNSET",
            name = "Sunset",
            description = "Ámbar y naranja de atardecer",
            lightPrimaryHex = "#EA580C",
            darkPrimaryHex = "#FB923C"
        ),
        ThemePreset(
            id = "PURPLE",
            name = "Purple",
            description = "Púrpura real y violeta de alta concentración",
            lightPrimaryHex = "#9333EA",
            darkPrimaryHex = "#C084FC"
        ),
        ThemePreset(
            id = "ACADEMIC",
            name = "Academic",
            description = "Rojo carmesí y bordó sobrio tradicional",
            lightPrimaryHex = "#991B1B",
            darkPrimaryHex = "#F87171"
        ),
        ThemePreset(
            id = "MINIMAL",
            name = "Minimal",
            description = "Gris pizarra neutro y arquitectura sobria",
            lightPrimaryHex = "#475569",
            darkPrimaryHex = "#94A3B8"
        )
    )

    fun getPresetById(id: String): ThemePreset {
        return PRESETS.find { it.id == id } ?: PRESETS.first()
    }
}

object ThemeEngine {

    fun parseColor(hex: String, fallback: Color = Color(0xFF2563EB)): Color {
        return try {
            val cleanHex = hex.trim().removePrefix("#")
            when (cleanHex.length) {
                6 -> {
                    val r = cleanHex.substring(0, 2).toInt(16)
                    val g = cleanHex.substring(2, 4).toInt(16)
                    val b = cleanHex.substring(4, 6).toInt(16)
                    Color(r, g, b)
                }
                8 -> {
                    val a = cleanHex.substring(0, 2).toInt(16)
                    val r = cleanHex.substring(2, 4).toInt(16)
                    val g = cleanHex.substring(4, 6).toInt(16)
                    val b = cleanHex.substring(6, 8).toInt(16)
                    Color(r, g, b, a)
                }
                else -> fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }

    private fun getPerceivedLuminance(color: Color): Float {
        return (0.299f * color.red) + (0.587f * color.green) + (0.114f * color.blue)
    }

    fun buildLightColorScheme(primaryColor: Color): ColorScheme {
        val lum = getPerceivedLuminance(primaryColor)
        val onPrimary = if (lum > 0.58f) Color(0xFF0F172A) else Color.White

        val primaryContainer = Color(
            red = (primaryColor.red * 0.18f + Slate50.red * 0.82f).coerceIn(0f, 1f),
            green = (primaryColor.green * 0.18f + Slate50.green * 0.82f).coerceIn(0f, 1f),
            blue = (primaryColor.blue * 0.18f + Slate50.blue * 0.82f).coerceIn(0f, 1f)
        )

        val onPrimaryContainer = Color(
            red = (primaryColor.red * 0.6f).coerceIn(0f, 1f),
            green = (primaryColor.green * 0.6f).coerceIn(0f, 1f),
            blue = (primaryColor.blue * 0.6f).coerceIn(0f, 1f)
        )

        return lightColorScheme(
            primary = primaryColor,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
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
    }

    fun buildDarkColorScheme(primaryColor: Color): ColorScheme {
        val lum = getPerceivedLuminance(primaryColor)
        val onPrimary = if (lum > 0.65f) Navy950 else Color.White

        val primaryContainer = Color(
            red = (primaryColor.red * 0.35f + Navy900.red * 0.65f).coerceIn(0f, 1f),
            green = (primaryColor.green * 0.35f + Navy900.green * 0.65f).coerceIn(0f, 1f),
            blue = (primaryColor.blue * 0.35f + Navy900.blue * 0.65f).coerceIn(0f, 1f)
        )

        val onPrimaryContainer = Color(
            red = (primaryColor.red * 0.85f + 0.15f).coerceIn(0f, 1f),
            green = (primaryColor.green * 0.85f + 0.15f).coerceIn(0f, 1f),
            blue = (primaryColor.blue * 0.85f + 0.15f).coerceIn(0f, 1f)
        )

        return darkColorScheme(
            primary = primaryColor,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
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
    }
}
