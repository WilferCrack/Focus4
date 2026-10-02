package com.example.focus4.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.focus4.notifications.NotificationHelper
import com.example.focus4.ui.Focus4ViewModel
import com.example.focus4.ui.components.AvatarCatalog
import com.example.focus4.ui.components.AvatarSelectionDialog
import com.example.focus4.ui.components.UserAvatar
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: Focus4ViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val exams by viewModel.exams.collectAsState()

    // Form states
    var displayName by remember { mutableStateOf(user?.getEffectiveDisplayName() ?: "") }
    var career by remember { mutableStateOf(user?.career ?: "") }
    var semester by remember { mutableStateOf(user?.semester ?: "1° Semestre") }
    var selectedAvatarId by remember { mutableStateOf(user?.avatarId ?: "student_01") }
    var dailyHours by remember { mutableFloatStateOf(user?.dailyAvailableHours ?: 2.0f) }
    var sessionMinutes by remember { mutableIntStateOf(user?.preferredSessionMinutes ?: 25) }
    var mainGoal by remember { mutableStateOf(user?.mainGoal ?: "") }

    var displayNameError by remember { mutableStateOf<String?>(null) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showImageOptionsDialog by remember { mutableStateOf(false) }
    var semesterMenuExpanded by remember { mutableStateOf(false) }

    // Synchronize form if user changes
    LaunchedEffect(user) {
        user?.let {
            displayName = it.getEffectiveDisplayName()
            career = it.career
            semester = it.semester
            selectedAvatarId = it.avatarId
            dailyHours = it.dailyAvailableHours
            sessionMinutes = it.preferredSessionMinutes
            mainGoal = it.mainGoal
        }
    }

    // Photo picker launcher (Android standard zero-permission Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.saveCustomProfilePicture(uri)
        }
    }

    // Notification permission launcher
    var hasNotificationPermission by remember {
        mutableStateOf(NotificationHelper.hasNotificationPermission(context))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            NotificationHelper.createNotificationChannels(context)
        }
    }

    val semesterOptions = listOf(
        "1° Semestre", "2° Semestre", "3° Semestre", "4° Semestre",
        "5° Semestre", "6° Semestre", "7° Semestre", "8° Semestre",
        "9° Semestre", "10° Semestre", "Tesis / Posgrado", "Graduado"
    )

    val currentAvatar = remember(selectedAvatarId) { AvatarCatalog.getById(selectedAvatarId) }

    // Calculate real stats
    val totalMinutes = remember(allSessions) { allSessions.sumOf { it.actualMinutes } }
    val totalHours = remember(totalMinutes) { (totalMinutes / 60.0 * 10).roundToInt() / 10.0 }
    val completedSessions = remember(allSessions) { allSessions.count { it.status == "completed" } }
    val activeSubjectsCount = remember(subjects) { subjects.count { it.active } }
    val upcomingExamsCount = remember(exams) {
        val now = System.currentTimeMillis()
        exams.count { it.examDate >= now }
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

            // HERO CARD: User Avatar, Identity & Quick Photo Actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.clickable { showImageOptionsDialog = true }
                    ) {
                        UserAvatar(
                            avatarId = selectedAvatarId,
                            profileImageUri = user?.profileImageUri,
                            size = 96.dp,
                            showEditBadge = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = displayName.ifBlank { "Estudiante" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = currentAvatar.bgColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = currentAvatar.icon,
                                    contentDescription = null,
                                    tint = currentAvatar.accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentAvatar.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = currentAvatar.accentColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = semester,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAvatarPicker = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_avatar_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Avatares", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_photo_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mi Foto", fontSize = 12.sp)
                        }
                    }

                    if (!user?.profileImageUri.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = { viewModel.removeCustomProfilePicture() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = StatusDanger,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Quitar foto y usar avatar profesional",
                                color = StatusDanger,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // SECTION: EDITABLE IDENTITY
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
                        text = "IDENTIDAD VISIBLE Y ACADÉMICA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = BluePrimary
                    )

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = {
                            displayName = it
                            displayNameError = if (it.trim().isEmpty()) {
                                "El nombre no puede estar vacío"
                            } else null
                        },
                        label = { Text("Nombre Visible (Mostrado en la App)") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        isError = displayNameError != null,
                        supportingText = {
                            if (displayNameError != null) {
                                Text(displayNameError!!, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Puedes personalizar tu nombre sin cambiar tu cuenta de acceso.")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("display_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = career,
                        onValueChange = { career = it },
                        label = { Text("Carrera Universitaria / Disciplina") },
                        leadingIcon = {
                            Icon(Icons.Default.School, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("career_input"),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = semesterMenuExpanded,
                        onExpandedChange = { semesterMenuExpanded = !semesterMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = semester,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Semestre / Nivel Actual") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = semesterMenuExpanded,
                            onDismissRequest = { semesterMenuExpanded = false }
                        ) {
                            semesterOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        semester = opt
                                        semesterMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Read-only login credential info
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Cuenta de Acceso (Inmutable)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = user?.email ?: "usuario@email.com",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // SECTION: ACADEMIC PREFERENCES & HABITS
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
                        text = "PREFERENCIAS DE ESTUDIO Y FOCO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = BluePrimary
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Horas diarias disponibles:")
                            Text(
                                "${String.format("%.1f", dailyHours)} horas/día",
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        }
                        Slider(
                            value = dailyHours,
                            onValueChange = { dailyHours = it },
                            valueRange = 0.5f..12.0f,
                            steps = 22,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column {
                        Text("Duración de bloque preferido:")
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 25, 45, 60, 90).forEach { mins ->
                                FilterChip(
                                    selected = sessionMinutes == mins,
                                    onClick = { sessionMinutes = mins },
                                    label = { Text("$mins min") },
                                    leadingIcon = if (sessionMinutes == mins) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = mainGoal,
                        onValueChange = { mainGoal = it },
                        label = { Text("Objetivo Académico Principal") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        }

        // SECTION: REAL ACTIVITY STATISTICS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ESTADÍSTICAS REALES DE ACTIVIDAD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = BluePrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatCard(
                            title = "Horas Estudiadas",
                            value = "$totalHours h",
                            subtitle = "$totalMinutes minutos",
                            color = BluePrimary,
                            icon = Icons.Default.Timer,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatCard(
                            title = "Sesiones",
                            value = "$completedSessions",
                            subtitle = "completadas",
                            color = StatusSuccess,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatCard(
                            title = "Materias Activas",
                            value = "$activeSubjectsCount",
                            subtitle = "en curso",
                            color = StatusPurple,
                            icon = Icons.Default.School,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatCard(
                            title = "Exámenes",
                            value = "$upcomingExamsCount",
                            subtitle = "programados",
                            color = StatusWarning,
                            icon = Icons.Default.CalendarMonth,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // SECTION: NOTIFICATIONS & LOCAL ALERTS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasNotificationPermission) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (hasNotificationPermission) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NOTIFICACIONES LOCALES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        }

                        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                            ) {
                                Text("Activar", fontSize = 11.sp)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StatusSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Permiso concedido",
                                    fontSize = 11.sp,
                                    color = StatusSuccess,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "FOCUS4 envía alertas automáticas cuando completas un bloque de foco y recordatorios de proximidad de exámenes.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.sendTestNotification("session") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Aviso de Sesión", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.sendTestNotification("exam") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Alerta Examen", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // SAVE BUTTON
        item {
            Button(
                onClick = {
                    if (displayName.trim().isEmpty()) {
                        displayNameError = "El nombre no puede estar vacío"
                        return@Button
                    }
                    viewModel.updateProfile(
                        displayName = displayName,
                        career = career,
                        semester = semester,
                        avatarId = selectedAvatarId,
                        dailyAvailableHours = dailyHours,
                        preferredSessionMinutes = sessionMinutes,
                        mainGoal = mainGoal,
                        onSuccess = { onBackClick() }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Cambios del Perfil", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Avatar Selection Dialog
    if (showAvatarPicker) {
        AvatarSelectionDialog(
            currentAvatarId = selectedAvatarId,
            onAvatarSelected = { avatarId ->
                selectedAvatarId = avatarId
                // If user selected an avatar, ask if they want to apply right now
                viewModel.updateProfile(
                    displayName = displayName,
                    career = career,
                    semester = semester,
                    avatarId = avatarId,
                    dailyAvailableHours = dailyHours,
                    preferredSessionMinutes = sessionMinutes,
                    mainGoal = mainGoal
                )
            },
            onDismiss = { showAvatarPicker = false }
        )
    }

    // Options Dialog for Photo/Avatar
    if (showImageOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showImageOptionsDialog = false },
            title = { Text("Foto y Avatar", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Elige cómo deseas presentarte en FOCUS4:", fontSize = 13.sp)

                    Button(
                        onClick = {
                            showImageOptionsDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subir foto personal de la galería")
                    }

                    OutlinedButton(
                        onClick = {
                            showImageOptionsDialog = false
                            showAvatarPicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Explorar avatares profesionales")
                    }

                    if (!user?.profileImageUri.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                showImageOptionsDialog = false
                                viewModel.removeCustomProfilePicture()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Eliminar foto y volver al avatar", color = StatusDanger)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageOptionsDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun ProfileStatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(width = 0.8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
