package com.zeros.basheer.feature.quizbank.domain.repository

import com.zeros.basheer.feature.quizbank.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for QuizBank feature.
 *
 * Handles questions, exams, attempts, and stats.
 */
interface QuizBankRepository {

    // ==================== Questions ====================
    suspend fun getQuestionById(questionId: String): Question?
    fun getQuestionsBySubject(subjectId: String): Flow<List<Question>>
    fun getQuestionsByUnit(unitId: String): Flow<List<Question>>
    fun getQuestionsByLesson(lessonId: String): Flow<List<Question>>
    fun getQuestionsByType(subjectId: String, type: QuestionType): Flow<List<Question>>
    fun getFeedEligibleQuestions(subjectId: String): Flow<List<Question>>
    fun getQuestionsByConcept(conceptId: String): Flow<List<Question>>
    suspend fun getQuestionsForConcepts(conceptIds: List<String>, limit: Int): List<Question>
    suspend fun getFilteredQuestions(
        subjectId: String,
        unitId: String? = null,
        type: QuestionType? = null,
        conceptId: String? = null,
        minDifficulty: Int? = null,
        maxDifficulty: Int? = null,
        limit: Int = 20
    ): List<Question>
    suspend fun insertQuestion(question: Question)
    suspend fun insertQuestions(questions: List<Question>)
    suspend fun deleteQuestionById(questionId: String)
    suspend fun getQuestionCounts(subjectId: String): QuestionCounts

    // ==================== Exams ====================
    fun getAllExams(): Flow<List<Exam>>
    fun getExamsBySubject(subjectId: String): Flow<List<Exam>>
    fun getExamsBySource(subjectId: String, source: ExamSource): Flow<List<Exam>>
    suspend fun getExamById(examId: String): Exam?
    suspend fun getQuestionsForExam(examId: String): List<Question>
    fun getQuestionsForExamFlow(examId: String): Flow<List<Question>>
    suspend fun insertExam(exam: Exam)
    suspend fun insertExams(exams: List<Exam>)
    suspend fun deleteExamsBySubject(subjectId: String)

    // ==================== ExamQuestions Junction ====================
    suspend fun insertExamQuestion(examQuestion: ExamQuestion)
    suspend fun insertExamQuestions(examQuestions: List<ExamQuestion>)
    suspend fun deleteExamQuestionsByExam(examId: String)

    // ==================== QuestionConcepts Junction ====================
    suspend fun insertQuestionConcept(questionConcept: QuestionConcept)
    suspend fun insertQuestionConcepts(questionConcepts: List<QuestionConcept>)
    suspend fun deleteQuestionConceptsByQuestion(questionId: String)

    // ==================== Quiz Attempts ====================
    fun getAttemptsByExam(examId: String): Flow<List<QuizAttempt>>
    suspend fun getAttemptById(attemptId: Long): QuizAttempt?
    fun getRecentAttempts(limit: Int = 10): Flow<List<QuizAttempt>>
    suspend fun getLastAttemptForExam(examId: String): QuizAttempt?
    suspend fun insertAttempt(attempt: QuizAttempt): Long
    suspend fun updateAttempt(attempt: QuizAttempt)
    suspend fun completeAttempt(attemptId: Long, score: Int, totalPoints: Int, timeSpentSeconds: Int)
    suspend fun deleteAttemptsByExam(examId: String)

    // ==================== Question Responses ====================
    fun getResponsesByAttempt(attemptId: Long): Flow<List<QuestionResponse>>
    suspend fun getResponse(attemptId: Long, questionId: String): QuestionResponse?
    suspend fun insertResponse(response: QuestionResponse): Long
    suspend fun insertResponses(responses: List<QuestionResponse>)


    // ==================== Question Stats ====================
    suspend fun getStatsForQuestion(questionId: String): QuestionStats?
    fun getStatsForQuestionFlow(questionId: String): Flow<QuestionStats?>
    suspend fun getStatsForQuestions(questionIds: List<String>): List<QuestionStats>
    fun getHardestQuestions(limit: Int = 10): Flow<List<QuestionStats>>
    fun getEasiestQuestions(limit: Int = 10): Flow<List<QuestionStats>>
    suspend fun insertStats(stats: QuestionStats)
    suspend fun updateStats(stats: QuestionStats)

    // ==================== Convenience Methods ====================

    suspend fun startQuizAttempt(examId: String): Long

    suspend fun completeQuizAttempt(
        attemptId: Long,
        score: Int,
        totalPoints: Int,
        timeSpentSeconds: Int
    )

    suspend fun recordQuestionResponse(
        attemptId: Long,
        questionId: String,
        userAnswer: String,
        isCorrect: Boolean,
        pointsEarned: Int,
        timeSpentSeconds: Int
    )


}