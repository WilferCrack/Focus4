package com.example.focus4.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focus4.ui.Focus4ViewModel
import com.example.ui.theme.ThemeCatalog
import com.example.ui.theme.ThemeEngine

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppearanceScreen(
    viewModel: Focus4ViewModel,
    onBackClick: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()

    var selectedMode by remember { mutableStateOf(user?.themeMode ?: "system") }
    var selectedPreset by remember { mutableStateOf(user?.themePreset ?: "FOCUS4") }
    var lightColorHex by remember { mutableStateOf(user?.lightThemeColor ?: "#2563EB") }
    var darkColorHex by remember { mutableStateOf(user?.darkThemeColor ?: "#7C3AED") }
    var useDynamicColor by remember { mutableStateOf(user?.useDynamicColor ?: false) }

    LaunchedEffect(user) {
        user?.let {
            selectedMode = it.themeMode
            selectedPreset = it.themePreset
            lightColorHex = it.lightThemeColor
            darkColorHex = it.darkThemeColor
            useDynamicColor = it.useDynamicColor
        }
    }

    val quickColors = listOf(
        "#2563EB", "#0284C7", "#059669", "#15803D",
        "#4338CA", "#7C3AED", "#9333EA", "#E11D48",
        "#EA580C", "#D97706", "#991B1B", "#475569"
    )

    val previewLightColor = remember(lightColorHex) {
        ThemeEngine.parseColor(lightColorHex, Color(0xFF2563EB))
    }
    val previewDarkColor = remember(darkColorHex) {
        ThemeEngine.parseColor(darkColorHex, Color(0xFF7C3AED))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "APARIENCIA Y PERSONALIZACIÓN",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Personaliza el modo visual, paletas de color y contrastes para el día y la noche",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. MODO DE COLOR: Claro / Oscuro / Seguir sistema
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MODO DE COLOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Claro
                        ThemeModeCard(
                            title = "Claro",
                            icon = Icons.Default.LightMode,
                            isSelected = selectedMode == "light",
                            onClick = { selectedMode = "light" },
                            modifier = Modifier.weight(1f)
                        )
                        // Oscuro
                        ThemeModeCard(
                            title = "Oscuro",
                            icon = Icons.Default.DarkMode,
                            isSelected = selectedMode == "dark",
                            onClick = { selectedMode = "dark" },
                            modifier = Modifier.weight(1f)
                        )
                        // Sistema
                        ThemeModeCard(
                            title = "Sistema",
                            icon = Icons.Default.SettingsBrightness,
                            isSelected = selectedMode == "system",
                            onClick = { selectedMode = "system" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. TEMAS PREDEFINIDOS (PRESETS)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PALETAS Y TEMAS PREDEFINIDOS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ThemeCatalog.PRESETS.forEach { preset ->
                            val isSelected = selectedPreset == preset.id
                            val lightP = ThemeEngine.parseColor(preset.lightPrimaryHex)
                            val darkP = ThemeEngine.parseColor(preset.darkPrimaryHex)

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPreset = preset.id
                                    lightColorHex = preset.lightPrimaryHex
                                    darkColorHex = preset.darkPrimaryHex
                                },
                                label = { Text(preset.name, fontSize = 12.sp) },
                                leadingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(lightP)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(darkP)
                                        )
                                    }
                                }
                            )
                        }

                        FilterChip(
                            selected = selectedPreset == "CUSTOM",
                            onClick = { selectedPreset = "CUSTOM" },
                            label = { Text("Personalizado", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        )
                    }
                }
            }
        }

        // 3. COLOR PERSONALIZADO INDEPENDIENTE PARA MODO CLARO Y OSCURO
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "COLORES PRINCIPALES INDEPENDIENTES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Color modo claro
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(previewLightColor)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Color Principal Modo Claro:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickColors.forEach { hex ->
                                val color = ThemeEngine.parseColor(hex)
                                val isChosen = lightColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isChosen) 2.5.dp else 1.dp,
                                            color = if (isChosen) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            lightColorHex = hex
                                            selectedPreset = "CUSTOM"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = lightColorHex,
                            onValueChange = {
                                lightColorHex = it
                                selectedPreset = "CUSTOM"
                            },
                            label = { Text("Código Hexadecimal Claro (ej. #2563EB)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Color modo oscuro
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(previewDarkColor)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Color Principal Modo Oscuro:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickColors.forEach { hex ->
                                val color = ThemeEngine.parseColor(hex)
                                val isChosen = darkColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isChosen) 2.5.dp else 1.dp,
                                            color = if (isChosen) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            darkColorHex = hex
                                            selectedPreset = "CUSTOM"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = darkColorHex,
                            onValueChange = {
                                darkColorHex = it
                                selectedPreset = "CUSTOM"
                            },
                            label = { Text("Código Hexadecimal Oscuro (ej. #7C3AED)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 4. COLORES DEL DISPOSITIVO (DYNAMIC COLOR ANDROID 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Colores Dinámicos del Dispositivo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Extraer tonalidades del fondo de pantalla (Material You)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useDynamicColor,
                            onCheckedChange = { useDynamicColor = it }
                        )
                    }
                }
            }
        }

        // 5. VISTA PREVIA INTERACTIVA (LIVE PREVIEW)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMode == "dark") Color(0xFF0F172A) else Color(0xFFF8FAFC)
                ),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                val previewActiveColor = if (selectedMode == "dark") previewDarkColor else previewLightColor
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VISTA PREVIA EN VIVO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = previewActiveColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedMode == "dark") Color(0xFF1E293B) else Color.White,
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(previewActiveColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.School,
                                        contentDescription = null,
                                        tint = previewActiveColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Próxima Clase: Algoritmos y Datos",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (selectedMode == "dark") Color.White else Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "15:00 - 17:00 • Aula Magna 3",
                                        fontSize = 11.sp,
                                        color = if (selectedMode == "dark") Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {},
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = previewActiveColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Iniciar Sesión de Foco", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 6. ACTION BUTTONS: APLICAR & RESTAURAR
        item {
            Button(
                onClick = {
                    viewModel.updateAppearance(
                        themeMode = selectedMode,
                        lightColor = lightColorHex,
                        darkColor = darkColorHex,
                        preset = selectedPreset,
                        useDynamic = useDynamicColor,
                        onSuccess = { onBackClick() }
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("apply_theme_button")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aplicar Preferencias Visuales", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    selectedMode = "system"
                    selectedPreset = "FOCUS4"
                    lightColorHex = "#2563EB"
                    darkColorHex = "#7C3AED"
                    useDynamicColor = false
                    viewModel.updateAppearance(
                        themeMode = "system",
                        lightColor = "#2563EB",
                        darkColor = "#7C3AED",
                        preset = "FOCUS4",
                        useDynamic = false
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_theme_button")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restaurar Tema Predeterminado")
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ThemeModeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(width = 2.dp, brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary))
        } else {
            CardDefaults.outlinedCardBorder().copy(width = 0.8.dp)
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
