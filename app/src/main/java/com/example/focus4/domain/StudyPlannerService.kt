package com.example.focus4.domain

import com.example.focus4.data.model.ExamEntity
import com.example.focus4.data.model.StudySessionEntity
import com.example.focus4.data.model.SubjectEntity
import com.example.focus4.data.model.TopicEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

object StudyPlannerService {

    data class TopicPriorityScore(
        val topic: TopicEntity,
        val subject: SubjectEntity,
        val score: Double,
        val reason: String
    )

    data class ExamPlanResult(
        val exam: ExamEntity,
        val subject: SubjectEntity,
        val daysLeft: Long,
        val availableStudyDaysCount: Int,
        val totalRequiredMinutes: Int,
        val totalAvailableMinutes: Int,
        val totalSessionsNeeded: Int,
        val isCapacitySufficient: Boolean,
        val deficitMinutes: Int,
        val plannedSessions: List<StudySessionEntity>
    )

    /**
     * Deterministic algorithm that calculates priority for topics based on:
     * - Exam proximity (within 7 days = +50pts, within 14 days = +30pts)
     * - Topic importance (Crítica = +30, Alta = +20, Media = +10)
     * - Topic difficulty (Difícil = +15, Media = +10)
     * - Topic progress (lower progress = higher priority)
     */
    fun calculateTopicPriorities(
        subjects: List<SubjectEntity>,
        topics: List<TopicEntity>,
        exams: List<ExamEntity>
    ): List<TopicPriorityScore> {
        val now = System.currentTimeMillis()
        val subjectMap = subjects.associateBy { it.id }

        return topics.mapNotNull { topic ->
            val subject = subjectMap[topic.subjectId] ?: return@mapNotNull null

            var score = 0.0
            var reason = "Repaso regular"

            // 1. Exam proximity
            val subjectExams = exams.filter { it.subjectId == subject.id && it.examDate >= now }
                .sortedBy { it.examDate }
            val nextExam = subjectExams.firstOrNull()

            if (nextExam != null) {
                val daysLeft = TimeUnit.MILLISECONDS.toDays(nextExam.examDate - now)
                when {
                    daysLeft <= 3 -> {
                        score += 60.0
                        reason = "Examen en $daysLeft días (Crítico)"
                    }
                    daysLeft <= 7 -> {
                        score += 45.0
                        reason = "Examen en $daysLeft días (Urgente)"
                    }
                    daysLeft <= 14 -> {
                        score += 30.0
                        reason = "Examen en $daysLeft días"
                    }
                    else -> {
                        score += 15.0
                        reason = "Preparación con anticipación"
                    }
                }
            }

            // 2. Topic Importance
            when (topic.importance) {
                "Crítica" -> score += 30.0
                "Alta" -> score += 20.0
                "Media" -> score += 10.0
                else -> score += 5.0
            }

            // 3. Topic Difficulty
            when (topic.difficulty) {
                "Difícil" -> score += 15.0
                "Media" -> score += 10.0
                else -> score += 5.0
            }

            // 4. Progress factor (if low progress, prioritize more)
            val remainingFactor = (100 - topic.progress) / 100.0
            score += remainingFactor * 25.0

            if (topic.status == "dominado") {
                score -= 30.0 // already mastered
            }

            TopicPriorityScore(topic, subject, score, reason)
        }.sortedByDescending { it.score }
    }

    /**
     * Generates a realistic daily plan that strictly respects the user's available time.
     */
    fun generateDailyPlan(
        userId: Long,
        todayDateString: String,
        availableMinutes: Int,
        preferredSessionMinutes: Int,
        prioritizedTopics: List<TopicPriorityScore>
    ): List<StudySessionEntity> {
        val sessionDuration = if (preferredSessionMinutes in 15..60) preferredSessionMinutes else 25
        val maxSessions = (availableMinutes / sessionDuration).coerceAtLeast(1).coerceAtMost(6)

        val resultSessions = mutableListOf<StudySessionEntity>()

        for (i in 0 until maxSessions.coerceAtMost(prioritizedTopics.size)) {
            val item = prioritizedTopics[i]
            val concreteTitle = when {
                item.topic.progress == 0 -> "Comprender conceptos clave de ${item.topic.name}"
                item.topic.progress < 50 -> "Resolver 3 a 5 ejercicios prácticos de ${item.topic.name}"
                else -> "Repaso activo y fijación de ${item.topic.name}"
            }

            val priorityLabel = when {
                item.score >= 70 -> "Crítica"
                item.score >= 50 -> "Alta"
                item.score >= 30 -> "Media"
                else -> "Baja"
            }

            resultSessions.add(
                StudySessionEntity(
                    userId = userId,
                    subjectId = item.subject.id,
                    topicId = item.topic.id,
                    title = concreteTitle,
                    description = "${item.subject.name} • ${item.reason}",
                    plannedMinutes = sessionDuration,
                    actualMinutes = 0,
                    scheduledDate = todayDateString,
                    priority = priorityLabel,
                    status = "planned"
                )
            )
        }

        return resultSessions
    }

