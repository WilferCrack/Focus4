package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.focus4.data.db.Focus4Database
import com.example.focus4.data.model.ExamEntity
import com.example.focus4.data.model.StudySessionEntity
import com.example.focus4.data.model.SubjectEntity
import com.example.focus4.data.model.TopicEntity
import com.example.focus4.data.repository.Focus4Repository
import com.example.focus4.domain.HabitAnalysisService
import com.example.focus4.domain.StudyPlannerService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app_name from context should be FOCUS4`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FOCUS4", appName)
  }

  @Test
  fun `newly registered user starts with zero academic entities`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemDb = Room.inMemoryDatabaseBuilder(context, Focus4Database::class.java).build()
    val repo = Focus4Repository(inMemDb)

    val regResult = repo.registerUser("Maria", "González", "maria@universidad.edu", "pass123", "Ingeniería")
    assertTrue(regResult.isSuccess)
    val user = regResult.getOrThrow()

    val subjects = repo.getSubjects(user.id).first()
    val exams = repo.getExams(user.id).first()
    val sessions = repo.getSessionsForUser(user.id).first()
    val flashcards = repo.getAllFlashcards(user.id).first()

    assertEquals(0, subjects.size)
    assertEquals(0, exams.size)
    assertEquals(0, sessions.size)
    assertEquals(0, flashcards.size)

    inMemDb.close()
  }

  @Test
  fun `deleting subject cascades and removes related topics, exams and sessions`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemDb = Room.inMemoryDatabaseBuilder(context, Focus4Database::class.java).build()
    val repo = Focus4Repository(inMemDb)

    val user = repo.registerUser("Carlos", "Perez", "carlos@universidad.edu", "pass123").getOrThrow()
    val subId = repo.insertSubject(SubjectEntity(userId = user.id, name = "Física II"))

    val topId = repo.insertTopic(TopicEntity(subjectId = subId, name = "Electromagnetismo"))
    val examId = repo.insertExam(ExamEntity(userId = user.id, subjectId = subId, title = "Primer Parcial", examDate = System.currentTimeMillis() + 1000000))
    val sessId = repo.insertSession(StudySessionEntity(userId = user.id, subjectId = subId, topicId = topId, title = "Ley de Gauss", scheduledDate = "2026-10-01"))

    assertEquals(1, repo.getSubjects(user.id).first().size)
    assertEquals(1, repo.getTopicsListForSubject(subId).size)
    assertEquals(1, repo.getExams(user.id).first().size)
    assertEquals(1, repo.getSessionsForUser(user.id).first().size)

    // Delete subject
    repo.deleteSubject(subId)

    // Verify all associated entities are deleted and no orphans remain
    assertEquals(0, repo.getSubjects(user.id).first().size)
    assertEquals(0, repo.getTopicsListForSubject(subId).size)
    assertEquals(0, repo.getExams(user.id).first().size)
    assertEquals(0, repo.getSessionsForUser(user.id).first().size)

    inMemDb.close()
  }

  @Test
  fun `study planner prioritizes topic when exam is near`() {
    val subject = SubjectEntity(id = 1L, userId = 1L, name = "Análisis Matemático II")
    val topic = TopicEntity(id = 10L, subjectId = 1L, name = "Integrales por partes", progress = 20)
    val exam = ExamEntity(
      id = 100L,
      userId = 1L,
      subjectId = 1L,
      title = "Parcial",
      examDate = System.currentTimeMillis() + (3L * 24 * 60 * 60 * 1000) // in 3 days
    )

    val priorities = StudyPlannerService.calculateTopicPriorities(
      listOf(subject),
      listOf(topic),
      listOf(exam)
    )

    assertEquals(1, priorities.size)
    assertTrue(priorities[0].score >= 60.0)
    assertTrue(priorities[0].reason.contains("Crítico"))

    val plan = StudyPlannerService.generateDailyPlan(
      userId = 1L,
      todayDateString = "2026-10-01",
      availableMinutes = 60,
      preferredSessionMinutes = 25,
      prioritizedTopics = priorities
    )

    assertEquals(1, plan.size) // 1 prioritized topic provided
    assertEquals(25, plan[0].plannedMinutes)
    assertEquals("Crítica", plan[0].priority)
  }

  @Test
  fun `habit analysis calculates streaks and uncompleted reasons accurately`() {
    val sessions = listOf(
      StudySessionEntity(
        id = 1L,
        userId = 1L,
        subjectId = 1L,
        title = "Sesión 1",
        plannedMinutes = 25,
        actualMinutes = 25,
        scheduledDate = "2026-10-01",
        status = "completed"
      ),
      StudySessionEntity(
        id = 2L,
        userId = 1L,
        subjectId = 1L,
        title = "Sesión 2",
        plannedMinutes = 25,
        actualMinutes = 10,
        scheduledDate = "2026-10-01",
        status = "partially_completed"
      ),
      StudySessionEntity(
        id = 3L,
        userId = 1L,
        subjectId = 1L,
        title = "Sesión 3",
        plannedMinutes = 25,
        actualMinutes = 0,
        scheduledDate = "2026-10-01",
        status = "skipped",
        uncompletedReason = "Me distraje"
      )
    )

    val report = HabitAnalysisService.analyzeHabits(sessions)
    assertEquals(3, report.totalSessionsCount)
    assertEquals(1, report.completedCount)
    assertEquals(1, report.partialCount)
    assertEquals(1, report.skippedCount)
    assertEquals(35, report.totalMinutesStudied)
    assertEquals("Me distraje", report.mostFrequentUncompletedReason)
    assertTrue(report.hasEnoughData)
  }

  @Test
  fun `ownership isolates data completely between User A and User B`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemDb = Room.inMemoryDatabaseBuilder(context, Focus4Database::class.java).build()
    val repo = Focus4Repository(inMemDb)

    val userA = repo.registerUser("Ana", "Rios", "ana@uni.edu", "pass123").getOrThrow()
    val userB = repo.registerUser("Bernardo", "Diaz", "bernardo@uni.edu", "pass123").getOrThrow()

    val subA = repo.insertSubject(SubjectEntity(userId = userA.id, name = "Derecho Romano"))
    val subB = repo.insertSubject(SubjectEntity(userId = userB.id, name = "Anatomía I"))

    val subjectsA = repo.getSubjects(userA.id).first()
    val subjectsB = repo.getSubjects(userB.id).first()

    assertEquals(1, subjectsA.size)
    assertEquals("Derecho Romano", subjectsA[0].name)

    assertEquals(1, subjectsB.size)
    assertEquals("Anatomía I", subjectsB[0].name)

    inMemDb.close()
  }

  @Test
  fun `create exam persists exact real date and notes`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemDb = Room.inMemoryDatabaseBuilder(context, Focus4Database::class.java).build()
    val repo = Focus4Repository(inMemDb)

    val user = repo.registerUser("Elena", "Gomez", "elena@uni.edu", "pass123").getOrThrow()
    val subId = repo.insertSubject(SubjectEntity(userId = user.id, name = "Bioquímica"))

    val examDate = 1774000000000L
    val examId = repo.insertExam(
      ExamEntity(
        userId = user.id,
        subjectId = subId,
        title = "Segundo Parcial",
        examDate = examDate,
        notes = "Unidades 4 a 7",
        priority = "Crítica"
      )
    )

    val examLoaded = repo.getExams(user.id).first().firstOrNull()
    assertNotNull(examLoaded)
    assertEquals("Segundo Parcial", examLoaded?.title)
    assertEquals(examDate, examLoaded?.examDate)
    assertEquals("Unidades 4 a 7", examLoaded?.notes)

    inMemDb.close()
  }

  @Test
  fun `complete session records actualMinutes and advances topic progress`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemDb = Room.inMemoryDatabaseBuilder(context, Focus4Database::class.java).build()
    val repo = Focus4Repository(inMemDb)

    val user = repo.registerUser("Lucas", "Vega", "lucas@uni.edu", "pass123").getOrThrow()
    val subId = repo.insertSubject(SubjectEntity(userId = user.id, name = "Álgebra Lineal"))
    val topId = repo.insertTopic(TopicEntity(subjectId = subId, name = "Espacios Vectoriales", progress = 10))

    val sessId = repo.insertSession(
      StudySessionEntity(
        userId = user.id,
        subjectId = subId,
        topicId = topId,
        title = "Ejercicios de bases y dimensión",
        plannedMinutes = 30,
        actualMinutes = 0,
        scheduledDate = repo.getTodayString(),
        status = "planned"
      )
    )

    // Complete session with 28 actual minutes studied
    repo.completeSession(
      sessionId = sessId,
      actualMinutes = 28,
      evaluation = "completed",
      notes = "Entendí el teorema de dimensión"
    )

    val session = repo.getSessionsForUser(user.id).first().first()
    assertEquals("completed", session.status)
    assertEquals(28, session.actualMinutes)

    // Topic progress advances from 10 to 35
    val topic = repo.getTopicById(topId)
    assertNotNull(topic)
    assertEquals(35, topic?.progress)
    assertEquals("en_progreso", topic?.status)

    inMemDb.close()
  }

  @Test
  fun `exam preparation engine calculates deficit when daily capacity is insufficient`() {
    val subject = SubjectEntity(id = 1L, userId = 1L, name = "Termodinámica")
    val topic1 = TopicEntity(id = 10L, subjectId = 1L, name = "Primer Principio", estimatedMinutes = 120, progress = 0)
    val topic2 = TopicEntity(id = 11L, subjectId = 1L, name = "Ciclos de Carnot", estimatedMinutes = 180, progress = 0)
    val exam = ExamEntity(
      id = 100L,
      userId = 1L,
      subjectId = 1L,
      title = "Parcial Final",
      examDate = System.currentTimeMillis() + (2L * 24 * 60 * 60 * 1000) // 2 days
    )

    val plan = StudyPlannerService.calculateExamPreparation(
      userId = 1L,
      exam = exam,
      subject = subject,
      selectedTopics = listOf(topic1, topic2),
      dailyAvailableMinutes = 60, // only 60m/day = 120m total available in 2 days
      sessionDurationMinutes = 30,
      availableDaysOfWeek = setOf(java.util.Calendar.MONDAY, java.util.Calendar.TUESDAY, java.util.Calendar.WEDNESDAY, java.util.Calendar.THURSDAY, java.util.Calendar.FRIDAY, java.util.Calendar.SATURDAY, java.util.Calendar.SUNDAY)
    )

    // 300 minutes needed vs ~120 available -> capacity is insufficient
    assertEquals(false, plan.isCapacitySufficient)
    assertTrue(plan.deficitMinutes > 0)
    assertTrue(plan.plannedSessions.isNotEmpty())
  }

  @Test
  fun `what to do now engine prioritizes overdue and urgent exam sessions`() {
    val subject = SubjectEntity(id = 1L, userId = 1L, name = "Química Orgánica")
    val topic = TopicEntity(id = 10L, subjectId = 1L, name = "Reacciones de sustitución", progress = 10)
    val exam = ExamEntity(
      id = 100L,
      userId = 1L,
      subjectId = 1L,
      title = "Parcial",
      examDate = System.currentTimeMillis() + (2L * 24 * 60 * 60 * 1000)
    )

    val normalSession = StudySessionEntity(
      id = 1L,
      userId = 1L,
      subjectId = 1L,
      title = "Lectura general",
      scheduledDate = "2026-10-01",
      priority = "Baja",
      status = "planned"
    )

    val urgentSession = StudySessionEntity(
      id = 2L,
      userId = 1L,
      subjectId = 1L,
      topicId = 10L,
      title = "Mecanismos de reacción para parcial",
      scheduledDate = "2026-09-28", // overdue!
      priority = "Crítica",
      status = "planned"
    )

    val rec = com.example.focus4.domain.WhatToDoNowEngine.evaluate(
      subjects = listOf(subject),
      todaySessions = listOf(normalSession),
      pendingSessions = listOf(urgentSession),
      exams = listOf(exam),
      topics = listOf(topic),
      dailyAvailableHours = 2.0f,
      preferredSessionMinutes = 25
    )

    assertEquals(2L, rec.session?.id)
    assertEquals("Crítica", rec.priority)
    assertTrue(rec.reasonLabel.contains("Urgente") || rec.reasonLabel.contains("atrasada"))
  }

  @Test
  fun `delete all user data purges user A and preserves user B completely`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemDb = Room.inMemoryDatabaseBuilder(context, Focus4Database::class.java).build()
    val repo = Focus4Repository(inMemDb)

    val userA = repo.registerUser("A", "User", "a@uni.edu", "pass").getOrThrow()
    val userB = repo.registerUser("B", "User", "b@uni.edu", "pass").getOrThrow()

    val subA = repo.insertSubject(SubjectEntity(userId = userA.id, name = "Materia A"))
    val subB = repo.insertSubject(SubjectEntity(userId = userB.id, name = "Materia B"))

    repo.insertExam(ExamEntity(userId = userA.id, subjectId = subA, title = "Examen A", examDate = 2000000000L))
    repo.insertExam(ExamEntity(userId = userB.id, subjectId = subB, title = "Examen B", examDate = 2000000000L))

    // Purge user A
    repo.deleteAllUserData(userA.id)

    assertEquals(0, repo.getSubjects(userA.id).first().size)
    assertEquals(0, repo.getExams(userA.id).first().size)

    // Verify User B is completely intact
    assertEquals(1, repo.getSubjects(userB.id).first().size)
    assertEquals("Materia B", repo.getSubjects(userB.id).first()[0].name)
    assertEquals(1, repo.getExams(userB.id).first().size)

    inMemDb.close()
  }

  @Test
  fun `user profile update persists visible display name, avatar, and semester without altering login credentials`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemDb = Room.inMemoryDatabaseBuilder(context, Focus4Database::class.java).build()
    val repo = Focus4Repository(inMemDb)

    val registered = repo.registerUser("Valeria", "Mendez", "valeria@uni.edu", "securePass123", "Ingeniería").getOrThrow()
    assertEquals("Valeria Mendez", registered.getEffectiveDisplayName())
    assertEquals("student_01", registered.avatarId)
    assertEquals("1° Semestre", registered.semester)

    // Update profile
    val updated = repo.updateUserProfile(
      userId = registered.id,
      displayName = "Vale Mendez (Dev)",
      career = "Ingeniería de Software",
      semester = "4° Semestre",
      avatarId = "engineering_01",
      profileImageUri = "file:///data/user/0/com.example/files/profiles/profile_1.jpg",
      dailyAvailableHours = 4.5f,
      preferredSessionMinutes = 45,
      mainGoal = "Dominar Estructuras de Datos y aprobar parciales"
    ).getOrThrow()

    assertEquals("Vale Mendez (Dev)", updated.getEffectiveDisplayName())
    assertEquals("engineering_01", updated.avatarId)
    assertEquals("4° Semestre", updated.semester)
    assertEquals("Ingeniería de Software", updated.career)
    assertEquals(4.5f, updated.dailyAvailableHours)
    assertEquals(45, updated.preferredSessionMinutes)

    // Verify login still works with original email and password
    val loginResult = repo.loginUser("valeria@uni.edu", "securePass123")
    assertTrue(loginResult.isSuccess)
    val loggedUser = loginResult.getOrThrow()
    assertEquals("Vale Mendez (Dev)", loggedUser.getEffectiveDisplayName())
    assertEquals("engineering_01", loggedUser.avatarId)

    inMemDb.close()
  }

  @Test
  fun `professional avatar catalog provides valid avatars across disciplines`() {
    val engAvatar = com.example.focus4.ui.components.AvatarCatalog.getById("engineering_01")
    assertEquals("engineering_01", engAvatar.id)
    assertEquals("Ingeniería", engAvatar.category)

    val medAvatar = com.example.focus4.ui.components.AvatarCatalog.getById("medicine_01")
    assertEquals("medicine_01", medAvatar.id)
    assertEquals("Salud", medAvatar.category)

    val lawAvatar = com.example.focus4.ui.components.AvatarCatalog.getById("law_01")
    assertEquals("law_01", lawAvatar.id)
    assertEquals("Derecho", lawAvatar.category)

    // Fallback for null or unknown id
    val fallback = com.example.focus4.ui.components.AvatarCatalog.getById("non_existing_id")
    assertEquals("student_01", fallback.id)
  }
}
