package com.example.focus4.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Replay
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focus4.data.model.StudySessionEntity
import com.example.focus4.ui.Focus4ViewModel
import com.example.focus4.ui.components.PriorityBadge
import com.example.focus4.ui.components.SessionCard
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.StatusDanger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    viewModel: Focus4ViewModel,
    onStartSession: (StudySessionEntity) -> Unit
) {
    val todaySessions by viewModel.todaySessions.collectAsState()
    val pendingSessions by viewModel.pendingSessions.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val subjectMap = subjects.associateBy { it.id }

    var showAddDialog by remember { mutableStateOf(false) }
    var sessionToEdit by remember { mutableStateOf<StudySessionEntity?>(null) }
    var sessionToDelete by remember { mutableStateOf<StudySessionEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BluePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_session_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Sesión")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "MI PLAN DE ESTUDIO",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Menos planificación abstracta. Más acciones concretas.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (subjects.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.generateDailyPlan() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Optimizar", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Academic debt section if any
            if (pendingSessions.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Replay, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Deuda Académica (${pendingSessions.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                                Button(
                                    onClick = { viewModel.reorganizeAcademicDebt() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Reorganizar Plan", fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Distribuiremos tus sesiones pendientes sin generar sobrecarga.",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }

                items(pendingSessions, key = { "pending_${it.id}" }) { session ->
                    val subject = subjectMap[session.subjectId]
                    val subName = subject?.name ?: "Materia"
                    val subColor = try {
                        Color(android.graphics.Color.parseColor(subject?.colorHex ?: "#2563EB"))
                    } catch (e: Exception) {
                        BluePrimary
                    }
                    SessionCardWithActions(
                        session = session,
                        subjectName = subName,
                        subjectColor = subColor,
                        onStartClick = { onStartSession(session) },
                        onSkipClick = { viewModel.skipSession(session.id, "Omitida") },
                        onEditClick = { sessionToEdit = session },
                        onDeleteClick = { sessionToDelete = session }
                    )
                }
            }

            // Today Sessions Section
            item {
                Text(
                    text = "SESIONES DE HOY (${todaySessions.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (todaySessions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No tenés sesiones programadas.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pulsa el botón '+' para agregar una sesión concreta de 25 minutos o 'Optimizar' para generar un plan.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("CREAR SESIÓN")
                            }
                        }
                    }
                }
            } else {
                items(todaySessions, key = { it.id }) { session ->
                    val subject = subjectMap[session.subjectId]
                    val subName = subject?.name ?: "Materia"
                    val subColor = try {
                        Color(android.graphics.Color.parseColor(subject?.colorHex ?: "#2563EB"))
                    } catch (e: Exception) {
                        BluePrimary
                    }
                    SessionCardWithActions(
                        session = session,
                        subjectName = subName,
                        subjectColor = subColor,
                        onStartClick = { onStartSession(session) },
                        onSkipClick = { viewModel.skipSession(session.id, "Omitida por usuario") },
                        onEditClick = { sessionToEdit = session },
                        onDeleteClick = { sessionToDelete = session }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add Session Dialog
    if (showAddDialog) {
        SessionFormDialog(
            session = null,
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onSave = { subId, title, desc, mins, dateStr, priority ->
                viewModel.addSession(
                    subjectId = subId,
                    topicId = null,
                    title = title,
                    description = desc,
                    plannedMinutes = mins,
                    scheduledDate = dateStr,
                    priority = priority
                )
                showAddDialog = false
            }
        )
    }

    // Edit Session Dialog
    sessionToEdit?.let { s ->
        SessionFormDialog(
            session = s,
            subjects = subjects,
            onDismiss = { sessionToEdit = null },
            onSave = { subId, title, desc, mins, dateStr, priority ->
                viewModel.updateSession(
                    s.copy(
                        subjectId = subId,
                        title = title,
                        description = desc,
                        plannedMinutes = mins,
                        scheduledDate = dateStr,
                        priority = priority,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                sessionToEdit = null
            }
        )
    }

    // Delete Session Confirmation
    sessionToDelete?.let { s ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("¿Eliminar sesión?", fontWeight = FontWeight.Bold) },
            text = { Text("Se eliminará \"${s.title}\" de tu plan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSession(s.id)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun SessionCardWithActions(
    session: StudySessionEntity,
    subjectName: String,
    subjectColor: Color,
    onStartClick: () -> Unit,
    onSkipClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_item_${session.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(subjectColor, RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subjectName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = subjectColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriorityBadge(priority = session.priority)
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
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
                                    onEditClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar", color = StatusDanger) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusDanger) },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = session.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (session.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = session.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${session.plannedMinutes} min • Fecha: ${session.scheduledDate}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (session.status == "planned") {
                    Row {
                        OutlinedButton(
                            onClick = onSkipClick,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text("Omitir", fontSize = 11.sp)
                        }
                        Button(
                            onClick = onStartClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Text("Iniciar", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionFormDialog(
    session: StudySessionEntity?,
    subjects: List<com.example.focus4.data.model.SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (subjectId: Long, title: String, desc: String, plannedMinutes: Int, scheduledDate: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf(session?.title ?: "") }
    var description by remember { mutableStateOf(session?.description ?: "") }
    var selectedSubjectId by remember {
        mutableStateOf(session?.subjectId ?: (subjects.firstOrNull()?.id ?: 0L))
    }
    var plannedMinutes by remember { mutableIntStateOf(session?.plannedMinutes ?: 25) }
    var priority by remember { mutableStateOf(session?.priority ?: "Media") }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var scheduledDate by remember { mutableStateOf(session?.scheduledDate ?: sdf.format(Date())) }

    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    val priorities = listOf("Baja", "Media", "Alta", "Crítica")
    val durations = listOf(15, 25, 30, 45, 60)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (session == null) "Nueva Sesión Concreta" else "Editar Sesión",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (subjects.isEmpty()) {
                    Text(
                        text = "Primero debes agregar una materia para poder asociar tus sesiones.",
                        fontSize = 13.sp,
                        color = StatusDanger
                    )
                } else {
                    Text(
                        text = "Ejemplo: 'Resolver 5 ejercicios de integrales por partes'",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Acción Concreta *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción o notas") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

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

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Duración de la sesión", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        durations.forEach { d ->
                            val isSel = plannedMinutes == d
                            OutlinedButton(
                                onClick = { plannedMinutes = d },
                                shape = RoundedCornerShape(8.dp),
                                colors = if (isSel) ButtonDefaults.buttonColors(containerColor = BluePrimary) else ButtonDefaults.outlinedButtonColors(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("${d}m", fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Prioridad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        priorities.forEach { p ->
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
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedSubjectId != 0L) {
                        onSave(selectedSubjectId, title, description, plannedMinutes, scheduledDate, priority)
                    }
                },
                enabled = subjects.isNotEmpty() && title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(if (session == null) "Crear Sesión" else "Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
