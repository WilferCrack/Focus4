package com.example.focus4.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getFirstUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSubjectsForUser(userId: Long): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: Long): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    fun getSubjectByIdFlow(id: Long): Flow<SubjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: Long)

    @Query("DELETE FROM subjects WHERE userId = :userId")
    suspend fun deleteSubjectsByUserId(userId: Long)

    @Query("SELECT * FROM subjects WHERE userId = :userId")
    suspend fun getSubjectsListForUser(userId: Long): List<SubjectEntity>
}

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY importance DESC, createdAt ASC")
    fun getTopicsForSubject(subjectId: Long): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId")
    suspend fun getTopicsListForSubject(subjectId: Long): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: Long): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity): Long

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Delete
    suspend fun deleteTopic(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteTopicById(id: Long)

    @Query("DELETE FROM topics WHERE subjectId = :subjectId")
    suspend fun deleteTopicsBySubjectId(subjectId: Long)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams WHERE userId = :userId ORDER BY examDate ASC")
    fun getExamsForUser(userId: Long): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE userId = :userId AND examDate >= :nowMillis ORDER BY examDate ASC")
    fun getUpcomingExams(userId: Long, nowMillis: Long): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE userId = :userId AND examDate >= :nowMillis ORDER BY examDate ASC LIMIT 1")
    fun getNextExam(userId: Long, nowMillis: Long): Flow<ExamEntity?>

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    suspend fun getExamById(id: Long): ExamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: Long)

    @Query("DELETE FROM exams WHERE subjectId = :subjectId")
    suspend fun deleteExamsBySubjectId(subjectId: Long)

    @Query("DELETE FROM exams WHERE userId = :userId")
    suspend fun deleteExamsByUserId(userId: Long)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY deadline ASC")
    fun getGoalsForUser(userId: Long): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteGoalsByUserId(userId: Long)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions WHERE userId = :userId ORDER BY scheduledDate DESC, id DESC")
    fun getSessionsForUser(userId: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND scheduledDate = :date ORDER BY status ASC, id ASC")
    fun getSessionsByDate(userId: Long, date: String): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND (status = 'planned' OR status = 'skipped') AND scheduledDate < :today ORDER BY scheduledDate ASC")
    fun getPendingSessions(userId: Long, today: String): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND (status = 'planned' OR status = 'skipped') AND scheduledDate < :today")
    suspend fun getPendingSessionsList(userId: Long, today: String): List<StudySessionEntity>

    @Query("SELECT * FROM study_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): StudySessionEntity?

    @Query("SELECT * FROM study_sessions WHERE id = :id LIMIT 1")
    fun getSessionByIdFlow(id: Long): Flow<StudySessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<StudySessionEntity>)

    @Update
    suspend fun updateSession(session: StudySessionEntity)

    @Delete
    suspend fun deleteSession(session: StudySessionEntity)

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM study_sessions WHERE subjectId = :subjectId")
    suspend fun deleteSessionsBySubjectId(subjectId: Long)

    @Query("DELETE FROM study_sessions WHERE topicId = :topicId")
    suspend fun deleteSessionsByTopicId(topicId: Long)

    @Query("DELETE FROM study_sessions WHERE userId = :userId")
    suspend fun deleteSessionsByUserId(userId: Long)

    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND status IN ('completed', 'partially_completed')")
    fun getCompletedSessionsFlow(userId: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE userId = :userId")
    suspend fun getAllSessionsForUser(userId: Long): List<StudySessionEntity>
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE userId = :userId ORDER BY id DESC")
    fun getAllFlashcards(userId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE userId = :userId AND subjectId = :subjectId ORDER BY id DESC")
    fun getFlashcardsForSubject(userId: Long, subjectId: Long): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)

    @Delete
    suspend fun deleteFlashcard(flashcard: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteFlashcardById(id: Long)

    @Query("DELETE FROM flashcards WHERE subjectId = :subjectId")
    suspend fun deleteFlashcardsBySubjectId(subjectId: Long)

    @Query("DELETE FROM flashcards WHERE topicId = :topicId")
    suspend fun deleteFlashcardsByTopicId(topicId: Long)

    @Query("DELETE FROM flashcards WHERE userId = :userId")
    suspend fun deleteFlashcardsByUserId(userId: Long)
}

@Dao
interface ExamQuestionDao {
    @Query("SELECT * FROM exam_questions WHERE userId = :userId ORDER BY id ASC")
    fun getExamQuestions(userId: Long): Flow<List<ExamQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<ExamQuestionEntity>)

    @Update
    suspend fun updateQuestion(question: ExamQuestionEntity)

    @Query("DELETE FROM exam_questions WHERE userId = :userId")
    suspend fun clearQuestionsForUser(userId: Long)

    @Query("DELETE FROM exam_questions WHERE subjectId = :subjectId")
    suspend fun deleteQuestionsBySubjectId(subjectId: Long)

    @Query("DELETE FROM exam_questions WHERE topicId = :topicId")
    suspend fun deleteQuestionsByTopicId(topicId: Long)
}

@Dao
interface AIMessageDao {
    @Query("SELECT * FROM ai_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getAIMessages(userId: Long): Flow<List<AIMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAIMessage(message: AIMessageEntity): Long

    @Query("DELETE FROM ai_messages WHERE userId = :userId")
    suspend fun clearHistory(userId: Long)

    @Query("DELETE FROM ai_messages WHERE subjectId = :subjectId")
    suspend fun deleteMessagesBySubjectId(subjectId: Long)

    @Query("DELETE FROM ai_messages WHERE topicId = :topicId")
    suspend fun deleteMessagesByTopicId(topicId: Long)
}

@Dao
interface ClassScheduleDao {
    @Query("SELECT * FROM class_schedules WHERE userId = :userId ORDER BY dayOfWeek ASC, startTime ASC")
    fun getSchedulesForUser(userId: Long): Flow<List<ClassScheduleEntity>>

    @Query("SELECT * FROM class_schedules WHERE userId = :userId ORDER BY dayOfWeek ASC, startTime ASC")
    suspend fun getSchedulesListForUser(userId: Long): List<ClassScheduleEntity>

    @Query("SELECT * FROM class_schedules WHERE userId = :userId AND dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getSchedulesForDay(userId: Long, dayOfWeek: Int): Flow<List<ClassScheduleEntity>>

    @Query("SELECT * FROM class_schedules WHERE userId = :userId AND dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    suspend fun getSchedulesListForDay(userId: Long, dayOfWeek: Int): List<ClassScheduleEntity>

    @Query("SELECT * FROM class_schedules WHERE subjectId = :subjectId ORDER BY dayOfWeek ASC, startTime ASC")
    fun getSchedulesForSubject(subjectId: Long): Flow<List<ClassScheduleEntity>>

    @Query("SELECT * FROM class_schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): ClassScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ClassScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ClassScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ClassScheduleEntity)

    @Query("DELETE FROM class_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)

    @Query("DELETE FROM class_schedules WHERE subjectId = :subjectId")
    suspend fun deleteSchedulesBySubjectId(subjectId: Long)

    @Query("DELETE FROM class_schedules WHERE userId = :userId")
    suspend fun deleteSchedulesByUserId(userId: Long)
}

@Dao
interface ClassAttendanceDao {
    @Query("SELECT * FROM class_attendance WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    fun getAttendanceForUser(userId: Long): Flow<List<ClassAttendanceEntity>>

    @Query("SELECT * FROM class_attendance WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    suspend fun getAttendanceListForUser(userId: Long): List<ClassAttendanceEntity>

    @Query("SELECT * FROM class_attendance WHERE userId = :userId AND date = :date ORDER BY startTime ASC")
    fun getAttendanceForDate(userId: Long, date: String): Flow<List<ClassAttendanceEntity>>

    @Query("SELECT * FROM class_attendance WHERE userId = :userId AND date = :date ORDER BY startTime ASC")
    suspend fun getAttendanceListForDate(userId: Long, date: String): List<ClassAttendanceEntity>

    @Query("SELECT * FROM class_attendance WHERE userId = :userId AND subjectId = :subjectId ORDER BY date DESC")
    fun getAttendanceForSubject(userId: Long, subjectId: Long): Flow<List<ClassAttendanceEntity>>

    @Query("SELECT * FROM class_attendance WHERE userId = :userId AND subjectId = :subjectId ORDER BY date DESC")
    suspend fun getAttendanceListForSubject(userId: Long, subjectId: Long): List<ClassAttendanceEntity>

    @Query("SELECT * FROM class_attendance WHERE scheduleId = :scheduleId AND date = :date LIMIT 1")
    suspend fun getAttendanceForScheduleAndDate(scheduleId: Long, date: String): ClassAttendanceEntity?

    @Query("SELECT * FROM class_attendance WHERE id = :id LIMIT 1")
    suspend fun getAttendanceById(id: Long): ClassAttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: ClassAttendanceEntity): Long

    @Update
    suspend fun updateAttendance(attendance: ClassAttendanceEntity)

    @Query("DELETE FROM class_attendance WHERE id = :id")
    suspend fun deleteAttendanceById(id: Long)

    @Query("DELETE FROM class_attendance WHERE scheduleId = :scheduleId")
    suspend fun deleteAttendanceByScheduleId(scheduleId: Long)

    @Query("DELETE FROM class_attendance WHERE subjectId = :subjectId")
    suspend fun deleteAttendanceBySubjectId(subjectId: Long)

    @Query("DELETE FROM class_attendance WHERE userId = :userId")
    suspend fun deleteAttendanceByUserId(userId: Long)
}

