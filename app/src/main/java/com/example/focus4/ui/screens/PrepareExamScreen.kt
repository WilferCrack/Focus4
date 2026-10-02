package com.example.focus4.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focus4.data.model.TopicEntity
import com.example.focus4.domain.StudyPlannerService
import com.example.focus4.ui.Focus4ViewModel
import com.example.focus4.ui.components.PriorityBadge
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BlueSecondary
import com.example.ui.theme.Navy700
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun PrepareExamScreen(
    examId: Long,
    viewModel: Focus4ViewModel,
    onBackClick: () -> Unit,
    onPlanGenerated: () -> Unit
) {
    val exams by viewModel.exams.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    val exam = exams.find { it.id == examId }
    val subject = exam?.let { e -> subjects.find { it.id == e.subjectId } }

    var topics by remember { mutableStateOf<List<TopicEntity>>(emptyList()) }
    val selectedTopicIds = remember { mutableStateMapOf<Long, Boolean>() }

    var dailyMinutes by remember {
        mutableIntStateOf(((user?.dailyAvailableHours ?: 2.0f) * 60).toInt().coerceIn(30, 240))
    }
    var sessionDurationMinutes by remember {
        mutableIntStateOf(user?.preferredSessionMinutes ?: 25)
    }

    // Days of the week availability (Calendar constants)
    val availableDays = remember {
        mutableStateMapOf(
            Calendar.MONDAY to true,
            Calendar.TUESDAY to true,
            Calendar.WEDNESDAY to true,
            Calendar.THURSDAY to true,
            Calendar.FRIDAY to true,
            Calendar.SATURDAY to false,
            Calendar.SUNDAY to true
        )
    }

    var showAddTopicDialog by remember { mutableStateOf(false) }

    LaunchedEffect(subject?.id) {
        subject?.let { s ->
            viewModel.repository.getTopicsForSubject(s.id).collect { list ->
                topics = list
                list.forEach { t ->
                    if (!selectedTopicIds.containsKey(t.id)) {
                        selectedTopicIds[t.id] = true
                    }
                }
            }
        }
    }

    if (exam == null || subject == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No se encontró el examen seleccionado.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBackClick) {
                    Text("Volver a Exámenes")
                }
            }
        }
        return
    }

    val now = System.currentTimeMillis()
    val daysLeft = TimeUnit.MILLISECONDS.toDays(exam.examDate - now).coerceAtLeast(0)
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateFormatted = sdf.format(Date(exam.examDate))

    val subjectColor = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (e: Exception) {
        BluePrimary
    }

    val selectedTopicsList = topics.filter { selectedTopicIds[it.id] == true }

    // Run deterministic calculation
    val enabledDayOfWeekSet = availableDays.filter { it.value }.keys.toSet()
    val planResult = remember(selectedTopicsList, dailyMinutes, sessionDurationMinutes, enabledDayOfWeekSet) {
        StudyPlannerService.calculateExamPreparation(
            userId = user?.id ?: 1L,
            exam = exam,
            subject = subject,
            selectedTopics = selectedTopicsList,
            dailyAvailableMinutes = dailyMinutes,
            sessionDurationMinutes = sessionDurationMinutes,
            availableDaysOfWeek = enabledDayOfWeekSet
        )
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
            // Exam Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = subjectColor.copy(alpha = 0.12f)),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subject.name.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = subjectColor,
                            letterSpacing = 1.sp
                        )
                        PriorityBadge(priority = exam.priority)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = exam.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (daysLeft <= 7) StatusDanger else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$dateFormatted • Faltan $daysLeft días",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (daysLeft <= 7) StatusDanger else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section 1: Temas para el examen
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TEMAS A EVALUAR (${selectedTopicsList.size}/${topics.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(onClick = { showAddTopicDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar Tema", fontSize = 12.sp)
                }
            }
        }

        if (topics.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Esta materia todavía no tiene temas.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Para generar un plan de preparación primero agregá al menos un tema.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showAddTopicDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = subjectColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("AGREGAR TEMA")
                        }
                    }
                }
            }
        } else {
            items(topics, key = { it.id }) { topic ->
                val isChecked = selectedTopicIds[topic.id] ?: true
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTopicIds[topic.id] = !isChecked },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChecked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { selectedTopicIds[topic.id] = it },
                            colors = CheckboxDefaults.colors(checkedColor = subjectColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Progreso: ${topic.progress}% • Dificultad: ${topic.difficulty}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { topic.progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = subjectColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Disponibilidad de Estudio
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "CONFIGURACIÓN DE DISPONIBILIDAD",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
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
                    Text(
                        text = "Tiempo de estudio diario: $dailyMinutes min (${dailyMinutes / 60}h ${dailyMinutes % 60}m)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(30, 45, 60, 90, 120).forEach { m ->
                            val isSel = dailyMinutes == m
                            OutlinedButton(
                                onClick = { dailyMinutes = m },
                                shape = RoundedCornerShape(8.dp),
                                colors = if (isSel) ButtonDefaults.buttonColors(containerColor = BluePrimary) else ButtonDefaults.outlinedButtonColors(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("${m}m", fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Duración por sesión de foco: $sessionDurationMinutes min",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 25, 30, 45, 60).forEach { mins ->
                            val isSel = sessionDurationMinutes == mins
                            OutlinedButton(
                                onClick = { sessionDurationMinutes = mins },
                                shape = RoundedCornerShape(8.dp),
                                colors = if (isSel) ButtonDefaults.buttonColors(containerColor = BluePrimary) else ButtonDefaults.outlinedButtonColors(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("${mins}m", fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Días habituales disponibles para estudiar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Calendar.MONDAY to "L",
                            Calendar.TUESDAY to "M",
                            Calendar.WEDNESDAY to "X",
                            Calendar.THURSDAY to "J",
                            Calendar.FRIDAY to "V",
                            Calendar.SATURDAY to "S",
                            Calendar.SUNDAY to "D"
                        ).forEach { (dayConst, label) ->
                            val isChecked = availableDays[dayConst] ?: false
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChecked) BluePrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { availableDays[dayConst] = !isChecked },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Resumen del Plan y Factibilidad
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ANÁLISIS DE FACTIBILIDAD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Días hábiles hasta examen:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${planResult.availableStudyDaysCount} días", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tiempo estimado requerido:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${planResult.totalRequiredMinutes / 60}h ${planResult.totalRequiredMinutes % 60}m", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sesiones concretas recomendadas:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${planResult.totalSessionsNeeded} sesiones", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                    }

                    if (!planResult.isCapacitySufficient) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = StatusDanger.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Capacidad insuficiente (Déficit: ${planResult.deficitMinutes} min)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusDanger
                                    )
                                    Text(
                                        text = "Con tu disponibilidad actual no alcanza para cubrir todo el temario antes de la fecha. Puedes aumentar el tiempo diario o continuar igualmente.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Generar Plan
        item {
            Button(
                onClick = {
                    if (planResult.plannedSessions.isNotEmpty()) {
                        viewModel.applyExamPreparationPlan(planResult.plannedSessions)
                        onPlanGenerated()
                    }
                },
                enabled = planResult.plannedSessions.isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("apply_exam_plan_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GENERAR PLAN (${planResult.plannedSessions.size} SESIONES)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Add Topic Dialog if user needs to add a topic on the fly
    if (showAddTopicDialog) {
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var estimatedMinutes by remember { mutableIntStateOf(60) }
        var difficulty by remember { mutableStateOf("Media") }
        var importance by remember { mutableStateOf("Alta") }

        AlertDialog(
            onDismissRequest = { showAddTopicDialog = false },
            title = {
                Text(
                    text = "Agregar Tema a ${subject.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del tema *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción o conceptos") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addTopic(
                                subjectId = subject.id,
                                name = name,
                                description = description,
                                estimatedMinutes = estimatedMinutes,
                                difficulty = difficulty,
                                importance = importance
                            )
                            showAddTopicDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = subjectColor)
                ) {
                    Text("Crear Tema")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTopicDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
