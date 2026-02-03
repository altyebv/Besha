package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Individual question response within a quiz attempt.
 * 
 * Tracks what the user answered for each question,
 * whether it was correct, and how long they spent.
 */
@Entity(
    tableName = "question_responses",
    foreignKeys = [
        ForeignKey(
            entity = QuizAttempt::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("attemptId"),
        Index("questionId")
    ]
)
data class QuestionResponse(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val attemptId: Long,
    val questionId: String,
    val userAnswer: String,                 // What the user answered
    val isCorrect: Boolean,
    val pointsEarned: Int = 0,
    val timeSpentSeconds: Int? = null,
    val answeredAt: Long = System.currentTimeMillis()
)
