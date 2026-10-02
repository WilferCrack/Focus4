package com.example.focus4.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val passwordHash: String,
    val displayName: String = "",
    val avatarId: String = "student_01",
    val profileImageUri: String? = null,
    val career: String = "Ingeniería / Ciencias",
    val semester: String = "1° Semestre",
    val dailyAvailableHours: Float = 2.0f,
    val preferredSessionMinutes: Int = 25,
    val mainGoal: String = "Aprobar exámenes y mantener constancia",
    val timezone: String = "America/Argentina/Buenos_Aires",
    val preferredLanguage: String = "es",
    val onboardingCompleted: Boolean = false,
    val themeMode: String = "system", // "light", "dark", "system"
    val lightThemeColor: String = "#2563EB",
    val darkThemeColor: String = "#7C3AED",
    val themePreset: String = "FOCUS4",
    val useDynamicColor: Boolean = false,
    val classReminderDefaultMinutes: Int = 30,
    val classRemindersEnabled: Boolean = true,
    val examRemindersEnabled: Boolean = true,
    val sessionRemindersEnabled: Boolean = true,
    val changeAlertsEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getEffectiveDisplayName(): String {
        return if (displayName.isNotBlank()) {
            displayName.trim()
        } else {
            val combined = "$firstName $lastName".trim()
            if (combined.isNotBlank()) combined else "Estudiante"
        }
    }
}

@Entity(
    tableName = "subjects",
    indices = [Index("userId")]
)
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val description: String = "",
    val colorHex: String = "#3B82F6",
    val iconName: String = "book",
    val teacher: String = "",
    val semester: String = "1° Semestre",
    val priority: String = "Alta", // Crítica, Alta, Media, Baja
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "topics",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val name: String,
    val description: String = "",
    val estimatedMinutes: Int = 60,
    val difficulty: String = "Media", // Fácil, Media, Difícil
    val importance: String = "Alta", // Crítica, Alta, Media, Baja
    val progress: Int = 0, // 0 - 100
    val status: String = "pendiente", // pendiente, en_progreso, dominado
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "exams",
    indices = [Index("userId"), Index("subjectId")]
)
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val subjectId: Long,
    val title: String,
    val examDate: Long, // Epoch timestamp in milliseconds
    val notes: String = "",
    val priority: String = "Alta", // Crítica, Alta, Media, Baja
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "goals",
    indices = [Index("userId")]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val type: String = "sesiones", // tiempo, sesiones, temas, ejercicios, examen
    val targetValue: Int = 10,
    val currentValue: Int = 0,
    val deadline: Long = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000,
    val status: String = "activo", // activo, completado
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "study_sessions",
    indices = [Index("userId"), Index("subjectId"), Index("topicId"), Index("scheduledDate")]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val subjectId: Long,
    val topicId: Long? = null,
    val title: String,
    val description: String = "",
    val plannedMinutes: Int = 25,
    val actualMinutes: Int = 0,
    val scheduledDate: String, // YYYY-MM-DD
    val startTime: Long? = null,
    val endTime: Long? = null,
    val status: String = "planned", // planned, active, completed, partially_completed, skipped, cancelled
    val priority: String = "Media", // Crítica, Alta, Media, Baja
    val completionPercentage: Int = 0, // 0, 50, 100
    val distractionCount: Int = 0,
    val notes: String = "",
    val uncompletedReason: String? = null,
    val pausesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "flashcards",
    indices = [Index("userId"), Index("subjectId"), Index("topicId")]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val subjectId: Long,
    val topicId: Long? = null,
    val question: String,
    val answer: String,
    val difficulty: String = "Media", // Fácil, Media, Difícil
    val reviewCount: Int = 0,
    val mastered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "exam_questions",
    indices = [Index("userId"), Index("subjectId"), Index("topicId")]
)
data class ExamQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val subjectId: Long,
    val topicId: Long? = null,
    val questionText: String,
    val type: String = "multiple_choice", // multiple_choice, true_false, open
    val optionsJson: String = "[]", // Serialized JSON array of strings
    val correctAnswer: String = "",
    val explanation: String = "",
    val userSelectedAnswer: String? = null,
    val isCorrect: Boolean? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ai_messages",
    indices = [Index("userId"), Index("subjectId")]
)
data class AIMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val subjectId: Long? = null,
    val topicId: Long? = null,
    val role: String, // "user", "model", "system"
    val content: String,
    val isTutorMode: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "class_schedules",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("subjectId"), Index("dayOfWeek")]
)
data class ClassScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val subjectId: Long,
    val dayOfWeek: Int, // 1 = Lunes, 2 = Martes, 3 = Miércoles, 4 = Jueves, 5 = Viernes, 6 = Sábado, 7 = Domingo
    val startTime: String, // HH:mm, e.g. "15:00"
    val endTime: String,   // HH:mm, e.g. "17:00"
    val classroom: String = "",
    val location: String = "",
    val professor: String = "",
    val modality: String = "Presencial", // Presencial, Virtual, Híbrida
    val virtualUrl: String = "",
    val notes: String = "",
    val colorHex: String = "#2563EB",
    val reminderMinutesBefore: Int = 30, // 0 = desactivado, 5, 15, 30, 60
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "class_attendance",
    foreignKeys = [
        ForeignKey(
            entity = ClassScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("scheduleId"), Index("subjectId"), Index("date")]
)
data class ClassAttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val scheduleId: Long,
    val subjectId: Long,
    val date: String, // yyyy-MM-dd
    val startTime: String,
    val endTime: String,
    val status: String = "PENDING", // PENDING, ATTENDED, ABSENT, LATE, CANCELLED, RESCHEDULED, EXCUSED
    val lateMinutes: Int = 0,
    val notes: String = "",
    val rescheduledDate: String? = null,
    val rescheduledStartTime: String? = null,
    val rescheduledEndTime: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

