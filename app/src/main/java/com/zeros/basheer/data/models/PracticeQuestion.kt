package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table linking PracticeSession to Questions.
 * Stores the questions in this session and student's responses.
 */
@Entity(
    tableName = "practice_questions",
    primaryKeys = ["sessionId", "questionId"],
    foreignKeys = [
        ForeignKey(
            entity = PracticeSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
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
        Index("sessionId"),
        Index("questionId")
    ]
)
data class PracticeQuestion(
    val sessionId: Long,
    val questionId: String,
    val order: Int,                          // Order in this session
    
    // Response (null if not answered yet)
    val userAnswer: String? = null,
    val isCorrect: Boolean? = null,
    val timeSpentSeconds: Int? = null,
    val answeredAt: Long? = null,
    
    // State
    val skipped: Boolean = false
)
