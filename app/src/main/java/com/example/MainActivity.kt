package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focus4.ui.Focus4ViewModel
import com.example.focus4.ui.components.FocusBottomNav
import com.example.focus4.ui.components.FocusTopBar
import com.example.focus4.ui.components.MainTab
import com.example.focus4.ui.components.UserAvatar
import com.example.focus4.ui.screens.AITutorScreen
import com.example.focus4.ui.screens.AuthScreen
import com.example.focus4.ui.screens.CalendarScreen
import com.example.focus4.ui.screens.DashboardScreen
import com.example.focus4.ui.screens.ExamQuizScreen
import com.example.focus4.ui.screens.ExamsScreen
import com.example.focus4.ui.screens.FlashcardsScreen
import com.example.focus4.ui.screens.HabitsScreen
import com.example.focus4.ui.screens.MoreMenuScreen
import com.example.focus4.ui.screens.OnboardingScreen
import com.example.focus4.ui.screens.PrepareExamScreen
import com.example.focus4.ui.screens.ProfileScreen
import com.example.focus4.ui.screens.SessionsScreen
import com.example.focus4.ui.screens.SettingsScreen
import com.example.focus4.ui.screens.SubjectDetailScreen
import com.example.focus4.ui.screens.SubjectsScreen
import com.example.focus4.ui.screens.TimerScreen
import com.example.ui.theme.Focus4Theme

