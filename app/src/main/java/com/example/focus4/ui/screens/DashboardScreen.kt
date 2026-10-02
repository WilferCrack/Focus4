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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focus4.data.model.StudySessionEntity
import com.example.focus4.ui.Focus4ViewModel
import com.example.focus4.ui.components.ExamCard
import com.example.focus4.ui.components.SessionCard
import com.example.focus4.ui.components.StatCard
import com.example.focus4.ui.components.UserAvatar
import com.example.focus4.ui.components.WhatToDoNowCard
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BlueSecondary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: Focus4ViewModel,
    onStartSession: (StudySessionEntity) -> Unit,
    onPrepareExam: (Long) -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToPlan: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val user by viewModel.currentUser.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val todaySessions by viewModel.todaySessions.collectAsState()
    val pendingSessions by viewModel.pendingSessions.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val habitReport by viewModel.habitReport.collectAsState()

    val subjectMap = subjects.associateBy { it.id }

    // Today's Date formatted
    val sdfDate = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    val todayFormatted = sdfDate.format(Date()).replaceFirstChar { it.uppercase() }

    // Calculate Today's Progress
    val completedCount = todaySessions.count { it.status == "completed" }
    val totalSessions = todaySessions.size
    val totalMinutesToday = todaySessions.filter { it.status == "completed" || it.status == "partially_completed" }
        .sumOf { it.actualMinutes }
    val progressPercentage = if (totalSessions > 0) (completedCount * 100) / totalSessions else 0

    // What To Do Now deterministic recommendation engine
    val recommendation = remember(subjects, todaySessions, pendingSessions, exams, user) {
        com.example.focus4.domain.WhatToDoNowEngine.evaluate(
            subjects = subjects,
            todaySessions = todaySessions,
            pendingSessions = pendingSessions,
            exams = exams,
            topics = emptyList(),
            dailyAvailableHours = user?.dailyAvailableHours ?: 2.0f,
            preferredSessionMinutes = user?.preferredSessionMinutes ?: 25
        )
    }

    // Next upcoming exam
    val now = System.currentTimeMillis()
    val nextExam = exams.filter { it.examDate >= now }.minByOrNull { it.examDate }

    var showQuickAddSubjectDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Greeting & Streak
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToProfile() }
                ) {
                    UserAvatar(
                        avatarId = user?.avatarId,
                        profileImageUri = user?.profileImageUri,
                        size = 46.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "HOLA, ${(user?.getEffectiveDisplayName() ?: "ESTUDIANTE").uppercase()}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1
                        )
                        Text(
                            text = todayFormatted,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Streak Badge
                Surface(
                    color = if (habitReport.currentStreakDays > 0) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (habitReport.currentStreakDays > 0) "🔥" else "🌱",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (habitReport.currentStreakDays > 0)
                                "${habitReport.currentStreakDays} días"
                            else "0 días",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (habitReport.currentStreakDays > 0) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Empty state callout for brand new user with 0 subjects
        if (subjects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Todavía no tenés nada planificado.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Empezá agregando tu primera materia universitaria para organizar temas y preparar tus parciales.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onNavigateToSubjects,
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("dashboard_add_subject_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AGREGAR MATERIA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 1. ¿QUÉ HAGO AHORA? Hero Component
        item {
            WhatToDoNowCard(
                session = recommendation.session,
                subjectName = recommendation.subject?.name,
                reasonLabel = recommendation.reasonLabel,
                recommendationTitle = recommendation.title,
                recommendationSubtitle = recommendation.description,
                recommendationPriority = recommendation.priority,
                onStartClick = {
                    recommendation.session?.let { onStartSession(it) }
                },
                onCreateSessionClick = {
                    if (recommendation.exam != null) {
                        onPrepareExam(recommendation.exam.id)
                    } else if (subjects.isEmpty()) {
                        onNavigateToSubjects()
                    } else {
                        onNavigateToPlan()
                    }
                }
            )
        }

        // 2. Academic Debt alert if pending sessions exist
        if (pendingSessions.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFFBEB)
                    ),
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
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Tenés contenido pendiente (${pendingSessions.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Reorganicemos tu plan sin sobrecargarte.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.reorganizeAcademicDebt() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reorganizar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. PROGRESO DE HOY
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "PROGRESO DE HOY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$progressPercentage%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = if (progressPercentage == 100 && totalSessions > 0) StatusSuccess else BluePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { if (totalSessions > 0) progressPercentage / 100f else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progressPercentage == 100 && totalSessions > 0) StatusSuccess else BluePrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Tiempo estudiado",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val hours = totalMinutesToday / 60
                            val mins = totalMinutesToday % 60
                            Text(
                                text = if (hours > 0) "${hours}h ${mins}m" else "${mins}m",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Sesiones",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$completedCount / $totalSessions",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 4. PLAN DE HOY
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "PLAN DE HOY (${todaySessions.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (subjects.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { viewModel.generateDailyPlan() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Optimizar", fontSize = 11.sp)
                    }
                }
            }
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
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tenés sesiones programadas.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (subjects.isEmpty())
                                "Agregá una materia para comenzar a estructurar tus sesiones."
                            else
                                "Creá una sesión concreta de 25 minutos o pulsá 'Optimizar' para generar tu plan diario.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (subjects.isEmpty()) onNavigateToSubjects() else onNavigateToPlan()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (subjects.isEmpty()) "AGREGAR MATERIA" else "CREAR SESIÓN", fontSize = 13.sp)
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

                SessionCard(
                    session = session,
                    subjectName = subName,
                    subjectColor = subColor,
                    onStartClick = { onStartSession(session) },
                    onSkipClick = { viewModel.skipSession(session.id, "Omitida por el usuario") }
                )
            }
        }

        // 5. PRÓXIMO EXAMEN
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRÓXIMO EXAMEN",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (exams.isNotEmpty()) {
                    TextButton(onClick = onNavigateToExams) {
                        Text("Ver Todos (${exams.size})", fontSize = 12.sp)
                    }
                }
            }
        }

        if (nextExam != null) {
            item {
                val subName = subjectMap[nextExam.subjectId]?.name ?: "Materia"
                ExamCard(
                    exam = nextExam,
                    subjectName = subName,
                    onPrepareClick = { onPrepareExam(nextExam.id) }
                )
            }
        } else {
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
                            text = "No tenés exámenes registrados.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Agregá un examen con su fecha real para calcular días restantes y preparar un plan de estudio.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToExams,
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("AGREGAR EXAMEN", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 6. RESUMEN ACADÉMICO REAL
        item {
            Text(
                text = "RESUMEN ACADÉMICO",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Estudiado",
                    value = "${habitReport.totalMinutesStudied / 60}h ${habitReport.totalMinutesStudied % 60}m",
                    subtitle = "${habitReport.completedCount} sesiones",
                    icon = Icons.Default.Timer,
                    accentColor = BluePrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Días Activos",
                    value = "${habitReport.activeDaysCount}",
                    subtitle = "Racha máx: ${habitReport.longestStreakDays}d",
                    icon = Icons.Default.DateRange,
                    accentColor = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
