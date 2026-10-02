package com.example.focus4.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush as UiBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.BluePrimary

data class ProfessionalAvatar(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val bgColor: Color
)

object AvatarCatalog {
    val CATEGORIES = listOf(
        "Todas",
        "Ingeniería",
        "Salud",
        "Ciencias",
        "Derecho",
        "Negocios",
        "Diseño",
        "Estudiante"
    )

    val AVATARS = listOf(
        // INGENIERÍA Y TECNOLOGÍA
        ProfessionalAvatar(
            id = "engineering_01",
            name = "Software y Datos",
            category = "Ingeniería",
            description = "Sistemas, Algoritmos, IA y Computación",
            icon = Icons.Default.Computer,
            accentColor = Color(0xFF2563EB),
            bgColor = Color(0xFFEFF6FF)
        ),
        ProfessionalAvatar(
            id = "engineering_02",
            name = "Industrial y Procesos",
            category = "Ingeniería",
            description = "Optimización, Planta y Mecánica",
            icon = Icons.Default.Settings,
            accentColor = Color(0xFFEA580C),
            bgColor = Color(0xFFFFF7ED)
        ),
        ProfessionalAvatar(
            id = "engineering_03",
            name = "Civil y Estructuras",
            category = "Ingeniería",
            description = "Obras, Puentes, Geotecnia y Construcción",
            icon = Icons.Default.Construction,
            accentColor = Color(0xFFD97706),
            bgColor = Color(0xFFFEF3C7)
        ),
        ProfessionalAvatar(
            id = "engineering_04",
            name = "Electrónica y Robótica",
            category = "Ingeniería",
            description = "Circuitos, Embebidos y Telecomunicaciones",
            icon = Icons.Default.Memory,
            accentColor = Color(0xFF7C3AED),
            bgColor = Color(0xFFF5F3FF)
        ),

        // MEDICINA Y CIENCIAS DE LA SALUD
        ProfessionalAvatar(
            id = "medicine_01",
            name = "Medicina Clínica",
            category = "Salud",
            description = "Diagnóstico, Anatomía y Práctica Hospitalaria",
            icon = Icons.Default.MedicalServices,
            accentColor = Color(0xFFDC2626),
            bgColor = Color(0xFFFEF2F2)
        ),
        ProfessionalAvatar(
            id = "medicine_02",
            name = "Enfermería y Cuidados",
            category = "Salud",
            description = "Atención al Paciente y Terapias",
            icon = Icons.Default.Favorite,
            accentColor = Color(0xFFE11D48),
            bgColor = Color(0xFFFFF1F2)
        ),
        ProfessionalAvatar(
            id = "medicine_03",
            name = "Bioquímica y Farmacia",
            category = "Salud",
            description = "Farmacología, Genética y Laboratorio",
            icon = Icons.Default.Biotech,
            accentColor = Color(0xFF059669),
            bgColor = Color(0xFFECFDF5)
        ),
        ProfessionalAvatar(
            id = "medicine_04",
            name = "Odontología",
            category = "Salud",
            description = "Salud Bucal, Ortodoncia y Cirugía",
            icon = Icons.Default.HealthAndSafety,
            accentColor = Color(0xFF0284C7),
            bgColor = Color(0xFFF0F9FF)
        ),
        ProfessionalAvatar(
            id = "medicine_05",
            name = "Psicología y Neurociencias",
            category = "Salud",
            description = "Cognición, Salud Mental y Comportamiento",
            icon = Icons.Default.Psychology,
            accentColor = Color(0xFF9333EA),
            bgColor = Color(0xFFFAF5FF)
        ),

        // CIENCIAS EXACTAS Y NATURALES
        ProfessionalAvatar(
            id = "science_01",
            name = "Matemáticas y Física",
            category = "Ciencias",
            description = "Álgebra, Modelado Cuántico y Cálculo",
            icon = Icons.Default.Calculate,
            accentColor = Color(0xFF3B82F6),
            bgColor = Color(0xFFEFF6FF)
        ),
        ProfessionalAvatar(
            id = "science_02",
            name = "Química Pura",
            category = "Ciencias",
            description = "Reacciones, Ensayos y Química Orgánica",
            icon = Icons.Default.Science,
            accentColor = Color(0xFF0D9488),
            bgColor = Color(0xFFF0FDFA)
        ),
        ProfessionalAvatar(
            id = "science_03",
            name = "Biología y Ambiente",
            category = "Ciencias",
            description = "Ecología, Botánica y Biodiversidad",
            icon = Icons.Default.Spa,
            accentColor = Color(0xFF16A34A),
            bgColor = Color(0xFFF0FDF4)
        ),

        // DERECHO Y CIENCIAS SOCIALES
        ProfessionalAvatar(
            id = "law_01",
            name = "Derecho y Jurídica",
            category = "Derecho",
            description = "Leyes, Juicios, Jurisprudencia y Códigos",
            icon = Icons.Default.Gavel,
            accentColor = Color(0xFFB45309),
            bgColor = Color(0xFFFFFBEB)
        ),
        ProfessionalAvatar(
            id = "law_02",
            name = "Relaciones y Diplomacia",
            category = "Derecho",
            description = "Geopolítica, Tratados y Comercio Exterior",
            icon = Icons.Default.Language,
            accentColor = Color(0xFF0284C7),
            bgColor = Color(0xFFF0F9FF)
        ),

        // ECONOMÍA, FINANZAS Y NEGOCIOS
        ProfessionalAvatar(
            id = "business_01",
            name = "Administración y Negocios",
            category = "Negocios",
            description = "Liderazgo, Startups y Estrategia",
            icon = Icons.Default.RocketLaunch,
            accentColor = Color(0xFF059669),
            bgColor = Color(0xFFECFDF5)
        ),
        ProfessionalAvatar(
            id = "business_02",
            name = "Economía y Finanzas",
            category = "Negocios",
            description = "Mercados, Inversiones y Macroeconomía",
            icon = Icons.Default.AttachMoney,
            accentColor = Color(0xFF15803D),
            bgColor = Color(0xFFF0FDF4)
        ),
        ProfessionalAvatar(
            id = "business_03",
            name = "Auditoría y Contabilidad",
            category = "Negocios",
            description = "Balances, Tributación y Análisis de Costos",
            icon = Icons.Default.Insights,
            accentColor = Color(0xFF4338CA),
            bgColor = Color(0xFFEEF2FF)
        ),

        // DISEÑO Y ARQUITECTURA
        ProfessionalAvatar(
            id = "architecture_01",
            name = "Arquitectura y Urbanismo",
            category = "Diseño",
            description = "Planos, Espacios y Diseño Constructivo",
            icon = Icons.Default.HomeWork,
            accentColor = Color(0xFF475569),
            bgColor = Color(0xFFF1F5F9)
        ),
        ProfessionalAvatar(
            id = "architecture_02",
            name = "Diseño Visual y Creativo",
            category = "Diseño",
            description = "Identidad Visual, Ilustración y UI/UX",
            icon = Icons.Default.Palette,
            accentColor = Color(0xFFD946EF),
            bgColor = Color(0xFFFDF4FF)
        ),
        ProfessionalAvatar(
            id = "architecture_03",
            name = "Comunicación y Medios",
            category = "Diseño",
            description = "Cine, Edición y Comunicación Social",
            icon = Icons.Default.Movie,
            accentColor = Color(0xFFE11D48),
            bgColor = Color(0xFFFFF1F2)
        ),

        // ESTUDIANTE UNIVERSITARIO GENERAL
        ProfessionalAvatar(
            id = "student_01",
            name = "Estudiante Universitario",
            category = "Estudiante",
            description = "Foco diario, apuntes y constancia",
            icon = Icons.Default.School,
            accentColor = Color(0xFF2563EB),
            bgColor = Color(0xFFEFF6FF)
        ),
        ProfessionalAvatar(
            id = "student_02",
            name = "Investigador / Tesista",
            category = "Estudiante",
            description = "Lectura profunda, papers y tesis",
            icon = Icons.Default.MenuBook,
            accentColor = Color(0xFF4F46E5),
            bgColor = Color(0xFFEEF2FF)
        ),
        ProfessionalAvatar(
            id = "student_03",
            name = "Foco Nocturno",
            category = "Estudiante",
            description = "Sesiones intensivas de estudio concentrado",
            icon = Icons.Default.NightsStay,
            accentColor = Color(0xFF7C3AED),
            bgColor = Color(0xFFF5F3FF)
        ),
        ProfessionalAvatar(
            id = "student_04",
            name = "Graduación y Excelencia",
            category = "Estudiante",
            description = "Promedio destacado y metas cumplidas",
            icon = Icons.Default.EmojiEvents,
            accentColor = Color(0xFFCA8A04),
            bgColor = Color(0xFFFEFCE8)
        )
    )

