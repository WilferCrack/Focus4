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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focus4.data.model.ExamEntity
import com.example.focus4.ui.Focus4ViewModel
import com.example.focus4.ui.components.PriorityBadge
import com.example.focus4.ui.components.StatCard
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(
    viewModel: Focus4ViewModel,
    onPrepareExam: (Long) -> Unit
) {
    val exams by viewModel.exams.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val subjectMap = subjects.associateBy { it.id }

    var showAddDialog by remember { mutableStateOf(false) }
    var examToEdit by remember { mutableStateOf<ExamEntity?>(null) }
    var examToDelete by remember { mutableStateOf<ExamEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BluePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_exam_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Examen")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "EXÁMENES Y PARCIALES (${exams.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Fechas límite reales para anticipar tu preparación y priorizar temas",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (exams.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No tenés exámenes registrados.",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Agregá un examen con su fecha real para poder preparar un plan de estudio y calcular días restantes.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("AGREGAR EXAMEN")
                            }
                        }
                    }
                }
            } else {
                items(exams, key = { it.id }) { exam ->
                    val subName = subjectMap[exam.subjectId]?.name ?: "Materia"

                    val now = System.currentTimeMillis()
                    val diffDays = TimeUnit.MILLISECONDS.toDays(exam.examDate - now)
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dateFormatted = sdf.format(Date(exam.examDate))

                    val daysText = when {
                        diffDays < 0 -> "Examen finalizado"
                        diffDays == 0L -> "¡HOY ES EL EXAMEN!"
                        diffDays == 1L -> "¡Mañana!"
                        else -> "Faltan $diffDays días"
                    }

                    val isUrgent = diffDays in 0..7
                    var menuExpanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUrgent) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            width = if (isUrgent) 1.5.dp else 1.dp
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = subName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BluePrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PriorityBadge(priority = exam.priority)
                                    Box {
                                        IconButton(
                                            onClick = { menuExpanded = true },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Opciones",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = menuExpanded,
                                            onDismissRequest = { menuExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Editar") },
                                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                                onClick = {
                                                    menuExpanded = false
                                                    examToEdit = exam
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Eliminar", color = StatusDanger) },
                                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusDanger) },
                                                onClick = {
                                                    menuExpanded = false
                                                    examToDelete = exam
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = exam.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (exam.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = exam.notes,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = if (isUrgent) StatusDanger else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$dateFormatted • $daysText",
                                        fontSize = 13.sp,
                                        fontWeight = if (isUrgent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isUrgent) StatusDanger else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = { onPrepareExam(exam.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                    modifier = Modifier.testTag("prepare_exam_button_${exam.id}")
                                ) {
                                    Text("Preparar", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add Exam Dialog
    if (showAddDialog) {
        ExamFormDialog(
            exam = null,
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onSave = { subId, title, dateMillis, notes, priority ->
                viewModel.addExam(
                    subjectId = subId,
                    title = title,
                    examDate = dateMillis,
                    notes = notes,
                    priority = priority
                )
                showAddDialog = false
            }
        )
    }

    // Edit Exam Dialog
    examToEdit?.let { ex ->
        ExamFormDialog(
            exam = ex,
            subjects = subjects,
            onDismiss = { examToEdit = null },
            onSave = { subId, title, dateMillis, notes, priority ->
                viewModel.updateExam(
                    ex.copy(
                        subjectId = subId,
                        title = title,
                        examDate = dateMillis,
                        notes = notes,
                        priority = priority,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                examToEdit = null
            }
        )
    }

    // Delete Exam Confirmation
    examToDelete?.let { ex ->
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            title = { Text("¿Eliminar examen?", fontWeight = FontWeight.Bold) },
            text = { Text("Se eliminará \"${ex.title}\" de tu cronograma.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExam(ex.id)
                        examToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { examToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamFormDialog(
    exam: ExamEntity?,
    subjects: List<com.example.focus4.data.model.SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (subjectId: Long, title: String, dateMillis: Long, notes: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf(exam?.title ?: "") }
    var notes by remember { mutableStateOf(exam?.notes ?: "") }
    var selectedSubjectId by remember {
        mutableStateOf(exam?.subjectId ?: (subjects.firstOrNull()?.id ?: 0L))
    }
    var priority by remember { mutableStateOf(exam?.priority ?: "Alta") }

    // Real selected date in milliseconds (default: 14 days ahead if creating, or existing examDate)
    var selectedDateMillis by remember {
        mutableLongStateOf(
            exam?.examDate ?: (System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000)
        )
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf<String?>(null) }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val priorityOptions = listOf("Baja", "Media", "Alta", "Crítica")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (exam == null) "Registrar Fecha de Examen" else "Editar Examen",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del examen (Ej: 1° Parcial) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (subjects.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = subjectDropdownExpanded,
                        onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
                    ) {
                        val selectedName = subjects.find { it.id == selectedSubjectId }?.name ?: "Seleccionar Materia"
                        OutlinedTextField(
                            value = selectedName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Materia") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            subjects.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text(sub.name) },
                                    onClick = {
                                        selectedSubjectId = sub.id
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real Date Picker field
                Text("Fecha del examen *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { showDatePickerDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fecha: ${sdf.format(Date(selectedDateMillis))}",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Quick access chips that calculate real date
                Spacer(modifier = Modifier.height(6.dp))
                Text("Accesos rápidos de fecha:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(7, 14, 21, 30).forEach { days ->
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, days)
                                selectedDateMillis = cal.timeInMillis
                                dateError = null
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+${days}d", fontSize = 11.sp)
                        }
                    }
                }

                if (dateError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = dateError ?: "", color = StatusDanger, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Prioridad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    priorityOptions.forEach { p ->
                        val isSel = priority == p
                        OutlinedButton(
                            onClick = { priority = p },
                            shape = RoundedCornerShape(8.dp),
                            colors = if (isSel) ButtonDefaults.buttonColors(containerColor = BluePrimary) else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(p.take(4), fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas o unidades que entran") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val todayStart = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    if (selectedDateMillis < todayStart) {
                        dateError = "La fecha del examen debe ser posterior a hoy."
                        return@Button
                    }

                    onSave(selectedSubjectId, title, selectedDateMillis, notes, priority)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(if (exam == null) "Guardar Examen" else "Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    // Native Material 3 DatePickerDialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
                    return utcTimeMillis >= yesterday
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Convert UTC date to local start of day
                            val cal = Calendar.getInstance().apply { timeInMillis = millis }
                            cal.set(Calendar.HOUR_OF_DAY, 12)
                            selectedDateMillis = cal.timeInMillis
                            dateError = null
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Seleccionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun HabitsScreen(
    viewModel: Focus4ViewModel
) {
    val habitReport by viewModel.habitReport.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val subjectMap = subjects.associateBy { it.id }

    val plannedTotalMinutes = allSessions.sumOf { it.plannedMinutes }
    val realTotalMinutes = habitReport.totalMinutesStudied
    val compliancePercent = if (allSessions.isNotEmpty()) {
        (habitReport.completedCount * 100) / allSessions.size
    } else 0

    val sessionsBySubject = remember(allSessions, subjects) {
        subjects.map { sub ->
            val subSessions = allSessions.filter { it.subjectId == sub.id }
            val minutesStudied = subSessions.sumOf { it.actualMinutes }
            sub to minutesStudied
        }.filter { it.second > 0 }
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
                text = "MIS HÁBITOS Y RENDIMIENTO",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Análisis objetivo basado en tu historial real de sesiones de estudio",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Constancia Activa",
                    value = "${habitReport.currentStreakDays} días",
                    subtitle = "Racha máxima: ${habitReport.longestStreakDays}d",
                    icon = Icons.Default.DateRange,
                    accentColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Horas Totales",
                    value = "${habitReport.totalMinutesStudied / 60}h",
                    subtitle = "${habitReport.totalMinutesStudied % 60}m acumulados",
                    icon = Icons.Default.Timer,
                    accentColor = BluePrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Planificado vs Real
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
                            text = "PLANIFICADO VS REAL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$compliancePercent% cumplimiento",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (compliancePercent >= 70) StatusSuccess else StatusWarning
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Tiempo Planificado", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${plannedTotalMinutes / 60}h ${plannedTotalMinutes % 60}m", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Tiempo Real Estudiado", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${realTotalMinutes / 60}h ${realTotalMinutes % 60}m", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                        }
                    }
                }
            }
        }

        // Sessions Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DISTRIBUCIÓN DE SESIONES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${habitReport.completedCount}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusSuccess
                            )
                            Text("Completadas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${habitReport.partialCount}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarning
                            )
                            Text("Parciales", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${habitReport.skippedCount}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusDanger
                            )
                            Text("Omitidas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Time per Subject
        if (sessionsBySubject.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TIEMPO REAL POR MATERIA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        sessionsBySubject.forEach { (sub, mins) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sub.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("${mins / 60}h ${mins % 60}m", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                            }
                        }
                    }
                }
            }
        }

        // Procrastination & Peak Hour Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = StatusPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PATRONES DE CONCENTRACIÓN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (habitReport.hasEnoughData) {
                        if (habitReport.mostFrequentUncompletedReason != null) {
                            Text(
                                text = "Motivo recurrente de interrupción:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "\"${habitReport.mostFrequentUncompletedReason}\"",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarning
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (habitReport.peakProductiveHourRange != null) {
                            Text(
                                text = "Horario con mayor efectividad:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = habitReport.peakProductiveHourRange ?: "",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusSuccess
                            )
                        }
                    } else {
                        Text(
                            text = "No tenemos suficientes datos todavía.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Registra y evalúa al menos 3 sesiones de estudio para descubrir qué factores influyen en tu concentración.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Insights list
        item {
            Text(
                text = "CONCLUSIONES Y CONSEJOS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(habitReport.insights) { tip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tip,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