    /**
     * Generates a realistic, deterministic preparation plan for an upcoming exam.
     * Calculates days left, filters available study dates by chosen weekdays,
     * computes needed sessions per topic, checks feasibility, and distributes sessions without overloading.
     */
    fun calculateExamPreparation(
        userId: Long,
        exam: ExamEntity,
        subject: SubjectEntity,
        selectedTopics: List<TopicEntity>,
        dailyAvailableMinutes: Int,
        sessionDurationMinutes: Int,
        availableDaysOfWeek: Set<Int>
    ): ExamPlanResult {
        val now = System.currentTimeMillis()
        val daysLeft = TimeUnit.MILLISECONDS.toDays(exam.examDate - now).coerceAtLeast(0)

        // Find available dates between today and exam date matching chosen weekdays
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val examCal = Calendar.getInstance().apply { timeInMillis = exam.examDate }

        val availableDates = mutableListOf<String>()
        var daysChecked = 0
        while (cal.before(examCal) && daysChecked <= 90) {
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (availableDaysOfWeek.contains(dayOfWeek)) {
                availableDates.add(sdf.format(cal.time))
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
            daysChecked++
        }

        // Fallback: If no days matched (e.g. all unchecked or exam is today), at least include today/tomorrow
        if (availableDates.isEmpty()) {
            val fallbackCal = Calendar.getInstance()
            availableDates.add(sdf.format(fallbackCal.time))
        }

        val sessionDuration = if (sessionDurationMinutes in 15..60) sessionDurationMinutes else 25
        val dailyMinutes = dailyAvailableMinutes.coerceIn(15, 360)

        // Calculate required minutes per selected topic
        var totalReqMinutes = 0
        val topicNeededSessions = mutableListOf<Pair<TopicEntity, Int>>()

        selectedTopics.forEach { topic ->
            val pendingProgress = (100 - topic.progress).coerceAtLeast(15)
            val topicMinutes = (topic.estimatedMinutes * (pendingProgress / 100.0)).toInt().coerceAtLeast(sessionDuration)
            val sessionsForTopic = ceil(topicMinutes.toDouble() / sessionDuration).toInt().coerceAtLeast(1)
            totalReqMinutes += sessionsForTopic * sessionDuration
            topicNeededSessions.add(topic to sessionsForTopic)
        }

        val totalSessionsNeeded = topicNeededSessions.sumOf { it.second }
        val totalAvailableMinutes = availableDates.size * dailyMinutes
        val isSufficient = totalAvailableMinutes >= totalReqMinutes
        val deficitMinutes = (totalReqMinutes - totalAvailableMinutes).coerceAtLeast(0)

        // Generate sessions distributed over available dates
        val maxSessionsPerDay = (dailyMinutes / sessionDuration).coerceAtLeast(1)
        val plannedSessions = mutableListOf<StudySessionEntity>()

        var dateIndex = 0
        var currentDaySessions = 0

        for ((topic, count) in topicNeededSessions) {
            for (s in 1..count) {
                if (dateIndex >= availableDates.size) {
                    dateIndex = availableDates.size - 1 // wrap to last available date
                }
                val dateStr = availableDates[dateIndex]

                val title = when (s) {
                    1 -> "Comprender conceptos de ${topic.name}"
                    count -> "Repaso intensivo y ejercicios de ${topic.name}"
                    else -> "Resolver problemas prácticos de ${topic.name} ($s/$count)"
                }

                plannedSessions.add(
                    StudySessionEntity(
                        userId = userId,
                        subjectId = subject.id,
                        topicId = topic.id,
                        title = title,
                        description = "${subject.name} • Preparación para ${exam.title}",
                        plannedMinutes = sessionDuration,
                        actualMinutes = 0,
                        scheduledDate = dateStr,
                        priority = if (daysLeft <= 7) "Crítica" else "Alta",
                        status = "planned"
                    )
                )

                currentDaySessions++
                if (currentDaySessions >= maxSessionsPerDay) {
                    currentDaySessions = 0
                    if (dateIndex < availableDates.size - 1) {
                        dateIndex++
                    }
                }
            }
        }

        return ExamPlanResult(
            exam = exam,
            subject = subject,
            daysLeft = daysLeft,
            availableStudyDaysCount = availableDates.size,
            totalRequiredMinutes = totalReqMinutes,
            totalAvailableMinutes = totalAvailableMinutes,
            totalSessionsNeeded = totalSessionsNeeded,
            isCapacitySufficient = isSufficient,
            deficitMinutes = deficitMinutes,
            plannedSessions = plannedSessions
        )
    }
}