enum class ScreenState {
    MAIN_TABS,
    SUBJECTS,
    SUBJECT_DETAIL,
    EXAMS,
    PREPARE_EXAM,
    HABITS,
    FLASHCARDS,
    QUIZ,
    CALENDAR,
    SETTINGS,
    PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Focus4Theme {
                val viewModel: Focus4ViewModel = viewModel()
                Focus4App(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun Focus4App(viewModel: Focus4ViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isUserLoading by viewModel.isUserLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()

    var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }
    var currentSubscreen by remember { mutableStateOf<ScreenState>(ScreenState.MAIN_TABS) }
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
    var selectedExamId by remember { mutableStateOf<Long?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // Automatically navigate to Timer if activeSession starts
    LaunchedEffect(activeSession) {
        if (activeSession != null) {
            currentTab = MainTab.TIMER
            currentSubscreen = ScreenState.MAIN_TABS
        }
    }

    if (isUserLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (currentUser == null) {
        AuthScreen(
            viewModel = viewModel,
            onAuthSuccess = {
                currentTab = MainTab.DASHBOARD
                currentSubscreen = ScreenState.MAIN_TABS
            }
        )
        return
    }

    if (currentUser?.onboardingCompleted == false) {
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = {
                currentTab = MainTab.DASHBOARD
                currentSubscreen = ScreenState.MAIN_TABS
            }
        )
        return
    }

    // Subscreen Back Handling
    if (currentSubscreen != ScreenState.MAIN_TABS) {
        BackHandler {
            when (currentSubscreen) {
                ScreenState.SUBJECT_DETAIL -> currentSubscreen = ScreenState.SUBJECTS
                ScreenState.PREPARE_EXAM -> currentSubscreen = ScreenState.EXAMS
                else -> currentSubscreen = ScreenState.MAIN_TABS
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            when (currentSubscreen) {
                ScreenState.MAIN_TABS -> {
                    if (currentTab != MainTab.TIMER) {
                        FocusTopBar(
                            title = when (currentTab) {
                                MainTab.DASHBOARD -> "FOCUS4"
                                MainTab.PLAN -> "Plan de Estudio"
                                MainTab.TUTOR -> "Tutor Académico IA"
                                MainTab.MORE -> "Más Opciones"
                                else -> "FOCUS4"
                            },
                            subtitle = when (currentTab) {
                                MainTab.DASHBOARD -> "Productividad Académica"
                                MainTab.PLAN -> "Acciones Concretas"
                                MainTab.TUTOR -> "Preguntas y Explicaciones"
                                MainTab.MORE -> "Herramientas del Estudiante"
                                else -> null
                            },
                            actions = {
                                androidx.compose.material3.IconButton(
                                    onClick = { currentSubscreen = ScreenState.PROFILE },
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    UserAvatar(
                                        avatarId = currentUser?.avatarId,
                                        profileImageUri = currentUser?.profileImageUri,
                                        size = 34.dp
                                    )
                                }
                            }
                        )
                    }
                }
                ScreenState.SUBJECTS -> {
                    FocusTopBar(
                        title = "Materias",
                        showBackButton = true,
                        onBackClick = { currentSubscreen = ScreenState.MAIN_TABS }
                    )
                }
                ScreenState.SUBJECT_DETAIL -> {
                    FocusTopBar(
                        title = "Detalle de Materia",
                        showBackButton = true,
                        onBackClick = { currentSubscreen = ScreenState.SUBJECTS }
                    )
                }
                ScreenState.EXAMS -> {
                    FocusTopBar(
                        title = "Exámenes",
                        showBackButton = true,
                        onBackClick = { currentSubscreen = ScreenState.MAIN_TABS }
                    )
                }
                ScreenState.PREPARE_EXAM -> {
                    FocusTopBar(
                        title = "Preparar Examen",
                        showBackButton = true,
                        onBackClick = { currentSubscreen = ScreenState.EXAMS }
                    )
                }
                ScreenState.HABITS -> {
                    FocusTopBar(
                        title = "Mis Hábitos",
                        showBackButton = true,
                        onBackClick = { currentSubscreen = ScreenState.MAIN_TABS }
                    )
                }
                ScreenState.FLASHCARDS -> {
                    FocusTopBar(
                        title = "Flashcards",
                        showBackButton = true,
                        onBackClick = { currentSubscreen = ScreenState.MAIN_TABS }
                    )
                }
                ScreenState.QUIZ -> {
                    FocusTopBar(
                        title = "Simulacro de Examen",
                        showBackButton = true,
                        onBackClick = { currentSubscreen = ScreenState.MAIN_TABS }
                    )
                }
                ScreenState.CALENDAR -> {
                    FocusTopBar(
                        title = "Calendario Académico",
                        showBackButton = true,
                        onBackClick = { currentSubscreen = ScreenState.MAIN_TABS }
                    )
                }
                ScreenState.SETTINGS -> {
                    FocusTopBar(
                        title = "Configuración",
                        showBackButton = true,
                        onBackClick = { currentSubscreen = ScreenState.MAIN_TABS }
                    )
                }
                ScreenState.PROFILE -> {
                    FocusTopBar(
                        title = "Mi Perfil e Identidad",
                        subtitle = "Avatares, foto y preferencias",
                        showBackButton = true,
                        onBackClick = { currentSubscreen = ScreenState.MAIN_TABS }
                    )
                }
            }
        },
        bottomBar = {
            if (currentSubscreen == ScreenState.MAIN_TABS && currentTab != MainTab.TIMER) {
                FocusBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentSubscreen) {
                ScreenState.MAIN_TABS -> {
                    when (currentTab) {
                        MainTab.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onStartSession = { session ->
                                viewModel.startFocusSession(session)
                                currentTab = MainTab.TIMER
                            },
                            onPrepareExam = { examId ->
                                selectedExamId = examId
                                currentSubscreen = ScreenState.PREPARE_EXAM
                            },
                            onNavigateToSubjects = {
                                currentSubscreen = ScreenState.SUBJECTS
                            },
                            onNavigateToExams = {
                                currentSubscreen = ScreenState.EXAMS
                            },
                            onNavigateToPlan = {
                                currentTab = MainTab.PLAN
                            },
                            onNavigateToProfile = {
                                currentSubscreen = ScreenState.PROFILE
                            }
                        )
                        MainTab.PLAN -> SessionsScreen(
                            viewModel = viewModel,
                            onStartSession = { session ->
                                viewModel.startFocusSession(session)
                                currentTab = MainTab.TIMER
                            }
                        )
                        MainTab.TIMER -> TimerScreen(
                            viewModel = viewModel,
                            onFinishOrExit = {
                                currentTab = MainTab.DASHBOARD
                            }
                        )
                        MainTab.TUTOR -> AITutorScreen(
                            viewModel = viewModel,
                            onNavigateToFlashcards = { currentSubscreen = ScreenState.FLASHCARDS },
                            onNavigateToQuiz = { currentSubscreen = ScreenState.QUIZ }
                        )
                        MainTab.MORE -> MoreMenuScreen(
                            viewModel = viewModel,
                            onNavigateToSubjects = { currentSubscreen = ScreenState.SUBJECTS },
                            onNavigateToExams = { currentSubscreen = ScreenState.EXAMS },
                            onNavigateToHabits = { currentSubscreen = ScreenState.HABITS },
                            onNavigateToFlashcards = { currentSubscreen = ScreenState.FLASHCARDS },
                            onNavigateToQuiz = { currentSubscreen = ScreenState.QUIZ },
                            onNavigateToCalendar = { currentSubscreen = ScreenState.CALENDAR },
                            onNavigateToSettings = { currentSubscreen = ScreenState.SETTINGS },
                            onNavigateToProfile = { currentSubscreen = ScreenState.PROFILE }
                        )
                    }
                }
                ScreenState.SUBJECTS -> SubjectsScreen(
                    viewModel = viewModel,
                    onSubjectClick = { subId ->
                        selectedSubjectId = subId
                        currentSubscreen = ScreenState.SUBJECT_DETAIL
                    }
                )
                ScreenState.SUBJECT_DETAIL -> selectedSubjectId?.let { subId ->
                    SubjectDetailScreen(
                        subjectId = subId,
                        viewModel = viewModel,
                        onBackClick = { currentSubscreen = ScreenState.SUBJECTS }
                    )
                }
                ScreenState.EXAMS -> ExamsScreen(
                    viewModel = viewModel,
                    onPrepareExam = { examId ->
                        selectedExamId = examId
                        currentSubscreen = ScreenState.PREPARE_EXAM
                    }
                )
                ScreenState.PREPARE_EXAM -> selectedExamId?.let { examId ->
                    PrepareExamScreen(
                        examId = examId,
                        viewModel = viewModel,
                        onBackClick = { currentSubscreen = ScreenState.EXAMS },
                        onPlanGenerated = {
                            currentSubscreen = ScreenState.MAIN_TABS
                            currentTab = MainTab.PLAN
                        }
                    )
                }
                ScreenState.HABITS -> HabitsScreen(
                    viewModel = viewModel
                )
                ScreenState.FLASHCARDS -> FlashcardsScreen(
                    viewModel = viewModel,
                    onBackClick = { currentSubscreen = ScreenState.MAIN_TABS }
                )
                ScreenState.QUIZ -> ExamQuizScreen(
                    viewModel = viewModel,
                    onBackClick = { currentSubscreen = ScreenState.MAIN_TABS }
                )
                ScreenState.CALENDAR -> CalendarScreen(
                    viewModel = viewModel
                )
                ScreenState.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToProfile = { currentSubscreen = ScreenState.PROFILE },
                    onLogout = {
                        viewModel.logout()
                    }
                )
                ScreenState.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { currentSubscreen = ScreenState.MAIN_TABS }
                )
            }
        }
    }
}
