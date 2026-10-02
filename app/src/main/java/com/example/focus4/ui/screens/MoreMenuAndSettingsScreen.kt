package com.example.focus4.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focus4.ui.Focus4ViewModel
import com.example.focus4.ui.components.PriorityBadge
import com.example.focus4.ui.components.StatusBadge
import com.example.focus4.ui.components.UserAvatar
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.CheckCircle
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MoreMenuScreen(
    viewModel: Focus4ViewModel,
    onNavigateToSubjects: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val user by viewModel.currentUser.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // User Profile Card (Clickable to open ProfileScreen)
            Card(
                onClick = onNavigateToProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        avatarId = user?.avatarId,
                        profileImageUri = user?.profileImageUri,
                        size = 52.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.getEffectiveDisplayName() ?: "Estudiante",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${user?.career ?: "Carrera Universitaria"} • ${user?.semester ?: "1° Semestre"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Editar perfil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        item {
            Text(
                text = "IDENTIDAD Y PERFIL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            MenuNavigationItem(
                title = "Mi Perfil e Identidad",
                subtitle = "Avatares profesionales, foto y preferencias",
                icon = Icons.Default.Badge,
                iconColor = BluePrimary,
                onClick = onNavigateToProfile
            )
        }

        item {
            Text(
                text = "GESTIÓN ACADÉMICA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            MenuNavigationItem(
                title = "Materias y Temas",
                subtitle = "Organiza unidades, dificultades y contenidos",
                icon = Icons.Default.Book,
                iconColor = BluePrimary,
                onClick = onNavigateToSubjects
            )
        }

        item {
            MenuNavigationItem(
                title = "Exámenes y Fechas Límite",
                subtitle = "Parciales, finales y cuenta regresiva de días",
                icon = Icons.Default.DateRange,
                iconColor = StatusDanger,
                onClick = onNavigateToExams
            )
        }

        item {
            MenuNavigationItem(
                title = "Calendario Académico",
                subtitle = "Vista integrada de sesiones y evaluaciones",
                icon = Icons.Default.CalendarMonth,
                iconColor = Color(0xFF0284C7),
                onClick = onNavigateToCalendar
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "APRENDIZAJE ACTIVO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            MenuNavigationItem(
                title = "Flashcards de Repaso",
                subtitle = "Tarjetas interactivas de memorización activa",
                icon = Icons.Default.Style,
                iconColor = StatusSuccess,
                onClick = onNavigateToFlashcards
            )
        }

        item {
            MenuNavigationItem(
                title = "Simulacros de Examen",
                subtitle = "Evaluación generada con corrección pedagógica",
                icon = Icons.Default.Quiz,
                iconColor = StatusPurple,
                onClick = onNavigateToQuiz
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ANÁLISIS Y CONFIGURACIÓN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            MenuNavigationItem(
                title = "Mis Hábitos y Procrastinación",
                subtitle = "Patrones de estudio, horarios pico y motivos",
                icon = Icons.Default.Insights,
                iconColor = Color(0xFFD97706),
                onClick = onNavigateToHabits
            )
        }

        item {
            MenuNavigationItem(
                title = "Configuración",
                subtitle = "Preferencias de foco, descanso y cuenta",
                icon = Icons.Default.Settings,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onNavigateToSettings
            )
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun MenuNavigationItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: Focus4ViewModel,
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    var showSeedConfirmDialog by remember { mutableStateOf(false) }

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
                text = "CONFIGURACIÓN",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

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
                            text = "CUENTA Y PERFIL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )

                        TextButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier.testTag("edit_profile_settings_button")
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar Perfil", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            avatarId = user?.avatarId,
                            profileImageUri = user?.profileImageUri,
                            size = 50.dp,
                            onClick = onNavigateToProfile
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = user?.getEffectiveDisplayName() ?: "Estudiante",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = user?.email ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Carrera: ${user?.career} • ${user?.semester}", fontSize = 13.sp)
                    Text(text = "Duración preferida de sesión: ${user?.preferredSessionMinutes} min", fontSize = 13.sp)
                    Text(text = "Horas diarias disponibles: ${user?.dailyAvailableHours}h", fontSize = 13.sp)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HERRAMIENTAS DE DESARROLLO / PRUEBAS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Puedes recargar los datos demo universitarios (Análisis Matemático II, Física I, Algoritmos, parciales y temas).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showSeedConfirmDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recargar Datos de Prueba Universitarios")
                    }
                }
            }
        }

        // Danger Zone: Delete All Data
        item {
            var showDeleteAllDialog by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StatusDanger.copy(alpha = 0.08f)),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ZONA DE PELIGRO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusDanger
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Elimina permanentemente todas tus materias, temas, sesiones, exámenes y estadísticas. Tu cuenta quedará vacía.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showDeleteAllDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger),
                        modifier = Modifier.fillMaxWidth().testTag("delete_all_data_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar Todos Mis Datos", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (showDeleteAllDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteAllDialog = false },
                    title = {
                        Text(
                            text = "¿Eliminar todos tus datos académicos?",
                            fontWeight = FontWeight.Bold,
                            color = StatusDanger
                        )
                    },
                    text = {
                        Text(
                            text = "Esta acción eliminará de forma irreversible todas tus asignaturas, sesiones, exámenes y registros de hábitos de tu cuenta. No afectará a otros usuarios ni se podrá recuperar.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteAllDialog = false
                                viewModel.deleteAllUserData()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                        ) {
                            Text("Eliminar Todo Definitivamente")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteAllDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }

        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showSeedConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSeedConfirmDialog = false },
            title = { Text("¿Cargar materias y datos de prueba?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Esta acción agregará asignaturas de ejemplo (Análisis Matemático II, Física I, Algoritmos) con sus temas y parciales para realizar pruebas del sistema. No uses esto si quieres mantener tu cuenta vacía.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAndSeedDemo()
                        showSeedConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Cargar Datos de Prueba")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeedConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CalendarScreen(
    viewModel: Focus4ViewModel
) {
    val allSessions by viewModel.allSessions.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val subjectMap = subjects.associateBy { it.id }

    val sdfIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfDisplay = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    val sdfDayNum = SimpleDateFormat("d", Locale.getDefault())
    val sdfDayName = SimpleDateFormat("EEE", Locale("es", "ES"))

    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val selectedDateStr = sdfIso.format(selectedCalendar.time)

    // Compute week dates centered on selected week
    val weekDates = remember(selectedCalendar.get(Calendar.WEEK_OF_YEAR), selectedCalendar.get(Calendar.YEAR)) {
        val cal = selectedCalendar.clone() as Calendar
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        (0 until 7).map {
            val date = cal.time
            val iso = sdfIso.format(date)
            val dayNumber = sdfDayNum.format(date)
            val dayName = sdfDayName.format(date).take(3).replaceFirstChar { it.uppercase() }
            cal.add(Calendar.DAY_OF_YEAR, 1)
            Triple(iso, dayNumber, dayName)
        }
    }

    // Filter sessions and exams for selected date
    val daySessions = allSessions.filter { it.scheduledDate == selectedDateStr }
    val dayExams = exams.filter {
        val examDateStr = sdfIso.format(Date(it.examDate))
        examDateStr == selectedDateStr
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "CALENDARIO ACADÉMICO",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Cronograma interactivo de sesiones de estudio y fechas de examen",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Navigation Bar for Calendar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val newCal = selectedCalendar.clone() as Calendar
                            newCal.add(Calendar.DAY_OF_YEAR, -7)
                            selectedCalendar = newCal
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Semana anterior")
                        }

                        TextButton(onClick = {
                            selectedCalendar = Calendar.getInstance()
                        }) {
                            Text("Hoy", fontWeight = FontWeight.Bold, color = BluePrimary)
                        }

                        IconButton(onClick = {
                            val newCal = selectedCalendar.clone() as Calendar
                            newCal.add(Calendar.DAY_OF_YEAR, 7)
                            selectedCalendar = newCal
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Semana siguiente")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 7-day week row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        weekDates.forEach { (iso, dayNum, dayName) ->
                            val isSelected = iso == selectedDateStr
                            val hasSessions = allSessions.any { it.scheduledDate == iso }
                            val hasExams = exams.any { sdfIso.format(Date(it.examDate)) == iso }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) BluePrimary else Color.Transparent)
                                    .clickable {
                                        val cal = Calendar.getInstance()
                                        cal.time = sdfIso.parse(iso) ?: Date()
                                        selectedCalendar = cal
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = dayName,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dayNum,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    if (hasExams) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else StatusDanger)
                                        )
                                    }
                                    if (hasSessions) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else BluePrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Header
        item {
            val formattedSelected = sdfDisplay.format(selectedCalendar.time).replaceFirstChar { it.uppercase() }
            Text(
                text = formattedSelected,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Exams on selected date
        if (dayExams.isNotEmpty()) {
            item {
                Text(
                    text = "EXÁMENES (${dayExams.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusDanger,
                    letterSpacing = 0.5.sp
                )
            }
            items(dayExams.size) { idx ->
                val exam = dayExams[idx]
                val subName = subjectMap[exam.subjectId]?.name ?: "Materia"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(subName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                            PriorityBadge(priority = exam.priority)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(exam.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        if (exam.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(exam.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Sessions on selected date
        if (daySessions.isNotEmpty()) {
            item {
                Text(
                    text = "SESIONES PROGRAMADAS (${daySessions.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary,
                    letterSpacing = 0.5.sp
                )
            }
            items(daySessions.size) { idx ->
                val session = daySessions[idx]
                val subName = subjectMap[session.subjectId]?.name ?: "Materia"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(subName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BluePrimary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(status = session.status)
                                Spacer(modifier = Modifier.width(4.dp))
                                PriorityBadge(priority = session.priority)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(session.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Planificado: ${session.plannedMinutes}m" + if (session.actualMinutes > 0) " • Real: ${session.actualMinutes}m" else "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Empty state for selected date
        if (dayExams.isEmpty() && daySessions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sin actividades para este día.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No tienes sesiones ni exámenes programados para esta fecha.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