    fun getById(id: String?): ProfessionalAvatar {
        if (id.isNullOrBlank()) return AVATARS.first { it.id == "student_01" }
        return AVATARS.find { it.id == id } ?: AVATARS.first { it.id == "student_01" }
    }
}

@Composable
fun UserAvatar(
    avatarId: String?,
    profileImageUri: String? = null,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    showEditBadge: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val avatar = remember(avatarId) { AvatarCatalog.getById(avatarId) }
    val hasValidCustomPhoto = !profileImageUri.isNullOrBlank()

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (hasValidCustomPhoto) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(profileImageUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
            )
        } else {
            // Professional Vector Avatar Badge
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(avatar.bgColor)
                    .border(2.dp, avatar.accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = avatar.icon,
                    contentDescription = avatar.name,
                    tint = avatar.accentColor,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }

        if (showEditBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size((size * 0.36f).coerceAtLeast(20.dp))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cambiar avatar o foto",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size((size * 0.22f).coerceAtLeast(12.dp))
                )
            }
        }
    }
}

@Composable
fun AvatarSelectionDialog(
    currentAvatarId: String,
    onAvatarSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Todas") }
    var tempSelectedId by remember { mutableStateOf(currentAvatarId) }

    val filteredAvatars = remember(selectedCategory) {
        if (selectedCategory == "Todas") {
            AvatarCatalog.AVATARS
        } else {
            AvatarCatalog.AVATARS.filter { it.category == selectedCategory }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Avatares Profesionales",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Elige la identidad que mejor represente tu carrera y disciplina",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                // Category Selector Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AvatarCatalog.CATEGORIES) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Avatar Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredAvatars) { avatar ->
                        val isSelected = tempSelectedId == avatar.id
                        Card(
                            onClick = { tempSelectedId = avatar.id },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    avatar.bgColor
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = if (isSelected) {
                                CardDefaults.outlinedCardBorder().copy(
                                    width = 2.dp,
                                    brush = UiBrush.linearGradient(
                                        listOf(avatar.accentColor, avatar.accentColor)
                                    )
                                )
                            } else {
                                CardDefaults.outlinedCardBorder().copy(width = 0.8.dp)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("avatar_card_${avatar.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(avatar.bgColor)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = avatar.accentColor,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = avatar.icon,
                                        contentDescription = avatar.name,
                                        tint = avatar.accentColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = avatar.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) avatar.accentColor else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAvatarSelected(tempSelectedId)
                    onDismiss()
                }
            ) {
                Text("Confirmar Avatar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
