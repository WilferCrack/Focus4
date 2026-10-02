package com.example.focus4.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus4.ai.AIProvider
import com.example.focus4.ai.GeminiAIProvider
import com.example.focus4.data.db.Focus4Database
import com.example.focus4.data.model.AIMessageEntity
import com.example.focus4.data.model.ClassAttendanceEntity
import com.example.focus4.data.model.ClassScheduleEntity
import com.example.focus4.data.model.ExamEntity
import com.example.focus4.data.model.ExamQuestionEntity
import com.example.focus4.data.model.FlashcardEntity
import com.example.focus4.data.model.GoalEntity
import com.example.focus4.data.model.StudySessionEntity
import com.example.focus4.data.model.SubjectEntity
import com.example.focus4.data.model.TopicEntity
import com.example.focus4.data.model.UserEntity
import com.example.focus4.data.repository.Focus4Repository
import com.example.focus4.domain.HabitAnalysisService
import com.example.focus4.domain.StudyPlannerService
import com.example.focus4.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Focus4ViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Focus4Database.getDatabase(application)
    val repository = Focus4Repository(db)
    val aiProvider: AIProvider = GeminiAIProvider()

    // --- Current Active User ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isUserLoading = MutableStateFlow(true)
    val isUserLoading: StateFlow<Boolean> = _isUserLoading.asStateFlow()

    val currentUserId: Long get() = _currentUser.value?.id ?: 1L

    // --- Subjects & Topics ---
    private val _subjects = MutableStateFlow<List<SubjectEntity>>(emptyList())
    val subjects: StateFlow<List<SubjectEntity>> = _subjects.asStateFlow()

    private val _selectedSubjectTopics = MutableStateFlow<List<TopicEntity>>(emptyList())
    val selectedSubjectTopics: StateFlow<List<TopicEntity>> = _selectedSubjectTopics.asStateFlow()

    // --- Exams & Goals ---
    private val _exams = MutableStateFlow<List<ExamEntity>>(emptyList())
    val exams: StateFlow<List<ExamEntity>> = _exams.asStateFlow()

    private val _goals = MutableStateFlow<List<GoalEntity>>(emptyList())
    val goals: StateFlow<List<GoalEntity>> = _goals.asStateFlow()

    // --- Study Sessions ---
    private val _todaySessions = MutableStateFlow<List<StudySessionEntity>>(emptyList())
    val todaySessions: StateFlow<List<StudySessionEntity>> = _todaySessions.asStateFlow()

    private val _pendingSessions = MutableStateFlow<List<StudySessionEntity>>(emptyList())
    val pendingSessions: StateFlow<List<StudySessionEntity>> = _pendingSessions.asStateFlow()

    private val _allSessions = MutableStateFlow<List<StudySessionEntity>>(emptyList())
    val allSessions: StateFlow<List<StudySessionEntity>> = _allSessions.asStateFlow()

    // --- Habit Report & Stats ---
    private val _habitReport = MutableStateFlow(HabitAnalysisService.analyzeHabits(emptyList()))
    val habitReport: StateFlow<HabitAnalysisService.HabitReport> = _habitReport.asStateFlow()

    // --- Timer State (Focus Mode) ---
    private val _activeSession = MutableStateFlow<StudySessionEntity?>(null)
    val activeSession: StateFlow<StudySessionEntity?> = _activeSession.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(25 * 60)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _timerTotalSeconds = MutableStateFlow(25 * 60)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _isTimerPaused = MutableStateFlow(false)
    val isTimerPaused: StateFlow<Boolean> = _isTimerPaused.asStateFlow()

    private val _timerPausesCount = MutableStateFlow(0)
    val timerPausesCount: StateFlow<Int> = _timerPausesCount.asStateFlow()

    private val _effectiveSeconds = MutableStateFlow(0)
    val effectiveSeconds: StateFlow<Int> = _effectiveSeconds.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTimeMillis: Long = 0L
    private var totalPausedDurationMillis: Long = 0L
    private var pauseStartTimeMillis: Long = 0L

    // --- AI Messages & States ---
    private val _aiMessages = MutableStateFlow<List<AIMessageEntity>>(emptyList())
    val aiMessages: StateFlow<List<AIMessageEntity>> = _aiMessages.asStateFlow()

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    // --- Flashcards & Exam Questions ---
    private val _flashcards = MutableStateFlow<List<FlashcardEntity>>(emptyList())
    val flashcards: StateFlow<List<FlashcardEntity>> = _flashcards.asStateFlow()

    private val _examQuestions = MutableStateFlow<List<ExamQuestionEntity>>(emptyList())
    val examQuestions: StateFlow<List<ExamQuestionEntity>> = _examQuestions.asStateFlow()

    // --- Class Schedules & Attendance ---
    private val _schedules = MutableStateFlow<List<ClassScheduleEntity>>(emptyList())
    val schedules: StateFlow<List<ClassScheduleEntity>> = _schedules.asStateFlow()

    private val _allAttendance = MutableStateFlow<List<ClassAttendanceEntity>>(emptyList())
    val allAttendance: StateFlow<List<ClassAttendanceEntity>> = _allAttendance.asStateFlow()

    private val _todayAttendance = MutableStateFlow<List<ClassAttendanceEntity>>(emptyList())
    val todayAttendance: StateFlow<List<ClassAttendanceEntity>> = _todayAttendance.asStateFlow()

    private val _attendanceStats = MutableStateFlow<Focus4Repository.OverallAttendanceStats?>(null)
    val attendanceStats: StateFlow<Focus4Repository.OverallAttendanceStats?> = _attendanceStats.asStateFlow()

    // --- Notifications / Toast ---
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        checkInitialUser()
    }

    private fun checkInitialUser() {
        viewModelScope.launch {
            _isUserLoading.value = true
            val user = repository.getFirstUser()
            if (user != null) {
                _currentUser.value = user
                loadUserData(user.id)
            } else {
                // Pre-create or show Auth
                _isUserLoading.value = false
            }
        }
    }

    fun loadUserData(userId: Long) {
        viewModelScope.launch {
            // Collect Subjects for user
            repository.getSubjects(userId).collect { list ->
                _subjects.value = list
            }
        }
        viewModelScope.launch {
            repository.getExams(userId).collect { list ->
                _exams.value = list
            }
        }
        viewModelScope.launch {
            repository.getGoals(userId).collect { list ->
                _goals.value = list
            }
        }
        val today = repository.getTodayString()
        viewModelScope.launch {
            repository.getSessionsByDate(userId, today).collect { list ->
                _todaySessions.value = list
            }
        }
        viewModelScope.launch {
            repository.getPendingSessions(userId, today).collect { list ->
                _pendingSessions.value = list
            }
        }
        viewModelScope.launch {
            repository.getSessionsForUser(userId).collect { list ->
                _allSessions.value = list
                _habitReport.value = HabitAnalysisService.analyzeHabits(list)
            }
        }
        viewModelScope.launch {
            repository.getAllFlashcards(userId).collect { list ->
                _flashcards.value = list
            }
        }
        viewModelScope.launch {
            repository.getExamQuestions(userId).collect { list ->
                _examQuestions.value = list
            }
        }
        viewModelScope.launch {
            repository.getAIMessages(userId).collect { list ->
                _aiMessages.value = list
            }
        }
        viewModelScope.launch {
            repository.getSchedules(userId).collect { list ->
                _schedules.value = list
            }
        }
        viewModelScope.launch {
            repository.getAttendanceForUser(userId).collect { list ->
                _allAttendance.value = list
                val todayStr = repository.getTodayString()
                _todayAttendance.value = list.filter { it.date == todayStr }
                loadAttendanceStats(userId)
            }
        }
        _isUserLoading.value = false
    }

    // --- Auth Actions ---
    fun register(
        first: String,
        last: String,
        email: String,
        pass: String,
        career: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.registerUser(first, last, email, pass, career)
            result.onSuccess { user ->
                _currentUser.value = user
                loadUserData(user.id)
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Error al registrar")
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.loginUser(email, pass)
            result.onSuccess { user ->
                _currentUser.value = user
                loadUserData(user.id)
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Credenciales incorrectas")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _todaySessions.value = emptyList()
        _pendingSessions.value = emptyList()
        _subjects.value = emptyList()
        _activeSession.value = null
    }

    fun completeOnboarding(career: String, hours: Float, sessionMinutes: Int, goal: String) {
        val user = _currentUser.value ?: return
        val updated = user.copy(
            career = career,
            dailyAvailableHours = hours,
            preferredSessionMinutes = sessionMinutes,
            mainGoal = goal,
            onboardingCompleted = true
        )
        viewModelScope.launch {
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }

    // --- Profile & Identity Actions ---
    fun updateProfile(
        displayName: String,
        career: String,
        semester: String,
        avatarId: String,
        dailyAvailableHours: Float,
        preferredSessionMinutes: Int,
        mainGoal: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = _currentUser.value ?: return
        val cleanName = displayName.trim()
        if (cleanName.isEmpty()) {
            onError("El nombre visible no puede estar vacío.")
            return
        }
        if (cleanName.length > 40) {
            onError("El nombre visible no debe superar los 40 caracteres.")
            return
        }

        viewModelScope.launch {
            val result = repository.updateUserProfile(
                userId = user.id,
                displayName = cleanName,
                career = career.trim(),
                semester = semester.trim(),
                avatarId = avatarId.trim(),
                profileImageUri = user.profileImageUri,
                dailyAvailableHours = dailyAvailableHours,
                preferredSessionMinutes = preferredSessionMinutes,
                mainGoal = mainGoal.trim()
            )
            result.onSuccess { updatedUser ->
                _currentUser.value = updatedUser
                _statusMessage.value = "Perfil actualizado correctamente"
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "Error al actualizar perfil")
            }
        }
    }

    fun saveCustomProfilePicture(uri: Uri) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val profilesDir = File(app.filesDir, "profiles")
                if (!profilesDir.exists()) profilesDir.mkdirs()
                val targetFile = File(profilesDir, "profile_${user.id}.jpg")
                app.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                val localUri = Uri.fromFile(targetFile).toString()
                val updatedUser = user.copy(profileImageUri = localUri)
                repository.updateUser(updatedUser)
                _currentUser.value = updatedUser
                _statusMessage.value = "Foto de perfil actualizada correctamente"
            } catch (e: Exception) {
                _statusMessage.value = "Error al guardar foto: ${e.localizedMessage}"
            }
        }
    }

    fun removeCustomProfilePicture() {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val profilesDir = File(app.filesDir, "profiles")
                val targetFile = File(profilesDir, "profile_${user.id}.jpg")
                if (targetFile.exists()) targetFile.delete()
            } catch (_: Exception) {}
            val updatedUser = user.copy(profileImageUri = null)
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            _statusMessage.value = "Foto eliminada. Usando avatar profesional."
        }
    }

    fun sendTestNotification(channelType: String) {
        val app = getApplication<Application>()
        when (channelType) {
            "session" -> {
                NotificationHelper.sendSessionCompletedNotification(
                    context = app,
                    sessionTitle = "Cálculo y Álgebra - Sesión de Foco",
                    durationMinutes = 25
                )
                _statusMessage.value = "Notificación de sesión completada emitida"
            }
            "exam" -> {
                val nextExam = _exams.value.firstOrNull()
                val title = nextExam?.title ?: "Primer Parcial - Análisis Matemático"
                val subject = _subjects.value.find { it.id == nextExam?.subjectId }?.name ?: "Matemática"
                NotificationHelper.sendExamAlertNotification(
                    context = app,
                    examTitle = title,
                    daysRemaining = 2,
                    subjectName = subject
                )
                _statusMessage.value = "Recordatorio de examen emitido"
            }
            else -> {
                NotificationHelper.sendTestNotification(
                    context = app,
                    title = "FOCUS4 — Productividad Académica",
                    message = "Tus hábitos y materias están sincronizados. ¡Excelente momento para estudiar!"
                )
                _statusMessage.value = "Notificación general de prueba emitida"
            }
        }
    }

    // --- Subject & Topic CRUD ---
    fun addSubject(
        name: String,
        description: String,
        colorHex: String,
        priority: String,
        semester: String,
        teacher: String = "",
        iconName: String = "book"
    ) {
        viewModelScope.launch {
            repository.insertSubject(
                SubjectEntity(
                    userId = currentUserId,
                    name = name.trim(),
                    description = description.trim(),
                    colorHex = colorHex,
                    priority = priority,
                    semester = semester.trim(),
                    teacher = teacher.trim(),
                    iconName = iconName
                )
            )
            _statusMessage.value = "Materia agregada: $name"
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
            _statusMessage.value = "Materia actualizada"
        }
    }

    fun toggleArchiveSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            val newActive = !subject.active
            repository.updateSubject(subject.copy(active = newActive, updatedAt = System.currentTimeMillis()))
            _statusMessage.value = if (newActive) "Materia activada" else "Materia archivada"
        }
    }

    fun deleteSubject(subjectId: Long) {
        viewModelScope.launch {
            repository.deleteSubject(subjectId)
            _statusMessage.value = "Materia eliminada"
        }
    }

    fun loadTopicsForSubject(subjectId: Long) {
        viewModelScope.launch {
            repository.getTopicsForSubject(subjectId).collect {
                _selectedSubjectTopics.value = it
            }
        }
    }

    fun addTopic(subjectId: Long, name: String, description: String, estimatedMinutes: Int, difficulty: String, importance: String) {
        viewModelScope.launch {
            repository.insertTopic(
                TopicEntity(
                    subjectId = subjectId,
                    name = name,
                    description = description,
                    estimatedMinutes = estimatedMinutes,
                    difficulty = difficulty,
                    importance = importance
                )
            )
            _statusMessage.value = "Tema agregado: $name"
        }
    }

    fun updateTopic(topic: TopicEntity) {
        viewModelScope.launch {
            repository.updateTopic(topic)
            _statusMessage.value = "Tema actualizado"
        }
    }

    fun deleteTopic(topicId: Long) {
        viewModelScope.launch {
            repository.deleteTopic(topicId)
            _statusMessage.value = "Tema eliminado"
        }
    }

    // --- Exam & Goal Actions ---
    fun addExam(subjectId: Long, title: String, examDate: Long, notes: String, priority: String) {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (examDate < todayStart) {
            _statusMessage.value = "La fecha del examen debe ser posterior a hoy."
            return
        }

        viewModelScope.launch {
            repository.insertExam(
                ExamEntity(
                    userId = currentUserId,
                    subjectId = subjectId,
                    title = title,
                    examDate = examDate,
                    notes = notes,
                    priority = priority
                )
            )
            _statusMessage.value = "Examen registrado"
        }
    }

    fun updateExam(exam: ExamEntity) {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (exam.examDate < todayStart) {
            _statusMessage.value = "La fecha del examen debe ser posterior a hoy."
            return
        }

        viewModelScope.launch {
            repository.updateExam(exam)
            _statusMessage.value = "Examen actualizado"
        }
    }

    fun deleteExam(examId: Long) {
        viewModelScope.launch {
            repository.deleteExam(examId)
            _statusMessage.value = "Examen eliminado"
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
            _statusMessage.value = "Objetivo eliminado"
        }
    }

    fun deleteFlashcard(flashcardId: Long) {
        viewModelScope.launch {
            repository.deleteFlashcard(flashcardId)
            _statusMessage.value = "Flashcard eliminada"
        }
    }

    fun applyExamPreparationPlan(sessions: List<StudySessionEntity>) {
        viewModelScope.launch {
            repository.insertSessions(sessions)
            _statusMessage.value = "¡Plan generado! ${sessions.size} sesiones programadas"
        }
    }

    fun addGoal(title: String, type: String, targetValue: Int, deadline: Long) {
        viewModelScope.launch {
            repository.insertGoal(
                GoalEntity(
                    userId = currentUserId,
                    title = title,
                    type = type,
                    targetValue = targetValue,
                    deadline = deadline
                )
            )
        }
    }

    // --- Study Sessions & Planner ---
    fun addSession(
        subjectId: Long,
        topicId: Long?,
        title: String,
        description: String,
        plannedMinutes: Int,
        scheduledDate: String,
        priority: String
    ) {
        viewModelScope.launch {
            repository.insertSession(
                StudySessionEntity(
                    userId = currentUserId,
                    subjectId = subjectId,
                    topicId = topicId,
                    title = title,
                    description = description,
                    plannedMinutes = plannedMinutes,
                    scheduledDate = scheduledDate,
                    priority = priority
                )
            )
            _statusMessage.value = "Sesión programada"
        }
    }

    fun generateDailyPlan() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val subjectsList = _subjects.value
            val examsList = _exams.value
            val allTopics = mutableListOf<TopicEntity>()
            subjectsList.forEach { s ->
                allTopics.addAll(repository.getTopicsListForSubject(s.id))
            }

            val priorities = StudyPlannerService.calculateTopicPriorities(subjectsList, allTopics, examsList)
            val availableMins = (user.dailyAvailableHours * 60).toInt().coerceAtLeast(30)
            val plan = StudyPlannerService.generateDailyPlan(
                userId = user.id,
                todayDateString = repository.getTodayString(),
                availableMinutes = availableMins,
                preferredSessionMinutes = user.preferredSessionMinutes,
                prioritizedTopics = priorities
            )

            if (plan.isNotEmpty()) {
                repository.insertSessions(plan)
                _statusMessage.value = "Plan generado: ${plan.size} sesiones optimizadas"
            } else {
                _statusMessage.value = "Agrega temas para que podamos planificar tu día"
            }
        }
    }

    fun reorganizeAcademicDebt() {
        viewModelScope.launch {
            repository.reorganizeAcademicDebt(currentUserId)
            _statusMessage.value = "Deuda académica reorganizada exitosamente"
        }
    }

    fun updateSession(session: StudySessionEntity) {
        viewModelScope.launch {
            repository.updateSession(session)
            _statusMessage.value = "Sesión actualizada"
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun skipSession(sessionId: Long, reason: String? = null) {
        viewModelScope.launch {
            repository.skipSession(sessionId, reason)
            _statusMessage.value = "Sesión omitida"
        }
    }

    // --- Focus Mode Timer ---
    fun startFocusSession(session: StudySessionEntity) {
        _activeSession.value = session
        val totalSecs = session.plannedMinutes * 60
        _timerTotalSeconds.value = totalSecs
        _timerRemainingSeconds.value = totalSecs
        _effectiveSeconds.value = 0
        _timerPausesCount.value = 0
        _isTimerPaused.value = false
        _isTimerRunning.value = true

        sessionStartTimeMillis = System.currentTimeMillis()
        totalPausedDurationMillis = 0L
        pauseStartTimeMillis = 0L

        viewModelScope.launch {
            repository.startSession(session.id)
        }

        startTimerTicker()
    }

    private fun startTimerTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && _timerRemainingSeconds.value > 0) {
                delay(500)
                if (!_isTimerPaused.value) {
                    val now = System.currentTimeMillis()
                    val effectiveSecs = ((now - sessionStartTimeMillis - totalPausedDurationMillis) / 1000).toInt().coerceAtLeast(0)
                    _effectiveSeconds.value = effectiveSecs
                    val remaining = (_timerTotalSeconds.value - effectiveSecs).coerceAtLeast(0)
                    _timerRemainingSeconds.value = remaining
                    if (remaining <= 0) {
                        _isTimerRunning.value = false
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        if (_isTimerRunning.value && !_isTimerPaused.value) {
            _isTimerPaused.value = true
            _timerPausesCount.value += 1
            pauseStartTimeMillis = System.currentTimeMillis()
        }
    }

    fun resumeTimer() {
        if (_isTimerRunning.value && _isTimerPaused.value) {
            val now = System.currentTimeMillis()
            totalPausedDurationMillis += (now - pauseStartTimeMillis).coerceAtLeast(0L)
            _isTimerPaused.value = false
        }
    }

    fun finishFocusSession(
        evaluation: String, // "completed", "partially_completed", "not_completed"
        uncompletedReason: String? = null,
        notes: String = ""
    ) {
        val session = _activeSession.value ?: return
        timerJob?.cancel()
        _isTimerRunning.value = false
        _isTimerPaused.value = false

        // Compute actual minutes from real measured elapsed seconds
        val secs = _effectiveSeconds.value
        val actualMinutes = (secs / 60).coerceAtLeast(if (secs >= 30) 1 else 0)

        viewModelScope.launch {
            repository.completeSession(
                sessionId = session.id,
                actualMinutes = actualMinutes,
                evaluation = evaluation,
                uncompletedReason = uncompletedReason,
                pausesCount = _timerPausesCount.value,
                notes = notes
            )
            _activeSession.value = null
            if (evaluation == "completed") {
                NotificationHelper.sendSessionCompletedNotification(
                    context = getApplication<Application>(),
                    sessionTitle = session.title,
                    durationMinutes = actualMinutes
                )
            }
            _statusMessage.value = when (evaluation) {
                "completed" -> "🎉 ¡Sesión completada exitosamente!"
                "partially_completed" -> "Sesión guardada como parcial (${actualMinutes} min)"
                else -> "Sesión guardada en tu historial de hábitos"
            }
        }
    }

    fun cancelFocusSession() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _isTimerPaused.value = false
        _activeSession.value = null
    }

    fun deleteAllUserData() {
        val userId = currentUserId
        viewModelScope.launch {
            repository.deleteAllUserData(userId)
            _statusMessage.value = "Todos tus datos han sido eliminados"
            logout()
        }
    }

    // --- AI Tutor Actions ---
    fun sendAIMessage(
        text: String,
        isTutorMode: Boolean,
        subjectId: Long? = null,
        topicId: Long? = null
    ) {
        if (text.isBlank()) return
        val userMsg = AIMessageEntity(
            userId = currentUserId,
            subjectId = subjectId,
            topicId = topicId,
            role = "user",
            content = text,
            isTutorMode = isTutorMode
        )

        viewModelScope.launch {
            repository.insertAIMessage(userMsg)
            _isAILoading.value = true

            val subjectName = _subjects.value.find { it.id == subjectId }?.name
            val topicName = _selectedSubjectTopics.value.find { it.id == topicId }?.name

            val aiResult = aiProvider.askTutor(
                userMessage = text,
                subjectName = subjectName,
                topicName = topicName,
                contextMaterial = null,
                isTutorMode = isTutorMode
            )

            val modelReply = aiResult.getOrDefault("El tutor no pudo responder en este momento.")
            val modelMsg = AIMessageEntity(
                userId = currentUserId,
                subjectId = subjectId,
                topicId = topicId,
                role = "model",
                content = modelReply,
                isTutorMode = isTutorMode
            )
            repository.insertAIMessage(modelMsg)
            _isAILoading.value = false
        }
    }

    fun generateFlashcardsForTopic(subjectId: Long?, topicId: Long? = null, count: Int = 3) {
        viewModelScope.launch {
            _isAILoading.value = true
            val subject = _subjects.value.find { it.id == subjectId }
            val topic = topicId?.let { repository.getTopicById(it) }
            val subName = subject?.name ?: (_subjects.value.firstOrNull()?.name ?: "Estudio Universitario")
            val topName = topic?.name ?: "Conceptos Clave"
            val effectiveSubId = subject?.id ?: (_subjects.value.firstOrNull()?.id ?: 0L)

            val result = aiProvider.generateFlashcards(subName, topName, count)
            result.onSuccess { cards ->
                val entities = cards.map {
                    FlashcardEntity(
                        userId = currentUserId,
                        subjectId = effectiveSubId,
                        topicId = topicId,
                        question = it.question,
                        answer = it.answer,
                        difficulty = it.difficulty
                    )
                }
                repository.insertFlashcards(entities)
                _statusMessage.value = "${entities.size} flashcards generadas"
            }.onFailure {
                _statusMessage.value = "Error al generar flashcards: ${it.message}"
            }
            _isAILoading.value = false
        }
    }

    fun generateExamQuestionsForTopic(subjectId: Long?, topicId: Long? = null, count: Int = 3) {
        viewModelScope.launch {
            _isAILoading.value = true
            val subject = _subjects.value.find { it.id == subjectId }
            val topic = topicId?.let { repository.getTopicById(it) }
            val subName = subject?.name ?: (_subjects.value.firstOrNull()?.name ?: "Estudio Universitario")
            val topName = topic?.name ?: "Conceptos Clave"
            val effectiveSubId = subject?.id ?: (_subjects.value.firstOrNull()?.id ?: 0L)

            repository.clearExamQuestions(currentUserId)
            val result = aiProvider.generateExamQuestions(subName, topName, count)
            result.onSuccess { questions ->
                val entities = questions.map { q ->
                    val optionsJson = org.json.JSONArray(q.options).toString()
                    ExamQuestionEntity(
                        userId = currentUserId,
                        subjectId = effectiveSubId,
                        topicId = topicId,
                        questionText = q.questionText,
                        type = q.type,
                        optionsJson = optionsJson,
                        correctAnswer = q.correctAnswer,
                        explanation = q.explanation
                    )
                }
                repository.insertExamQuestions(entities)
                _statusMessage.value = "${entities.size} preguntas de examen preparadas"
            }.onFailure {
                _statusMessage.value = "Error al preparar preguntas: ${it.message}"
            }
            _isAILoading.value = false
        }
    }

    fun answerExamQuestion(question: ExamQuestionEntity, selectedAnswer: String) {
        viewModelScope.launch {
            val isCorrect = selectedAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
            repository.updateExamQuestion(
                question.copy(
                    userSelectedAnswer = selectedAnswer,
                    isCorrect = isCorrect
                )
            )
        }
    }

    fun toggleFlashcardMastered(flashcard: FlashcardEntity) {
        viewModelScope.launch {
            repository.updateFlashcard(
                flashcard.copy(
                    mastered = !flashcard.mastered,
                    reviewCount = flashcard.reviewCount + 1
                )
            )
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun resetAndSeedDemo() {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty(currentUserId)
            _statusMessage.value = "Datos de prueba universitarios cargados"
        }
    }
}
