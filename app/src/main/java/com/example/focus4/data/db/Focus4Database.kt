package com.example.focus4.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.focus4.data.dao.AIMessageDao
import com.example.focus4.data.dao.ClassAttendanceDao
import com.example.focus4.data.dao.ClassScheduleDao
import com.example.focus4.data.dao.ExamDao
import com.example.focus4.data.dao.ExamQuestionDao
import com.example.focus4.data.dao.FlashcardDao
import com.example.focus4.data.dao.GoalDao
import com.example.focus4.data.dao.StudySessionDao
import com.example.focus4.data.dao.SubjectDao
import com.example.focus4.data.dao.TopicDao
import com.example.focus4.data.dao.UserDao
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

@Database(
    entities = [
        UserEntity::class,
        SubjectEntity::class,
        TopicEntity::class,
        ExamEntity::class,
        GoalEntity::class,
        StudySessionEntity::class,
        FlashcardEntity::class,
        ExamQuestionEntity::class,
        AIMessageEntity::class,
        ClassScheduleEntity::class,
        ClassAttendanceEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class Focus4Database : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun examDao(): ExamDao
    abstract fun goalDao(): GoalDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun examQuestionDao(): ExamQuestionDao
    abstract fun aiMessageDao(): AIMessageDao
    abstract fun classScheduleDao(): ClassScheduleDao
    abstract fun classAttendanceDao(): ClassAttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: Focus4Database? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN displayName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE users ADD COLUMN avatarId TEXT NOT NULL DEFAULT 'student_01'")
                db.execSQL("ALTER TABLE users ADD COLUMN profileImageUri TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE users ADD COLUMN semester TEXT NOT NULL DEFAULT '1° Semestre'")
                db.execSQL("UPDATE users SET displayName = TRIM(firstName || ' ' || lastName) WHERE displayName = ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add appearance and reminder preferences to users
                db.execSQL("ALTER TABLE users ADD COLUMN themeMode TEXT NOT NULL DEFAULT 'system'")
                db.execSQL("ALTER TABLE users ADD COLUMN lightThemeColor TEXT NOT NULL DEFAULT '#2563EB'")
                db.execSQL("ALTER TABLE users ADD COLUMN darkThemeColor TEXT NOT NULL DEFAULT '#7C3AED'")
                db.execSQL("ALTER TABLE users ADD COLUMN themePreset TEXT NOT NULL DEFAULT 'FOCUS4'")
                db.execSQL("ALTER TABLE users ADD COLUMN useDynamicColor INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE users ADD COLUMN classReminderDefaultMinutes INTEGER NOT NULL DEFAULT 30")
                db.execSQL("ALTER TABLE users ADD COLUMN classRemindersEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN examRemindersEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN sessionRemindersEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE users ADD COLUMN changeAlertsEnabled INTEGER NOT NULL DEFAULT 1")

                // 2. Create class_schedules table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS class_schedules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        subjectId INTEGER NOT NULL,
                        dayOfWeek INTEGER NOT NULL,
                        startTime TEXT NOT NULL,
                        endTime TEXT NOT NULL,
                        classroom TEXT NOT NULL,
                        location TEXT NOT NULL,
                        professor TEXT NOT NULL,
                        modality TEXT NOT NULL,
                        virtualUrl TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        colorHex TEXT NOT NULL,
                        reminderMinutesBefore INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(subjectId) REFERENCES subjects(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_class_schedules_userId ON class_schedules(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_class_schedules_subjectId ON class_schedules(subjectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_class_schedules_dayOfWeek ON class_schedules(dayOfWeek)")

                // 3. Create class_attendance table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS class_attendance (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        scheduleId INTEGER NOT NULL,
                        subjectId INTEGER NOT NULL,
                        date TEXT NOT NULL,
                        startTime TEXT NOT NULL,
                        endTime TEXT NOT NULL,
                        status TEXT NOT NULL,
                        lateMinutes INTEGER NOT NULL,
                        notes TEXT NOT NULL,
                        rescheduledDate TEXT,
                        rescheduledStartTime TEXT,
                        rescheduledEndTime TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(scheduleId) REFERENCES class_schedules(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_class_attendance_userId ON class_attendance(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_class_attendance_scheduleId ON class_attendance(scheduleId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_class_attendance_subjectId ON class_attendance(subjectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_class_attendance_date ON class_attendance(date)")
            }
        }

        fun getDatabase(context: Context): Focus4Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Focus4Database::class.java,
                    "focus4_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
