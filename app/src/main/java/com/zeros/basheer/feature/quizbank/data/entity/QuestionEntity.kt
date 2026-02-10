package com.zeros.basheer.feature.quizbank.data.entity


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity
import com.zeros.basheer.feature.subject.data.entity.SubjectEntity
import com.zeros.basheer.feature.subject.data.entity.UnitEntity

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("subjectId"),
        Index("unitId"),
        Index("lessonId"),
        Index("type"),
        Index("difficulty"),
        Index("source"),
        Index("sourceExamId")
    ]
)
data class QuestionEntity(
    @PrimaryKey
    val id: String,
    val subjectId: String,
    val unitId: String? = null,
    val lessonId: String? = null,
    val type: String,
    val textAr: String,
    val textEn: String? = null,
    val correctAnswer: String,
    val options: String? = null,
    val explanation: String? = null,
    val imageUrl: String? = null,
    val tableData: String? = null,
    val source: String,
    val sourceExamId: String? = null,
    val sourceDetails: String? = null,
    val sourceYear: Int? = null,
    val difficulty: Int = 1,
    val cognitiveLevel: String,
    val points: Int = 1,
    val estimatedSeconds: Int = 60,
    val feedEligible: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)