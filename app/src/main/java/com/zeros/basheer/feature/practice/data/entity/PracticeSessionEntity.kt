package com.zeros.basheer.feature.practice.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.subject.data.entity.SubjectEntity

@Entity(
    tableName = "practice_sessions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [
        Index("subjectId"),
        Index("generationType"),
        Index("startedAt")
    ]
)
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: String,
    val generationType: String,
    val filterUnitIds: String? = null,
    val filterLessonIds: String? = null,
    val filterConceptIds: String? = null,
    val filterQuestionTypes: String? = null,
    val filterDifficulty: String? = null,
    val filterSource: String? = null,
    val questionCount: Int,
    val timeLimitSeconds: Int? = null,
    val shuffled: Boolean = true,
    val status: String = "IN_PROGRESS",
    val currentQuestionIndex: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val skippedCount: Int = 0,
    val score: Float? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val totalTimeSeconds: Int? = null
)