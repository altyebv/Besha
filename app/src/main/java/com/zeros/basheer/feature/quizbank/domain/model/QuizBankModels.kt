package com.zeros.basheer.feature.quizbank.domain.model


/**
 * Domain model for Exam.
 */
data class Exam(
    val id: String,
    val subjectId: String,
    val titleAr: String,
    val titleEn: String?,
    val source: ExamSource,
    val year: Int?,
    val schoolName: String?,
    val duration: Int?,
    val totalPoints: Int?,
    val description: String?
)

enum class ExamSource {
    MINISTRY,
    SCHOOL,
    PRACTICE,
    CUSTOM
}

/**
 * Junction: Exam ↔ Question.
 */


/**
 * Junction: Question ↔ Concept.
 */


/**
 * User's exam attempt.
 */
data class QuizAttempt(
    val id: Long,
    val examId: String,
    val startedAt: Long,
    val completedAt: Long?,
    val score: Int?,
    val totalPoints: Int?,
    val percentage: Float?,
    val timeSpentSeconds: Int?
)

/**
 * Individual question response in a quiz attempt.
 */
data class QuestionResponse(
    val id: Long,
    val attemptId: Long,
    val questionId: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val pointsEarned: Int,
    val timeSpentSeconds: Int?,
    val answeredAt: Long
)

/**
 * Aggregated question statistics.
 */
data class QuestionStats(
    val questionId: String,
    val timesAsked: Int,
    val timesCorrect: Int,
    val avgTimeSeconds: Float,
    val successRate: Float,
    val lastShownInFeed: Long?,
    val feedShowCount: Int,
    val lastAskedAt: Long?,
    val updatedAt: Long
) {
    companion object {
        fun forNewQuestion(questionId: String) = QuestionStats(
            questionId = questionId,
            timesAsked = 0,
            timesCorrect = 0,
            avgTimeSeconds = 0f,
            successRate = 0f,
            lastShownInFeed = null,
            feedShowCount = 0,
            lastAskedAt = null,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun withNewResponse(isCorrect: Boolean, timeSeconds: Int): QuestionStats {
        val newTimesAsked = timesAsked + 1
        val newTimesCorrect = if (isCorrect) timesCorrect + 1 else timesCorrect
        val newSuccessRate = newTimesCorrect.toFloat() / newTimesAsked

        val newAvgTime = if (timesAsked == 0) {
            timeSeconds.toFloat()
        } else {
            (avgTimeSeconds * timesAsked + timeSeconds) / newTimesAsked
        }

        return copy(
            timesAsked = newTimesAsked,
            timesCorrect = newTimesCorrect,
            successRate = newSuccessRate,
            avgTimeSeconds = newAvgTime,
            lastAskedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun withFeedShow(): QuestionStats {
        return copy(
            lastShownInFeed = System.currentTimeMillis(),
            feedShowCount = feedShowCount + 1,
            updatedAt = System.currentTimeMillis()
        )
    }
}