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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focus4.data.model.SubjectEntity
import com.example.focus4.data.model.TopicEntity
import com.example.focus4.ui.Focus4ViewModel
import com.example.focus4.ui.components.PriorityBadge
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@Composable
fun SubjectsScreen(
    viewModel: Focus4ViewModel,
    onSubjectClick: (Long) -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: Todas, 1: Activas, 2: Archivadas
    var showAddDialog by remember { mutableStateOf(false) }

    var subjectToEdit by remember { mutableStateOf<SubjectEntity?>(null) }
    var subjectToDelete by remember { mutableStateOf<SubjectEntity?>(null) }

    val filteredSubjects = when (selectedFilter) {
        1 -> subjects.filter { it.active }
        2 -> subjects.filter { !it.active }
        else -> subjects
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BluePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_subject_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Materia")
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
                    text = "MIS MATERIAS (${subjects.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Gestiona tus asignaturas universitarias y desglósalas en temas",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Tabs (Todas, Activas, Archivadas)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeCount = subjects.count { it.active }
                    val archivedCount = subjects.count { !it.active }

                    listOf(
                        0 to "Todas (${subjects.size})",
                        1 to "Activas ($activeCount)",
                        2 to "Archivadas ($archivedCount)"
                    ).forEach { (idx, label) ->
                        val isSelected = selectedFilter == idx
                        OutlinedButton(
                            onClick = { selectedFilter = idx },
                            shape = RoundedCornerShape(10.dp),
                            colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = BluePrimary) else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (filteredSubjects.isEmpty()) {
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
                                text = if (selectedFilter == 2) "No hay materias archivadas." else "Todavía no agregaste ninguna materia.",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (selectedFilter == 2) "Las materias que archives para un período posterior aparecerán aquí." else "Organizá tu estudio empezando por una materia. Al crearla podrás agregar temas y exámenes asociados.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (selectedFilter != 2) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("AGREGAR MATERIA")
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredSubjects, key = { it.id }) { subject ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(subject.colorHex))
                    } catch (e: Exception) {
                        BluePrimary
                    }

                    var menuExpanded by remember { mutableStateOf(false) }

                    Card(
                        onClick = { onSubjectClick(subject.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_card_${subject.id}"),
                        shape = RoundedCornerShape(16.dp),
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
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (subject.iconName) {
                                            "calculate" -> Icons.Default.Calculate
                                            "science" -> Icons.Default.Science
                                            "code" -> Icons.Default.Code
                                            else -> Icons.Default.Book
                                        },
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = subject.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!subject.active) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = StatusWarning.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Archivada",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StatusWarning,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (subject.teacher.isNotBlank() || subject.semester.isNotBlank()) {
                                        Text(
                                            text = listOfNotNull(
                                                subject.teacher.takeIf { it.isNotBlank() },
                                                subject.semester.takeIf { it.isNotBlank() }
                                            ).joinToString(" • "),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PriorityBadge(priority = subject.priority)
                                Box {
                                    IconButton(
                                        onClick = { menuExpanded = true },
                                        modifier = Modifier.size(32.dp)
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
                                                subjectToEdit = subject
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(if (subject.active) "Archivar" else "Reactivar / Desarchivar") },
                                            leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                                            onClick = {
                                                menuExpanded = false
                                                viewModel.toggleArchiveSubject(subject)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Eliminar", color = StatusDanger) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusDanger) },
                                            onClick = {
                                                menuExpanded = false
                                                subjectToDelete = subject
                                            }
                                        )
                                    }
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

    // Add Subject Dialog
    if (showAddDialog) {
        SubjectFormDialog(
            subject = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, desc, colorHex, priority, semester, teacher, active ->
                viewModel.addSubject(
                    name = name,
                    description = desc,
                    colorHex = colorHex,
                    priority = priority,
                    semester = semester,
                    teacher = teacher
                )
                showAddDialog = false
            }
        )
    }

    // Edit Subject Dialog
    subjectToEdit?.let { sub ->
        SubjectFormDialog(
            subject = sub,
            onDismiss = { subjectToEdit = null },
            onSave = { name, desc, colorHex, priority, semester, teacher, active ->
                viewModel.updateSubject(
                    sub.copy(
                        name = name,
                        description = desc,
                        colorHex = colorHex,
                        priority = priority,
                        semester = semester,
                        teacher = teacher,
                        active = active,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                subjectToEdit = null
            }
        )
    }

    // Delete Subject Confirmation Dialog
    subjectToDelete?.let { sub ->
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            title = {
                Text(
                    text = "¿Eliminar materia?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Esta acción eliminará también los datos asociados a esta materia.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubject(sub.id)
                        subjectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SubjectDetailScreen(
    subjectId: Long,
    viewModel: Focus4ViewModel,
    onBackClick: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    val topics by viewModel.selectedSubjectTopics.collectAsState()
    val subject = subjects.find { it.id == subjectId }

    var showAddTopicDialog by remember { mutableStateOf(false) }
    var topicToEdit by remember { mutableStateOf<TopicEntity?>(null) }
    var topicToDelete by remember { mutableStateOf<TopicEntity?>(null) }

    var showEditSubjectDialog by remember { mutableStateOf(false) }
    var showDeleteSubjectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(subjectId) {
        viewModel.loadTopicsForSubject(subjectId)
    }

    if (subject == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Materia eliminada o no encontrada.")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBackClick) { Text("Volver a Materias") }
            }
        }
        return
    }

    val color = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (e: Exception) {
        BluePrimary
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTopicDialog = true },
                containerColor = color,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_topic_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Tema")
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
                // Subject Hero Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = subject.semester,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PriorityBadge(priority = subject.priority)
                                IconButton(
                                    onClick = { showEditSubjectDialog = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = color, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.toggleArchiveSubject(subject) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Archive,
                                        contentDescription = if (subject.active) "Archivar" else "Reactivar",
                                        tint = if (subject.active) color else StatusWarning,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { showDeleteSubjectDialog = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = StatusDanger, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = subject.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (subject.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subject.description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (subject.teacher.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = subject.teacher,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TEMAS Y UNIDADES (${topics.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
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
                                text = "Esta materia aún no tiene temas cargados.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Desglosa la materia en temas concretos para generar sesiones de foco y preparar exámenes.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showAddTopicDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = color),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Agregar Primer Tema")
                            }
                        }
                    }
                }
            } else {
                items(topics, key = { it.id }) { topic ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                Text(
                                    text = topic.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                Surface(
                                    color = when (topic.status) {
                                        "dominado" -> StatusSuccess.copy(alpha = 0.15f)
                                        "en_progreso" -> StatusWarning.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = topic.status.replace("_", " ").uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (topic.status) {
                                            "dominado" -> StatusSuccess
                                            "en_progreso" -> StatusWarning
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (topic.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = topic.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Topic Progress Bar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Progreso del tema",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${topic.progress}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { topic.progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Dificultad: ${topic.difficulty} • Importancia: ${topic.importance}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row {
                                    IconButton(
                                        onClick = { topicToEdit = topic },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar Tema",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { topicToDelete = topic },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar Tema",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
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

    // Add Topic Dialog
    if (showAddTopicDialog) {
        TopicFormDialog(
            subjectColor = color,
            topic = null,
            onDismiss = { showAddTopicDialog = false },
            onSave = { name, desc, minutes, diff, imp, prog ->
                viewModel.addTopic(
                    subjectId = subjectId,
                    name = name,
                    description = desc,
                    estimatedMinutes = minutes,
                    difficulty = diff,
                    importance = imp
                )
                showAddTopicDialog = false
            }
        )
    }

    // Edit Topic Dialog
    topicToEdit?.let { top ->
        TopicFormDialog(
            subjectColor = color,
            topic = top,
            onDismiss = { topicToEdit = null },
            onSave = { name, desc, minutes, diff, imp, prog ->
                viewModel.updateTopic(
                    top.copy(
                        name = name,
                        description = desc,
                        estimatedMinutes = minutes,
                        difficulty = diff,
                        importance = imp,
                        progress = prog,
                        status = when {
                            prog >= 100 -> "dominado"
                            prog > 0 -> "en_progreso"
                            else -> "pendiente"
                        },
                        updatedAt = System.currentTimeMillis()
                    )
                )
                topicToEdit = null
            }
        )
    }

    // Delete Topic Confirmation
    topicToDelete?.let { top ->
        AlertDialog(
            onDismissRequest = { topicToDelete = null },
            title = { Text("¿Eliminar tema?", fontWeight = FontWeight.Bold) },
            text = { Text("Se eliminará \"${top.name}\" y las sesiones o flashcards asociadas.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTopic(top.id)
                        topicToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { topicToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    // Edit Subject Dialog from Detail
    if (showEditSubjectDialog) {
        SubjectFormDialog(
            subject = subject,
            onDismiss = { showEditSubjectDialog = false },
            onSave = { name, desc, colorHex, priority, semester, teacher, active ->
                viewModel.updateSubject(
                    subject.copy(
                        name = name,
                        description = desc,
                        colorHex = colorHex,
                        priority = priority,
                        semester = semester,
                        teacher = teacher,
                        active = active,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                showEditSubjectDialog = false
            }
        )
    }

    // Delete Subject Dialog from Detail
    if (showDeleteSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSubjectDialog = false },
            title = {
                Text(
                    text = "¿Eliminar materia?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Esta acción eliminará también los datos asociados a esta materia.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubject(subject.id)
                        showDeleteSubjectDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSubjectDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun SubjectFormDialog(
    subject: SubjectEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, desc: String, colorHex: String, priority: String, semester: String, teacher: String, active: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(subject?.name ?: "") }
    var description by remember { mutableStateOf(subject?.description ?: "") }
    var teacher by remember { mutableStateOf(subject?.teacher ?: "") }
    var semester by remember { mutableStateOf(subject?.semester ?: "1° Semestre") }
    var priority by remember { mutableStateOf(subject?.priority ?: "Alta") }
    var selectedColor by remember { mutableStateOf(subject?.colorHex ?: "#2563EB") }
    var active by remember { mutableStateOf(subject?.active ?: true) }

    val colorOptions = listOf("#2563EB", "#10B981", "#8B5CF6", "#F59E0B", "#EF4444", "#06B6D4")
    val priorityOptions = listOf("Baja", "Media", "Alta", "Crítica")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (subject == null) "Nueva Materia" else "Editar Materia",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la materia *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción o programa") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("Profesor / Cátedra") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = semester,
                    onValueChange = { semester = it },
                    label = { Text("Semestre / Cuatrimestre (ej. 1° Semestre)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
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

                Spacer(modifier = Modifier.height(10.dp))

                Text("Color de identificación", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { hex ->
                        val c = Color(android.graphics.Color.parseColor(hex))
                        val isSel = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSel) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, description, selectedColor, priority, semester, teacher, active)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(if (subject == null) "Crear Materia" else "Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun TopicFormDialog(
    subjectColor: Color,
    topic: TopicEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, desc: String, minutes: Int, difficulty: String, importance: String, progress: Int) -> Unit
) {
    var name by remember { mutableStateOf(topic?.name ?: "") }
    var description by remember { mutableStateOf(topic?.description ?: "") }
    var estimatedMinutes by remember { mutableIntStateOf(topic?.estimatedMinutes ?: 60) }
    var difficulty by remember { mutableStateOf(topic?.difficulty ?: "Media") }
    var importance by remember { mutableStateOf(topic?.importance ?: "Alta") }
    var progress by remember { mutableIntStateOf(topic?.progress ?: 0) }

    val diffOptions = listOf("Fácil", "Media", "Difícil")
    val impOptions = listOf("Baja", "Media", "Alta", "Crítica")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (topic == null) "Nuevo Tema" else "Editar Tema",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
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
                    label = { Text("Descripción o conceptos clave") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text("Dificultad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    diffOptions.forEach { d ->
                        val isSel = difficulty == d
                        OutlinedButton(
                            onClick = { difficulty = d },
                            shape = RoundedCornerShape(8.dp),
                            colors = if (isSel) ButtonDefaults.buttonColors(containerColor = subjectColor) else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(d, fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Importancia para examen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    impOptions.forEach { imp ->
                        val isSel = importance == imp
                        OutlinedButton(
                            onClick = { importance = imp },
                            shape = RoundedCornerShape(8.dp),
                            colors = if (isSel) ButtonDefaults.buttonColors(containerColor = subjectColor) else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(imp.take(4), fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                if (topic != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Progreso de dominio: $progress%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0, 25, 50, 75, 100).forEach { p ->
                            val isSel = progress == p
                            OutlinedButton(
                                onClick = { progress = p },
                                shape = RoundedCornerShape(8.dp),
                                colors = if (isSel) ButtonDefaults.buttonColors(containerColor = subjectColor) else ButtonDefaults.outlinedButtonColors(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("$p%", fontSize = 10.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, description, estimatedMinutes, difficulty, importance, progress)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = subjectColor)
            ) {
                Text(if (topic == null) "Crear Tema" else "Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
