package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks aggregated statistics for each question.
 *
 * This is computed over time as students answer questions.
 * Used for:
 * - Identifying hard/easy questions
 * - Avoiding showing same question too often in feeds
 * - Analytics and content improvement
 */
@Entity(
    tableName = "question_stats",
    foreignKeys = [
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("questionId")]
)
data class QuestionStats(
    @PrimaryKey
    val questionId: String,

    // Aggregate performance
    val timesAsked: Int = 0,
    val timesCorrect: Int = 0,
    val avgTimeSeconds: Float = 0f,

    // Computed success rate (for quick queries)
    val successRate: Float = 0f,  // timesCorrect / timesAsked

    // Feed tracking - avoid repetition
    val lastShownInFeed: Long? = null,
    val feedShowCount: Int = 0,

    // Last usage
    val lastAskedAt: Long? = null,

    // Updated timestamp
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Create initial stats for a new question.
         */
        fun forNewQuestion(questionId: String) = QuestionStats(questionId = questionId)
    }

    /**
     * Calculate updated stats after a new response.
     */
    fun withNewResponse(isCorrect: Boolean, timeSeconds: Int): QuestionStats {
        val newTimesAsked = timesAsked + 1
        val newTimesCorrect = if (isCorrect) timesCorrect + 1 else timesCorrect
        val newSuccessRate = newTimesCorrect.toFloat() / newTimesAsked

        // Running average for time
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

    /**
     * Mark as shown in feed.
     */
    fun withFeedShow(): QuestionStats {
        return copy(
            lastShownInFeed = System.currentTimeMillis(),
            feedShowCount = feedShowCount + 1,
            updatedAt = System.currentTimeMillis()
        )
    }
}