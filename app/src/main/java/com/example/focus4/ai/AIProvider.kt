package com.example.focus4.ai

data class GeneratedFlashcard(
    val question: String,
    val answer: String,
    val difficulty: String = "Media"
)

data class GeneratedQuestion(
    val questionText: String,
    val type: String, // "multiple_choice", "true_false", "open"
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

interface AIProvider {
    suspend fun askTutor(
        userMessage: String,
        subjectName: String?,
        topicName: String?,
        contextMaterial: String?,
        isTutorMode: Boolean
    ): Result<String>

    suspend fun generateFlashcards(
        subjectName: String,
        topicName: String,
        count: Int = 3
    ): Result<List<GeneratedFlashcard>>

    suspend fun generateExamQuestions(
        subjectName: String,
        topicName: String,
        count: Int = 3
    ): Result<List<GeneratedQuestion>>

    suspend fun explainConcept(
        concept: String,
        subjectName: String?
    ): Result<String>

    suspend fun summarizeMaterial(
        text: String,
        subjectName: String?
    ): Result<String>
}
