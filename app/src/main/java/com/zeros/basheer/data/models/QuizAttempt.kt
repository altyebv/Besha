package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks a user's attempt at an exam.
 * 
 * One exam can have multiple attempts over time.
 * Individual question responses are in QuestionResponse.
 */
@Entity(
    tableName = "quiz_attempts",
    foreignKeys = [
        ForeignKey(
            entity = Exam::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("examId"),
        Index("startedAt")
    ]
)
data class QuizAttempt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examId: String,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val score: Int? = null,                 // Points earned
    val totalPoints: Int? = null,           // Total possible points
    val percentage: Float? = null,          // Score percentage
    val timeSpentSeconds: Int? = null       // Total time spent
)
