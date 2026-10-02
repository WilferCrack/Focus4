package com.example.focus4.domain

import com.example.focus4.data.model.ExamEntity
import com.example.focus4.data.model.StudySessionEntity
import com.example.focus4.data.model.SubjectEntity
import com.example.focus4.data.model.TopicEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object WhatToDoNowEngine {

    data class Recommendation(
        val session: StudySessionEntity?,
        val subject: SubjectEntity?,
        val topic: TopicEntity?,
        val exam: ExamEntity?,
        val title: String,
        val description: String,
        val priority: String,
        val durationMinutes: Int,
        val reasonLabel: String,
        val isOverdue: Boolean,
        val canStartDirectly: Boolean
    )

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Deterministic engine that evaluates what the student should do right now.
     * Evaluates:
     * - Exam proximity (within 2 days = +85pts, within 5 days = +65pts, within 10 days = +40pts)
     * - Retraso / Academic Debt (+60pts for overdue sessions)
     * - Session priority (Crítica = +40, Alta = +25, Media = +15)
     * - Topic importance & difficulty (+25, +15)
     * - Low topic progress (+up to 20pts)
     * - Availability adherence (fits in daily hours)
     */
    fun evaluate(
        subjects: List<SubjectEntity>,
        todaySessions: List<StudySessionEntity>,
        pendingSessions: List<StudySessionEntity>,
        exams: List<ExamEntity>,
        topics: List<TopicEntity>,
        dailyAvailableHours: Float,
        preferredSessionMinutes: Int
    ): Recommendation {
        val now = System.currentTimeMillis()
        val todayStr = getTodayDateString()
        val subjectMap = subjects.associateBy { it.id }
        val topicMap = topics.associateBy { it.id }

        // 1. Gather all candidates (today's planned sessions + overdue pending sessions)
        val candidateSessions = mutableListOf<StudySessionEntity>()
        todaySessions.filter { it.status == "planned" }.forEach { candidateSessions.add(it) }
        pendingSessions.filter { it.status == "planned" }.forEach { candidateSessions.add(it) }

        if (candidateSessions.isNotEmpty()) {
            val scored = candidateSessions.map { session ->
                val subject = subjectMap[session.subjectId]
                val topic = session.topicId?.let { topicMap[it] }

                // Find if there is an upcoming exam for this subject
                val upcomingExam = exams.filter { it.subjectId == session.subjectId && it.examDate >= now }
                    .minByOrNull { it.examDate }

                var score = 0.0
                var reason = "Sesión programada en tu plan"

                // A. Examen próximo (Urgencia)
                if (upcomingExam != null) {
                    val days = TimeUnit.MILLISECONDS.toDays(upcomingExam.examDate - now)
                    when {
                        days <= 2 -> {
                            score += 85.0
                            reason = "🔴 Parcial en $days días (Urgente)"
                        }
                        days <= 5 -> {
                            score += 65.0
                            reason = "🟠 Parcial en $days días"
                        }
                        days <= 10 -> {
                            score += 40.0
                            reason = "Preparación de examen en $days días"
                        }
                        else -> {
                            score += 20.0
                            reason = "Anticipación para examen"
                        }
                    }
                }

                // B. Retraso (Deuda académica)
                val isOverdue = session.scheduledDate < todayStr
                if (isOverdue) {
                    score += 60.0
                    reason = "⏰ Actividad atrasada que requiere atención"
                }

                // C. Prioridad de la sesión
                when (session.priority) {
                    "Crítica" -> score += 40.0
                    "Alta" -> score += 25.0
                    "Media" -> score += 15.0
                    else -> score += 5.0
                }

                // D. Importancia y dificultad del tema
                if (topic != null) {
                    when (topic.importance) {
                        "Crítica" -> score += 25.0
                        "Alta" -> score += 15.0
                        "Media" -> score += 10.0
                    }
                    when (topic.difficulty) {
                        "Difícil" -> score += 15.0
                        "Media" -> score += 10.0
                    }
                    // Low progress = higher need
                    score += ((100 - topic.progress) * 0.2)
                }

                // E. Disponibilidad vs duración
                val maxMinutes = (dailyAvailableHours * 60).toInt().coerceAtLeast(15)
                if (session.plannedMinutes <= maxMinutes) {
                    score += 10.0
                } else {
                    score -= 20.0
                }

                Triple(session, score, reason)
            }.sortedByDescending { it.second }

            val best = scored.first()
            val bestSession = best.first
            val bestSub = subjectMap[bestSession.subjectId]
            val bestTopic = bestSession.topicId?.let { topicMap[it] }
            val bestExam = exams.filter { it.subjectId == bestSession.subjectId && it.examDate >= now }
                .minByOrNull { it.examDate }

            return Recommendation(
                session = bestSession,
                subject = bestSub,
                topic = bestTopic,
                exam = bestExam,
                title = bestSession.title,
                description = bestSession.description.ifBlank { bestSub?.name ?: "Sesión de estudio" },
                priority = bestSession.priority,
                durationMinutes = bestSession.plannedMinutes,
                reasonLabel = best.third,
                isOverdue = bestSession.scheduledDate < todayStr,
                canStartDirectly = true
            )
        }

        // 2. If NO planned sessions exist: Suggest what to do next based on real academic entities
        val activeSubjects = subjects.filter { it.active }
        if (activeSubjects.isEmpty()) {
            return Recommendation(
                session = null,
                subject = null,
                topic = null,
                exam = null,
                title = "Empieza agregando tu primera materia",
                description = "Configura tus asignaturas universitarias para organizar temas, preparar parciales y estudiar con foco.",
                priority = "Baja",
                durationMinutes = preferredSessionMinutes,
                reasonLabel = "Cuenta nueva",
                isOverdue = false,
                canStartDirectly = false
            )
        }

        // Check if there is an upcoming exam across active subjects
        val nextExam = exams.filter { it.examDate >= now }
            .minByOrNull { it.examDate }

        if (nextExam != null) {
            val examSub = subjectMap[nextExam.subjectId]
            val days = TimeUnit.MILLISECONDS.toDays(nextExam.examDate - now)
            return Recommendation(
                session = null,
                subject = examSub,
                topic = null,
                exam = nextExam,
                title = "Preparar ${nextExam.title}",
                description = "${examSub?.name ?: "Materia"} • Faltan $days días para la evaluación. Te sugerimos generar un plan de preparación.",
                priority = if (days <= 7) "Crítica" else "Alta",
                durationMinutes = preferredSessionMinutes,
                reasonLabel = if (days <= 7) "🔴 Parcial próximo en $days días" else "Evaluación en $days días",
                isOverdue = false,
                canStartDirectly = false
            )
        }

        // If no exams: suggest topic with lowest progress
        val candidateTopic = topics.filter { it.status != "dominado" }
            .minByOrNull { it.progress }

        if (candidateTopic != null) {
            val sub = subjectMap[candidateTopic.subjectId]
            return Recommendation(
                session = null,
                subject = sub,
                topic = candidateTopic,
                exam = null,
                title = "Estudiar ${candidateTopic.name}",
                description = "${sub?.name ?: "Materia"} • Progreso actual: ${candidateTopic.progress}%. Crea una sesión para avanzar.",
                priority = candidateTopic.importance,
                durationMinutes = preferredSessionMinutes,
                reasonLabel = "Tema en curso (${candidateTopic.difficulty})",
                isOverdue = false,
                canStartDirectly = false
            )
        }

        // Fallback: general study recommendation
        val firstSub = activeSubjects.first()
        return Recommendation(
            session = null,
            subject = firstSub,
            topic = null,
            exam = null,
            title = "Planificar sesión de estudio",
            description = "Dedica un bloque de ${preferredSessionMinutes} min a ${firstSub.name} para mantener el ritmo.",
            priority = "Media",
            durationMinutes = preferredSessionMinutes,
            reasonLabel = "Avance de cátedra",
            isOverdue = false,
            canStartDirectly = false
        )
    }
}
