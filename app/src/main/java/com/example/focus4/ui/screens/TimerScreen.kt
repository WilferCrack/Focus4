package com.example.focus4.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focus4.data.model.StudySessionEntity
import com.example.focus4.ui.Focus4ViewModel
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BlueSecondary
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy950
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@Composable
fun TimerScreen(
    viewModel: Focus4ViewModel,
    onFinishOrExit: () -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val remainingSeconds by viewModel.timerRemainingSeconds.collectAsState()
    val totalSeconds by viewModel.timerTotalSeconds.collectAsState()
    val isRunning by viewModel.isTimerRunning.collectAsState()
    val isPaused by viewModel.isTimerPaused.collectAsState()
    val pausesCount by viewModel.timerPausesCount.collectAsState()
    val effectiveSeconds by viewModel.effectiveSeconds.collectAsState()

    var showEvaluationDialog by remember { mutableStateOf(false) }
    var evaluationChoice by remember { mutableStateOf("completed") } // "completed", "partially_completed", "not_completed"
    var selectedUncompletedReason by remember { mutableStateOf<String?>(null) }
    var notesText by remember { mutableStateOf("") }

    val subjectName = activeSession?.let { s ->
        subjects.find { it.id == s.subjectId }?.name
    } ?: "Sesión de Estudio"

    // Time formatting: MM:SS
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val progress = if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else 0f

    // Effective time formatting: Xm Ys
    val effMinutes = effectiveSeconds / 60
    val effSeconds = effectiveSeconds % 60
    val effectiveFormatted = "${effMinutes}m ${effSeconds}s"

    val uncompletedReasons = listOf(
        "Me distraje",
        "Estaba cansado",
        "No tenía ganas",
        "No entendía el tema",
        "La tarea era demasiado difícil",
        "Tuve otra actividad",
        "Problemas personales",
        "Otro"
    )

    if (activeSession == null) {
        // Fallback state if user opens tab without active session
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Modo Focus",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selecciona una sesión de tu Plan para iniciar el temporizador limpio.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onFinishOrExit,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Ver Plan de Hoy")
                }
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy950)
            .padding(20.dp)
            .testTag("focus_timer_screen")
    ) {
        // Top Cancel / Exit Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Navy800,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "MODO FOCUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = BlueSecondary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            IconButton(
                onClick = {
                    showEvaluationDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar o Finalizar",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Center: Subject, Topic, Circular Countdown
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = subjectName.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = BlueSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = activeSession?.title ?: "Sesión",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Large Circular Progress Timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(260.dp)
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 14.dp,
                    color = Navy800,
                    strokeCap = StrokeCap.Round
                )
                // Active Countdown Progress
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 14.dp,
                    color = if (isPaused) StatusWarning else BluePrimary,
                    strokeCap = StrokeCap.Round
                )

                // Timer Numbers
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        modifier = Modifier.testTag("timer_countdown_text")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isPaused) "EN PAUSA" else "TIEMPO EFECTIVO: $effectiveFormatted",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPaused) StatusWarning else Color.White.copy(alpha = 0.6f)
                    )

                    if (pausesCount > 0) {
                        Text(
                            text = "Pausas: $pausesCount",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Main Actions: Pause / Resume & Finish
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPaused) {
                    Button(
                        onClick = { viewModel.resumeTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("resume_timer_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.pauseTimer() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .height(54.dp)
                            .border(1.dp, Navy700, RoundedCornerShape(16.dp))
                            .testTag("pause_timer_button")
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pausar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Button(
                    onClick = {
                        showEvaluationDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("finish_session_button")
                ) {
                    Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Finalizar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    // Evaluation Dialog upon finishing or abandoning
    if (showEvaluationDialog) {
        AlertDialog(
            onDismissRequest = { showEvaluationDialog = false },
            containerColor = Navy800,
            title = {
                Text(
                    text = "🎉 Sesión terminada",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Tiempo efectivo: $effectiveFormatted",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BlueSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Objetivo: ${activeSession?.title}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "¿Cómo salió esta sesión?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3 Evaluation Choices
                    listOf(
                        "completed" to "Completada",
                        "partially_completed" to "Parcial",
                        "not_completed" to "No pude completarla"
                    ).forEach { (choice, label) ->
                        val isSel = evaluationChoice == choice
                        Surface(
                            color = if (isSel) BluePrimary.copy(alpha = 0.3f) else Navy950,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) BluePrimary else Navy700
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    evaluationChoice = choice
                                    if (choice == "completed") {
                                        selectedUncompletedReason = null
                                    }
                                }
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // If not completed or partial, ask Why without judgement
                    AnimatedVisibility(visible = evaluationChoice != "completed") {
                        Column {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "¿Por qué no pudiste completarla?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarning
                            )
                            Text(
                                text = "Guardamos esto para ayudarte a detectar patrones, sin juzgar.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            uncompletedReasons.forEach { reason ->
                                val isR = selectedUncompletedReason == reason
                                Surface(
                                    color = if (isR) StatusWarning.copy(alpha = 0.25f) else Navy950,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isR) StatusWarning else Navy700
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable { selectedUncompletedReason = reason }
                                ) {
                                    Text(
                                        text = reason,
                                        fontSize = 12.sp,
                                        color = if (isR) Color.White else Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("¿Qué pasó? (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishFocusSession(
                            evaluation = evaluationChoice,
                            uncompletedReason = selectedUncompletedReason,
                            notes = notesText
                        )
                        showEvaluationDialog = false
                        onFinishOrExit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Guardar Resultado")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEvaluationDialog = false }) {
                    Text("Volver al temporizador", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}
