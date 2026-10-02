package com.example.focus4.data.repository

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Focus4Repository(private val database: Focus4Database) {
    private val userDao = database.userDao()
    private val subjectDao = database.subjectDao()
    private val topicDao = database.topicDao()
    private val examDao = database.examDao()
    private val goalDao = database.goalDao()
    private val sessionDao = database.studySessionDao()
    private val flashcardDao = database.flashcardDao()
    private val questionDao = database.examQuestionDao()
    private val aiDao = database.aiMessageDao()
    private val classScheduleDao = database.classScheduleDao()
    private val classAttendanceDao = database.classAttendanceDao()

    // --- Helpers ---
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // --- User & Auth ---
    fun getUserFlow(userId: Long): Flow<UserEntity?> = userDao.getUserById(userId)

    suspend fun getFirstUser(): UserEntity? = userDao.getFirstUser()

    suspend fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        career: String = "Ingeniería / Ciencias"
    ): Result<UserEntity> {
        val existing = userDao.getUserByEmail(email.trim().lowercase())
        if (existing != null) {
            return Result.failure(Exception("Ya existe una cuenta con este correo electrónico."))
        }
        val user = UserEntity(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            displayName = "$firstName $lastName".trim(),
            avatarId = "student_01",
            profileImageUri = null,
            email = email.trim().lowercase(),
            passwordHash = hashPassword(password),
            career = career.trim(),
            semester = "1° Semestre"
        )
        val id = userDao.insertUser(user)
        return Result.success(user.copy(id = id))
    }

    suspend fun loginUser(email: String, password: String): Result<UserEntity> {
        val user = userDao.getUserByEmail(email.trim().lowercase())
            ?: return Result.failure(Exception("Correo electrónico o contraseña incorrectos."))
        if (user.passwordHash != hashPassword(password)) {
            return Result.failure(Exception("Correo electrónico o contraseña incorrectos."))
        }
        return Result.success(user)
    }

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun updateUserProfile(
        userId: Long,
        displayName: String,
        career: String,
        semester: String,
        avatarId: String,
        profileImageUri: String?,
        dailyAvailableHours: Float,
        preferredSessionMinutes: Int,
        mainGoal: String
    ): Result<UserEntity> {
        val currentUser = userDao.getUserById(userId).first()
            ?: return Result.failure(Exception("Usuario no encontrado"))
        val updated = currentUser.copy(
            displayName = displayName.trim(),
            career = career.trim(),
            semester = semester.trim(),
            avatarId = avatarId.trim(),
            profileImageUri = profileImageUri,
            dailyAvailableHours = dailyAvailableHours.coerceIn(0.5f, 16.0f),
            preferredSessionMinutes = preferredSessionMinutes.coerceIn(10, 180),
            mainGoal = mainGoal.trim()
        )
        userDao.updateUser(updated)
        return Result.success(updated)
    }

    // --- Subjects ---
    fun getSubjects(userId: Long): Flow<List<SubjectEntity>> = subjectDao.getSubjectsForUser(userId)

    fun getSubjectByIdFlow(id: Long): Flow<SubjectEntity?> = subjectDao.getSubjectByIdFlow(id)

    suspend fun getSubjectById(id: Long): SubjectEntity? = subjectDao.getSubjectById(id)

    suspend fun insertSubject(subject: SubjectEntity): Long = subjectDao.insertSubject(subject)

    suspend fun updateSubject(subject: SubjectEntity) = subjectDao.updateSubject(subject)

    suspend fun deleteSubject(subjectId: Long) {
        classAttendanceDao.deleteAttendanceBySubjectId(subjectId)
        classScheduleDao.deleteSchedulesBySubjectId(subjectId)
        topicDao.deleteTopicsBySubjectId(subjectId)
        examDao.deleteExamsBySubjectId(subjectId)
        sessionDao.deleteSessionsBySubjectId(subjectId)
        flashcardDao.deleteFlashcardsBySubjectId(subjectId)
        questionDao.deleteQuestionsBySubjectId(subjectId)
        aiDao.deleteMessagesBySubjectId(subjectId)
        subjectDao.deleteSubjectById(subjectId)
    }

    // --- Topics ---
    fun getTopicsForSubject(subjectId: Long): Flow<List<TopicEntity>> = topicDao.getTopicsForSubject(subjectId)

    suspend fun getTopicsListForSubject(subjectId: Long): List<TopicEntity> = topicDao.getTopicsListForSubject(subjectId)

    suspend fun getTopicById(id: Long): TopicEntity? = topicDao.getTopicById(id)

    suspend fun insertTopic(topic: TopicEntity): Long = topicDao.insertTopic(topic)

    suspend fun updateTopic(topic: TopicEntity) = topicDao.updateTopic(topic)

    suspend fun deleteTopic(topicId: Long) {
        sessionDao.deleteSessionsByTopicId(topicId)
        flashcardDao.deleteFlashcardsByTopicId(topicId)
        questionDao.deleteQuestionsByTopicId(topicId)
        aiDao.deleteMessagesByTopicId(topicId)
        topicDao.deleteTopicById(topicId)
    }

    // --- Exams ---
    fun getExams(userId: Long): Flow<List<ExamEntity>> = examDao.getExamsForUser(userId)

    fun getUpcomingExams(userId: Long): Flow<List<ExamEntity>> =
        examDao.getUpcomingExams(userId, System.currentTimeMillis())

    fun getNextExam(userId: Long): Flow<ExamEntity?> =
        examDao.getNextExam(userId, System.currentTimeMillis())

    suspend fun insertExam(exam: ExamEntity): Long = examDao.insertExam(exam)

    suspend fun updateExam(exam: ExamEntity) = examDao.updateExam(exam)

    suspend fun deleteExam(examId: Long) = examDao.deleteExamById(examId)

    // --- Goals ---
    fun getGoals(userId: Long): Flow<List<GoalEntity>> = goalDao.getGoalsForUser(userId)

    suspend fun insertGoal(goal: GoalEntity): Long = goalDao.insertGoal(goal)

    suspend fun updateGoal(goal: GoalEntity) = goalDao.updateGoal(goal)

    suspend fun deleteGoal(goalId: Long) = goalDao.deleteGoalById(goalId)

    // --- Study Sessions ---
    fun getSessionsForUser(userId: Long): Flow<List<StudySessionEntity>> =
        sessionDao.getSessionsForUser(userId)

    fun getSessionsByDate(userId: Long, date: String): Flow<List<StudySessionEntity>> =
        sessionDao.getSessionsByDate(userId, date)

    fun getPendingSessions(userId: Long, today: String = getTodayString()): Flow<List<StudySessionEntity>> =
        sessionDao.getPendingSessions(userId, today)

    suspend fun getSessionById(id: Long): StudySessionEntity? = sessionDao.getSessionById(id)

    fun getSessionByIdFlow(id: Long): Flow<StudySessionEntity?> = sessionDao.getSessionByIdFlow(id)

    suspend fun insertSession(session: StudySessionEntity): Long = sessionDao.insertSession(session)

    suspend fun insertSessions(sessions: List<StudySessionEntity>) = sessionDao.insertSessions(sessions)

    suspend fun updateSession(session: StudySessionEntity) = sessionDao.updateSession(session)

    suspend fun deleteSession(sessionId: Long) = sessionDao.deleteSessionById(sessionId)

    fun getCompletedSessionsFlow(userId: Long): Flow<List<StudySessionEntity>> =
        sessionDao.getCompletedSessionsFlow(userId)

    suspend fun getAllSessionsForUser(userId: Long): List<StudySessionEntity> =
        sessionDao.getAllSessionsForUser(userId)

    // Session Execution State
    suspend fun startSession(sessionId: Long) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        sessionDao.updateSession(
            session.copy(
                status = "active",
                startTime = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun completeSession(
        sessionId: Long,
        actualMinutes: Int,
        evaluation: String, // "completed", "partially_completed", "not_completed"
        distractionCount: Int = 0,
        uncompletedReason: String? = null,
        pausesCount: Int = 0,
        notes: String = ""
    ) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val status = when (evaluation) {
            "completed" -> "completed"
            "partially_completed" -> "partially_completed"
            else -> "skipped"
        }
        val completionPercentage = when (evaluation) {
            "completed" -> 100
            "partially_completed" -> 50
            else -> 0
        }

        sessionDao.updateSession(
            session.copy(
                actualMinutes = actualMinutes,
                status = status,
                completionPercentage = completionPercentage,
                distractionCount = distractionCount,
                uncompletedReason = uncompletedReason,
                pausesCount = pausesCount,
                notes = notes,
                endTime = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        // If topic exists and session was completed, advance topic progress!
        session.topicId?.let { topicId ->
            val topic = topicDao.getTopicById(topicId)
            if (topic != null) {
                val newProgress = when (evaluation) {
                    "completed" -> (topic.progress + 25).coerceAtMost(100)
                    "partially_completed" -> (topic.progress + 10).coerceAtMost(100)
                    else -> topic.progress
                }
                val newStatus = when {
                    newProgress >= 100 -> "dominado"
                    newProgress > 0 -> "en_progreso"
                    else -> "pendiente"
                }
                topicDao.updateTopic(topic.copy(progress = newProgress, status = newStatus, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    suspend fun skipSession(sessionId: Long, reason: String?) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        sessionDao.updateSession(
            session.copy(
                status = "skipped",
                uncompletedReason = reason,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // --- Academic Debt Reorganization ---
    suspend fun reorganizeAcademicDebt(userId: Long) {
        val today = getTodayString()
        val pendingSessions = sessionDao.getPendingSessionsList(userId, today)
        if (pendingSessions.isEmpty()) return

        val user = userDao.getFirstUser()
        val dailyMinutesCapacity = ((user?.dailyAvailableHours ?: 2.0f) * 60).toInt().coerceIn(30, 240)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Sort pending sessions with critical priority first
        val sortedPending = pendingSessions.sortedWith(
            compareByDescending<StudySessionEntity> { it.priority == "Crítica" }
                .thenByDescending { it.priority == "Alta" }
                .thenBy { it.scheduledDate }
        )

        val dayMinutesAllocated = mutableMapOf<String, Int>()
        var dayOffset = 0

        for (session in sortedPending) {
            val sessionMinutes = session.plannedMinutes.coerceIn(15, 90)
            var scheduled = false

            while (!scheduled && dayOffset <= 14) {
                val cal = java.util.Calendar.getInstance()
                cal.add(java.util.Calendar.DAY_OF_YEAR, dayOffset)
                val targetDate = sdf.format(cal.time)

                val alreadyAllocated = dayMinutesAllocated.getOrDefault(targetDate, 0)
                if (alreadyAllocated + sessionMinutes <= dailyMinutesCapacity) {
                    dayMinutesAllocated[targetDate] = alreadyAllocated + sessionMinutes
                    sessionDao.updateSession(
                        session.copy(
                            scheduledDate = targetDate,
                            status = "planned",
                            uncompletedReason = null,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    scheduled = true
                } else {
                    dayOffset++
                }
            }

            if (!scheduled) {
                val cal = java.util.Calendar.getInstance()
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                sessionDao.updateSession(
                    session.copy(
                        scheduledDate = sdf.format(cal.time),
                        status = "planned",
                        uncompletedReason = null,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun deleteAllUserData(userId: Long) {
        val userSubjects = subjectDao.getSubjectsListForUser(userId)
        userSubjects.forEach { sub ->
            deleteSubject(sub.id)
        }
        classAttendanceDao.deleteAttendanceByUserId(userId)
        classScheduleDao.deleteSchedulesByUserId(userId)
        examDao.deleteExamsByUserId(userId)
        goalDao.deleteGoalsByUserId(userId)
        sessionDao.deleteSessionsByUserId(userId)
        flashcardDao.deleteFlashcardsByUserId(userId)
        questionDao.clearQuestionsForUser(userId)
        aiDao.clearHistory(userId)
    }

    // --- Flashcards ---
    fun getAllFlashcards(userId: Long): Flow<List<FlashcardEntity>> = flashcardDao.getAllFlashcards(userId)

    fun getFlashcardsForSubject(userId: Long, subjectId: Long): Flow<List<FlashcardEntity>> =
        flashcardDao.getFlashcardsForSubject(userId, subjectId)

    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long = flashcardDao.insertFlashcard(flashcard)

    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>) = flashcardDao.insertFlashcards(flashcards)

    suspend fun updateFlashcard(flashcard: FlashcardEntity) = flashcardDao.updateFlashcard(flashcard)

    suspend fun deleteFlashcard(flashcardId: Long) = flashcardDao.deleteFlashcardById(flashcardId)

    // --- Exam Questions ---
    fun getExamQuestions(userId: Long): Flow<List<ExamQuestionEntity>> = questionDao.getExamQuestions(userId)

    suspend fun insertExamQuestions(questions: List<ExamQuestionEntity>) = questionDao.insertQuestions(questions)

    suspend fun updateExamQuestion(question: ExamQuestionEntity) = questionDao.updateQuestion(question)

    suspend fun clearExamQuestions(userId: Long) = questionDao.clearQuestionsForUser(userId)

    // --- AI Messages ---
    fun getAIMessages(userId: Long): Flow<List<AIMessageEntity>> = aiDao.getAIMessages(userId)

    suspend fun insertAIMessage(message: AIMessageEntity): Long = aiDao.insertAIMessage(message)

    suspend fun clearAIHistory(userId: Long) = aiDao.clearHistory(userId)

    // --- Seed Demo Data (Real university data) ---
    suspend fun seedDemoDataIfEmpty(userId: Long) {
        val existing = database.subjectDao().getSubjectById(1)
        if (existing != null) return // Already seeded or created

        val subject1Id = subjectDao.insertSubject(
            SubjectEntity(
                userId = userId,
                name = "Análisis Matemático II",
                description = "Cálculo en varias variables, integrales dobles y triples, ecuaciones diferenciales.",
                colorHex = "#2563EB",
                iconName = "calculate",
                teacher = "Prof. Martinez",
                semester = "1° Semestre",
                priority = "Crítica"
            )
        )
        val subject2Id = subjectDao.insertSubject(
            SubjectEntity(
                userId = userId,
                name = "Física I",
                description = "Mecánica clásica, cinemática, dinámica rotacional y leyes de conservación.",
                colorHex = "#10B981",
                iconName = "science",
                teacher = "Dra. Gomez",
                semester = "1° Semestre",
                priority = "Alta"
            )
        )
        val subject3Id = subjectDao.insertSubject(
            SubjectEntity(
                userId = userId,
                name = "Algoritmos y Estructuras",
                description = "Complejidad algorítmica, árboles binarios, grafos y programación dinámica.",
                colorHex = "#8B5CF6",
                iconName = "code",
                teacher = "Ing. Perez",
                semester = "1° Semestre",
                priority = "Media"
            )
        )

        // Topics
        val topic1Id = topicDao.insertTopic(
            TopicEntity(
                subjectId = subject1Id,
                name = "Integrales por partes y sustitución",
                description = "Métodos avanzados de integración de productos algebraicos y trigonométricos.",
                estimatedMinutes = 60,
                difficulty = "Media",
                importance = "Crítica",
                progress = 40,
                status = "en_progreso"
            )
        )
        val topic2Id = topicDao.insertTopic(
            TopicEntity(
                subjectId = subject1Id,
                name = "Integrales dobles en coordenadas polares",
                description = "Cambio de variables y cálculo de áreas y volúmenes.",
                estimatedMinutes = 90,
                difficulty = "Difícil",
                importance = "Alta",
                progress = 10,
                status = "pendiente"
            )
        )
        val topic3Id = topicDao.insertTopic(
            TopicEntity(
                subjectId = subject2Id,
                name = "Leyes de Newton y diagrama de cuerpo libre",
                description = "Fuerzas de rozamiento, tensión, planos inclinados y aceleración.",
                estimatedMinutes = 45,
                difficulty = "Media",
                importance = "Alta",
                progress = 75,
                status = "en_progreso"
            )
        )
        val topic4Id = topicDao.insertTopic(
            TopicEntity(
                subjectId = subject3Id,
                name = "Árboles binarios de búsqueda (BST)",
                description = "Inserción, eliminación, balanceo y recorridos in-order, pre-order, post-order.",
                estimatedMinutes = 50,
                difficulty = "Media",
                importance = "Alta",
                progress = 50,
                status = "en_progreso"
            )
        )

        // Exams
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, 14)
        examDao.insertExam(
            ExamEntity(
                userId = userId,
                subjectId = subject1Id,
                title = "1° Parcial Análisis Matemático II",
                examDate = cal.timeInMillis,
                notes = "Incluye temas de Unidades 1 a 3. Llevar calculadora científica.",
                priority = "Crítica"
            )
        )
        cal.add(java.util.Calendar.DAY_OF_YEAR, 10)
        examDao.insertExam(
            ExamEntity(
                userId = userId,
                subjectId = subject2Id,
                title = "Examen Parcial de Física I",
                examDate = cal.timeInMillis,
                notes = "Laboratorio y problemas de dinámica.",
                priority = "Alta"
            )
        )

        // Today's Date
        val today = getTodayString()

        // Sessions for today
        sessionDao.insertSession(
            StudySessionEntity(
                userId = userId,
                subjectId = subject1Id,
                topicId = topic1Id,
                title = "Resolver 5 ejercicios de integrales por partes",
                description = "Ejercicios pares de la guía práctica N° 2.",
                plannedMinutes = 25,
                actualMinutes = 25,
                scheduledDate = today,
                status = "completed",
                priority = "Crítica",
                completionPercentage = 100
            )
        )
        sessionDao.insertSession(
            StudySessionEntity(
                userId = userId,
                subjectId = subject2Id,
                topicId = topic3Id,
                title = "Repasar leyes de Newton y hacer 3 problemas",
                description = "Problemas de plano inclinado con fricción.",
                plannedMinutes = 25,
                actualMinutes = 0,
                scheduledDate = today,
                status = "planned",
                priority = "Alta",
                completionPercentage = 0
            )
        )
        sessionDao.insertSession(
            StudySessionEntity(
                userId = userId,
                subjectId = subject3Id,
                topicId = topic4Id,
                title = "Implementar recorrido in-order en BST",
                description = "Escribir función recursiva e iterativa.",
                plannedMinutes = 25,
                actualMinutes = 0,
                scheduledDate = today,
                status = "planned",
                priority = "Media",
                completionPercentage = 0
            )
        )

        // Flashcards
        flashcardDao.insertFlashcard(
            FlashcardEntity(
                userId = userId,
                subjectId = subject1Id,
                topicId = topic1Id,
                question = "¿Cuál es la fórmula de integración por partes?",
                answer = "∫ u dv = u·v - ∫ v du (Regla mnemotécnica: Un Día Vi Una Vaca Vestida De Uniforme)",
                difficulty = "Fácil"
            )
        )
        flashcardDao.insertFlashcard(
            FlashcardEntity(
                userId = userId,
                subjectId = subject2Id,
                topicId = topic3Id,
                question = "¿Qué establece la Tercera Ley de Newton?",
                answer = "A toda acción se opone una reacción igual en magnitud y dirección pero de sentido contrario.",
                difficulty = "Fácil"
            )
        )
    }

    // --- Class Schedules CRUD & Management ---
    fun getSchedules(userId: Long): Flow<List<ClassScheduleEntity>> =
        classScheduleDao.getSchedulesForUser(userId)

    suspend fun getSchedulesList(userId: Long): List<ClassScheduleEntity> =
        classScheduleDao.getSchedulesListForUser(userId)

    fun getSchedulesForDay(userId: Long, dayOfWeek: Int): Flow<List<ClassScheduleEntity>> =
        classScheduleDao.getSchedulesForDay(userId, dayOfWeek)

    suspend fun getSchedulesListForDay(userId: Long, dayOfWeek: Int): List<ClassScheduleEntity> =
        classScheduleDao.getSchedulesListForDay(userId, dayOfWeek)

    fun getSchedulesForSubject(subjectId: Long): Flow<List<ClassScheduleEntity>> =
        classScheduleDao.getSchedulesForSubject(subjectId)

    suspend fun getScheduleById(id: Long): ClassScheduleEntity? =
        classScheduleDao.getScheduleById(id)

    suspend fun insertSchedule(schedule: ClassScheduleEntity): Long =
        classScheduleDao.insertSchedule(schedule)

    suspend fun updateSchedule(schedule: ClassScheduleEntity) =
        classScheduleDao.updateSchedule(schedule)

    suspend fun deleteSchedule(scheduleId: Long) {
        classAttendanceDao.deleteAttendanceByScheduleId(scheduleId)
        classScheduleDao.deleteScheduleById(scheduleId)
    }

    suspend fun duplicateSchedule(
        scheduleId: Long,
        newDayOfWeek: Int,
        newStartTime: String? = null,
        newEndTime: String? = null
    ): Result<ClassScheduleEntity> {
        val original = classScheduleDao.getScheduleById(scheduleId)
            ?: return Result.failure(Exception("Clase no encontrada"))
        val copy = original.copy(
            id = 0,
            dayOfWeek = newDayOfWeek,
            startTime = newStartTime ?: original.startTime,
            endTime = newEndTime ?: original.endTime,
            createdAt = System.currentTimeMillis()
        )
        val newId = classScheduleDao.insertSchedule(copy)
        return Result.success(copy.copy(id = newId))
    }

    private fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.trim().split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }

    suspend fun detectScheduleConflicts(
        userId: Long,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        excludeScheduleId: Long? = null
    ): List<ClassScheduleEntity> {
        val schedules = classScheduleDao.getSchedulesListForDay(userId, dayOfWeek)
            .filter { it.isActive && (excludeScheduleId == null || it.id != excludeScheduleId) }

        val startMinutes = parseTimeToMinutes(startTime)
        val endMinutes = parseTimeToMinutes(endTime)

        return schedules.filter { other ->
            val otherStart = parseTimeToMinutes(other.startTime)
            val otherEnd = parseTimeToMinutes(other.endTime)
            maxOf(startMinutes, otherStart) < minOf(endMinutes, otherEnd)
        }
    }

    // --- Class Attendance ---
    fun getAttendanceForUser(userId: Long): Flow<List<ClassAttendanceEntity>> =
        classAttendanceDao.getAttendanceForUser(userId)

    suspend fun getAttendanceListForUser(userId: Long): List<ClassAttendanceEntity> =
        classAttendanceDao.getAttendanceListForUser(userId)

    fun getAttendanceForDate(userId: Long, date: String): Flow<List<ClassAttendanceEntity>> =
        classAttendanceDao.getAttendanceForDate(userId, date)

    suspend fun getAttendanceListForDate(userId: Long, date: String): List<ClassAttendanceEntity> =
        classAttendanceDao.getAttendanceListForDate(userId, date)

    fun getAttendanceForSubject(userId: Long, subjectId: Long): Flow<List<ClassAttendanceEntity>> =
        classAttendanceDao.getAttendanceForSubject(userId, subjectId)

    suspend fun getAttendanceForScheduleAndDate(scheduleId: Long, date: String): ClassAttendanceEntity? =
        classAttendanceDao.getAttendanceForScheduleAndDate(scheduleId, date)

    suspend fun markAttendance(
        userId: Long,
        scheduleId: Long,
        subjectId: Long,
        date: String,
        startTime: String,
        endTime: String,
        status: String,
        lateMinutes: Int = 0,
        notes: String = "",
        rescheduledDate: String? = null,
        rescheduledStartTime: String? = null,
        rescheduledEndTime: String? = null
    ): Long {
        val existing = classAttendanceDao.getAttendanceForScheduleAndDate(scheduleId, date)
        return if (existing != null) {
            val updated = existing.copy(
                status = status,
                lateMinutes = lateMinutes,
                notes = notes,
                rescheduledDate = rescheduledDate,
                rescheduledStartTime = rescheduledStartTime,
                rescheduledEndTime = rescheduledEndTime,
                updatedAt = System.currentTimeMillis()
            )
            classAttendanceDao.updateAttendance(updated)
            existing.id
        } else {
            val newEntry = ClassAttendanceEntity(
                userId = userId,
                scheduleId = scheduleId,
                subjectId = subjectId,
                date = date,
                startTime = startTime,
                endTime = endTime,
                status = status,
                lateMinutes = lateMinutes,
                notes = notes,
                rescheduledDate = rescheduledDate,
                rescheduledStartTime = rescheduledStartTime,
                rescheduledEndTime = rescheduledEndTime
            )
            classAttendanceDao.insertAttendance(newEntry)
        }
    }

    suspend fun deleteAttendance(id: Long) = classAttendanceDao.deleteAttendanceById(id)

    data class SubjectAttendanceStats(
        val subjectId: Long,
        val subjectName: String,
        val totalClassesRecorded: Int,
        val attendedCount: Int,
        val absentCount: Int,
        val lateCount: Int,
        val cancelledCount: Int,
        val excusedCount: Int,
        val rescheduledCount: Int,
        val attendancePercentage: Int // attended / (attended + absent + late) * 100
    )

    data class OverallAttendanceStats(
        val totalClassesRecorded: Int,
        val attendedCount: Int,
        val absentCount: Int,
        val lateCount: Int,
        val cancelledCount: Int,
        val excusedCount: Int,
        val rescheduledCount: Int,
        val overallAttendancePercentage: Int,
        val bySubject: List<SubjectAttendanceStats>
    )

    suspend fun getAttendanceStats(userId: Long): OverallAttendanceStats {
        val attendances = classAttendanceDao.getAttendanceListForUser(userId)
        val subjects = subjectDao.getSubjectsListForUser(userId)

        val bySubjectStats = subjects.map { subject ->
            val subAtts = attendances.filter { it.subjectId == subject.id }
            val attended = subAtts.count { it.status == "ATTENDED" }
            val absent = subAtts.count { it.status == "ABSENT" }
            val late = subAtts.count { it.status == "LATE" }
            val cancelled = subAtts.count { it.status == "CANCELLED" }
            val excused = subAtts.count { it.status == "EXCUSED" }
            val rescheduled = subAtts.count { it.status == "RESCHEDULED" }

            val denominator = attended + absent + late
            val pct = if (denominator > 0) (attended * 100) / denominator else 100

            SubjectAttendanceStats(
                subjectId = subject.id,
                subjectName = subject.name,
                totalClassesRecorded = subAtts.size,
                attendedCount = attended,
                absentCount = absent,
                lateCount = late,
                cancelledCount = cancelled,
                excusedCount = excused,
                rescheduledCount = rescheduled,
                attendancePercentage = pct
            )
        }

        val totalAttended = attendances.count { it.status == "ATTENDED" }
        val totalAbsent = attendances.count { it.status == "ABSENT" }
        val totalLate = attendances.count { it.status == "LATE" }
        val totalCancelled = attendances.count { it.status == "CANCELLED" }
        val totalExcused = attendances.count { it.status == "EXCUSED" }
        val totalRescheduled = attendances.count { it.status == "RESCHEDULED" }

        val totalDenom = totalAttended + totalAbsent + totalLate
        val overallPct = if (totalDenom > 0) (totalAttended * 100) / totalDenom else 100

        return OverallAttendanceStats(
            totalClassesRecorded = attendances.size,
            attendedCount = totalAttended,
            absentCount = totalAbsent,
            lateCount = totalLate,
            cancelledCount = totalCancelled,
            excusedCount = totalExcused,
            rescheduledCount = totalRescheduled,
            overallAttendancePercentage = overallPct,
            bySubject = bySubjectStats
        )
    }

    // --- Appearance Preferences ---
    suspend fun updateAppearancePreferences(
        userId: Long,
        themeMode: String,
        lightColor: String,
        darkColor: String,
        preset: String,
        useDynamicColor: Boolean
    ): Result<UserEntity> {
        val user = userDao.getUserById(userId).first()
            ?: return Result.failure(Exception("Usuario no encontrado"))
        val updated = user.copy(
            themeMode = themeMode,
            lightThemeColor = lightColor,
            darkThemeColor = darkColor,
            themePreset = preset,
            useDynamicColor = useDynamicColor
        )
        userDao.updateUser(updated)
        return Result.success(updated)
    }

    suspend fun updateNotificationSettings(
        userId: Long,
        classReminders: Boolean,
        examReminders: Boolean,
        sessionReminders: Boolean,
        changeAlerts: Boolean,
        defaultMinutes: Int
    ): Result<UserEntity> {
        val user = userDao.getUserById(userId).first()
            ?: return Result.failure(Exception("Usuario no encontrado"))
        val updated = user.copy(
            classRemindersEnabled = classReminders,
            examRemindersEnabled = examReminders,
            sessionRemindersEnabled = sessionReminders,
            changeAlertsEnabled = changeAlerts,
            classReminderDefaultMinutes = defaultMinutes
        )
        userDao.updateUser(updated)
        return Result.success(updated)
    }
}
