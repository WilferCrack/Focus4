package com.example.focus4.domain

import com.example.focus4.data.model.StudySessionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object HabitAnalysisService {

    data class HabitReport(
        val totalSessionsCount: Int,
        val completedCount: Int,
        val partialCount: Int,
        val skippedCount: Int,
        val totalMinutesStudied: Int,
        val currentStreakDays: Int,
        val longestStreakDays: Int,
        val activeDaysCount: Int,
        val mostFrequentUncompletedReason: String?,
        val peakProductiveHourRange: String?,
        val insights: List<String>,
        val hasEnoughData: Boolean
    )

    fun analyzeHabits(sessions: List<StudySessionEntity>): HabitReport {
        if (sessions.isEmpty()) {
            return HabitReport(
                totalSessionsCount = 0,
                completedCount = 0,
                partialCount = 0,
                skippedCount = 0,
                totalMinutesStudied = 0,
                currentStreakDays = 0,
                longestStreakDays = 0,
                activeDaysCount = 0,
                mostFrequentUncompletedReason = null,
                peakProductiveHourRange = null,
                insights = listOf("No tenemos suficientes datos todavía. Realiza tus primeras sesiones para descubrir tus patrones."),
                hasEnoughData = false
            )
        }

        val completed = sessions.filter { it.status == "completed" }
        val partial = sessions.filter { it.status == "partially_completed" }
        val skipped = sessions.filter { it.status == "skipped" }
        val totalMinutes = completed.sumOf { it.actualMinutes } + partial.sumOf { it.actualMinutes }

        // Active days
        val activeDates = (completed + partial).map { it.scheduledDate }.distinct().sorted()
        val activeDaysCount = activeDates.size

        // Streak calculation
        val (currentStreak, longestStreak) = calculateStreaks(activeDates)

        // Uncompleted reason analysis
        val uncompletedReasons = sessions
            .mapNotNull { it.uncompletedReason }
            .filter { it.isNotBlank() }

        val reasonCounts = uncompletedReasons.groupingBy { it }.eachCount()
        val mostFrequentReason = reasonCounts.maxByOrNull { it.value }?.key

        // Productive hour range analysis
        val sessionHours = (completed + partial).mapNotNull { session ->
            session.startTime?.let { millis ->
                val cal = Calendar.getInstance().apply { timeInMillis = millis }
                cal.get(Calendar.HOUR_OF_DAY)
            }
        }

        val peakHourRange = if (sessionHours.size >= 3) {
            val hourGroups = sessionHours.groupingBy { it }.eachCount()
            val peakHour = hourGroups.maxByOrNull { it.value }?.key ?: 18
            "${peakHour}:00 - ${peakHour + 2}:00"
        } else null

        // Insights generation
        val insights = mutableListOf<String>()
        val hasEnoughData = (completed.size + partial.size + skipped.size) >= 3

        if (!hasEnoughData) {
            insights.add("No tenemos suficientes datos todavía. Con al menos 3 sesiones registradas podremos mostrarte tendencias de productividad.")
        } else {
            if (mostFrequentReason != null) {
                insights.add("El motivo más frecuente de sesiones no completadas fue: \"$mostFrequentReason\". Considera ajustar la duración o tu entorno de estudio.")
            }
            if (peakHourRange != null) {
                insights.add("Tu horario con mayor cantidad de sesiones completadas es entre las $peakHourRange.")
            }
            if (skipped.size > completed.size) {
                insights.add("Has tenido varias sesiones pendientes. Usa la opción 'Reorganizar plan' para distribuir tu carga académica sin sobrecargarte.")
            } else if (completed.size >= 5) {
                insights.add("¡Excelente ritmo de ejecución! Mantener sesiones cortas de 25 minutos está consolidando tu constancia.")
            }
            if (currentStreak >= 3) {
                insights.add("Llevas una racha activa de $currentStreak días continuos estudiando.")
            }
        }

        return HabitReport(
            totalSessionsCount = sessions.size,
            completedCount = completed.size,
            partialCount = partial.size,
            skippedCount = skipped.size,
            totalMinutesStudied = totalMinutes,
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            activeDaysCount = activeDaysCount,
            mostFrequentUncompletedReason = mostFrequentReason,
            peakProductiveHourRange = peakHourRange,
            insights = insights,
            hasEnoughData = hasEnoughData
        )
    }

    private fun calculateStreaks(sortedDateStrings: List<String>): Pair<Int, Int> {
        if (sortedDateStrings.isEmpty()) return Pair(0, 0)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateSet = sortedDateStrings.mapNotNull {
            try { sdf.parse(it) } catch (e: Exception) { null }
        }.map { truncateTime(it) }.toSet()

        if (dateSet.isEmpty()) return Pair(0, 0)

        val today = truncateTime(Date())
        val cal = Calendar.getInstance().apply { time = today }

        var currentStreak = 0
        // Check if today was active, if not check yesterday
        if (dateSet.contains(cal.time)) {
            currentStreak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
            while (dateSet.contains(cal.time)) {
                currentStreak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            if (dateSet.contains(cal.time)) {
                currentStreak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
                while (dateSet.contains(cal.time)) {
                    currentStreak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }
            }
        }

        // Longest streak
        var longestStreak = currentStreak
        var tempStreak = 0
        var prevDate: Date? = null

        val sortedDates = dateSet.sorted()
        for (d in sortedDates) {
            if (prevDate == null) {
                tempStreak = 1
            } else {
                val diffDays = ((d.time - prevDate.time) / (1000 * 60 * 60 * 24)).toInt()
                if (diffDays == 1) {
                    tempStreak++
                } else if (diffDays > 1) {
                    tempStreak = 1
                }
            }
            if (tempStreak > longestStreak) {
                longestStreak = tempStreak
            }
            prevDate = d
        }

        return Pair(currentStreak, longestStreak)
    }

    private fun truncateTime(date: Date): Date {
        val cal = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.time
    }
}
