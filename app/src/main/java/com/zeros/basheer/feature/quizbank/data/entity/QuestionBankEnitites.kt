package com.zeros.basheer.feature.quizbank.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.concept.data.entity.ConceptEntity
import com.zeros.basheer.feature.subject.data.entity.SubjectEntity

@Entity(
    tableName = "exams",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subjectId"),
        Index("source"),
        Index("year")
    ]
)
data class ExamEntity(
    @PrimaryKey
    val id: String,
    val subjectId: String,
    val titleAr: String,
    val titleEn: String? = null,
    val source: String,
    val year: Int? = null,
    val schoolName: String? = null,
    val duration: Int? = null,
    val totalPoints: Int? = null,
    val description: String? = null
)

@Entity(
    tableName = "exam_questions",
    primaryKeys = ["examId", "questionId"],
    foreignKeys = [
        ForeignKey(
            entity = ExamEntity::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
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
        Index("examId"),
        Index("questionId")
    ]
)
data class ExamQuestionEntity(
    val examId: String,
    val questionId: String,
    val order: Int,
    val sectionLabel: String? = null,
    val points: Int? = null
)

@Entity(
    tableName = "quiz_attempts",
    foreignKeys = [
        ForeignKey(
            entity = ExamEntity::class,
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
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examId: String,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val score: Int? = null,
    val totalPoints: Int? = null,
    val percentage: Float? = null,
    val timeSpentSeconds: Int? = null
)

@Entity(
    tableName = "question_responses",
    foreignKeys = [
        ForeignKey(
            entity = QuizAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
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
        Index("attemptId"),
        Index("questionId")
    ]
)
data class QuestionResponseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val attemptId: Long,
    val questionId: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val pointsEarned: Int = 0,
    val timeSpentSeconds: Int? = null,
    val answeredAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "question_stats",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("questionId")]
)
data class QuestionStatsEntity(
    @PrimaryKey
    val questionId: String,
    val timesAsked: Int = 0,
    val timesCorrect: Int = 0,
    val avgTimeSeconds: Float = 0f,
    val successRate: Float = 0f,
    val lastShownInFeed: Long? = null,
    val feedShowCount: Int = 0,
    val lastAskedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)