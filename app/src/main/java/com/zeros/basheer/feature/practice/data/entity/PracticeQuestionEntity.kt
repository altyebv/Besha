package com.zeros.basheer.feature.practice.data.entity


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.zeros.basheer.feature.quizbank.data.entity.QuestionEntity

@Entity(
    tableName = "practice_questions",
    primaryKeys = ["sessionId", "questionId"],
    foreignKeys = [
        ForeignKey(
            entity = PracticeSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
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
data class PracticeQuestionEntity(
    val sessionId: Long,
    val questionId: String,
    val order: Int,
    val userAnswer: String? = null,
    val isCorrect: Boolean? = null,
    val timeSpentSeconds: Int? = null,
    val answeredAt: Long? = null,
    val skipped: Boolean = false
)