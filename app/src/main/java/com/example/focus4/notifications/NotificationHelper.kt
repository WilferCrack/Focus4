package com.example.focus4.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_STUDY_SESSIONS = "study_sessions_channel"
    const val CHANNEL_EXAM_ALERTS = "exam_alerts_channel"
    const val CHANNEL_CLASS_REMINDERS = "class_reminders_channel"
    const val CHANNEL_GENERAL = "general_academic_channel"

    private const val NOTIFICATION_ID_SESSION = 1001
    private const val NOTIFICATION_ID_EXAM = 1002
    private const val NOTIFICATION_ID_CLASS = 1004
    private const val NOTIFICATION_ID_GENERAL = 1003

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val sessionChannel = NotificationChannel(
                CHANNEL_STUDY_SESSIONS,
                "Sesiones de Estudio y Pomodoro",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones al iniciar, pausar o completar sesiones de foco"
                enableVibration(true)
            }

            val examChannel = NotificationChannel(
                CHANNEL_EXAM_ALERTS,
                "Recordatorios de Exámenes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de proximidad de exámenes parciales y finales"
                enableVibration(true)
            }

            val classChannel = NotificationChannel(
                CHANNEL_CLASS_REMINDERS,
                "Recordatorios de Clases Universitarias",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas previas a clases presenciales, virtuales y cambios de aula"
                enableVibration(true)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "Avisos y Planificación",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de estudio diario y organización de materias"
            }

            notificationManager.createNotificationChannels(
                listOf(sessionChannel, examChannel, classChannel, generalChannel)
            )
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendSessionCompletedNotification(
        context: Context,
        sessionTitle: String,
        durationMinutes: Int
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_SESSION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_STUDY_SESSIONS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("¡Sesión de Estudio Completada! 🎯")
            .setContentText("Has completado $durationMinutes minutos de $sessionTitle.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("¡Excelente trabajo! Has completado $durationMinutes minutos de estudio en '$sessionTitle'. Revisa tu progreso y tómate un descanso consciente.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SESSION, notification)
    }

    fun sendExamAlertNotification(
        context: Context,
        examTitle: String,
        daysRemaining: Long,
        subjectName: String
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_EXAM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (daysRemaining <= 0L) {
            "¡Hoy es tu examen! 📝 $examTitle"
        } else if (daysRemaining == 1L) {
            "¡Mañana es tu examen! ⚠️ $examTitle"
        } else {
            "Faltan $daysRemaining días para $examTitle ⏳"
        }

        val text = "Materia: $subjectName. Recuerda repasar los temas de alta prioridad y descansar bien."

        val notification = NotificationCompat.Builder(context, CHANNEL_EXAM_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$text\nIngresa a FOCUS4 para repasar con flashcards y simulacro.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_EXAM + examTitle.hashCode() % 1000,
            notification
        )
    }

    fun sendClassReminderNotification(
        context: Context,
        subjectName: String,
        startTime: String,
        classroom: String,
        modality: String,
        professor: String = ""
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_CLASS + subjectName.hashCode() % 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val locDetail = when {
            classroom.isNotBlank() -> "en $classroom"
            modality.equals("Virtual", ignoreCase = true) -> "(Virtual)"
            else -> ""
        }
        val profDetail = if (professor.isNotBlank()) " con $professor" else ""
        val title = "🎓 Clase próxima: $subjectName"
        val text = "Comienza a las $startTime $locDetail$profDetail. ¡Prepárate para ingresar a tiempo!"

        val notification = NotificationCompat.Builder(context, CHANNEL_CLASS_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$text\nModalidad: $modality. Abre FOCUS4 para registrar tu asistencia o ver notas de la materia.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_CLASS + subjectName.hashCode() % 1000,
            notification
        )
    }

    fun sendClassChangeNotification(
        context: Context,
        subjectName: String,
        changeType: String, // "CANCELLED" or "RESCHEDULED"
        details: String
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_CLASS + 500 + subjectName.hashCode() % 500,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (changeType == "CANCELLED") {
            "⚠️ Clase Cancelada: $subjectName"
        } else {
            "🔄 Clase Reprogramada: $subjectName"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_CLASS_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(details)
            .setStyle(NotificationCompat.BigTextStyle().bigText(details))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_CLASS + 500 + subjectName.hashCode() % 500,
            notification
        )
    }

    fun sendTestNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_GENERAL
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_GENERAL,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_GENERAL, notification)
    }
}
