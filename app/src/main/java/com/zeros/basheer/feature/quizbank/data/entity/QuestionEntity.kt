package com.zeros.basheer.feature.quizbank.data.entity


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity
import com.zeros.basheer.feature.lesson.data.entity.SectionEntity
import com.zeros.basheer.feature.quizbank.domain.model.CognitiveLevel
import com.zeros.basheer.feature.quizbank.domain.model.QuestionSource
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
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
        ),
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("subjectId"),
        Index("unitId"),
        Index("lessonId"),
        Index("sectionId"),
        Index("type"),
        Index("difficulty"),
        Index("source"),
        Index("sourceExamId"),
        Index("isCheckpoint")
    ]
)
data class QuestionEntity(
    @PrimaryKey
    val id: String,
    val subjectId: String,
    val unitId: String? = null,
    val lessonId: String? = null,
    /**
     * Links this question to a specific section for checkpoint use.
     * When [isCheckpoint] is true, this is the section gate the question guards.
     * FK → sections(id) ON DELETE SET NULL.
     */
    val sectionId: String? = null,
    val type: QuestionType,
    val textAr: String,
    val textEn: String? = null,
    val correctAnswer: String,
    val options: String? = null,
    val explanation: String? = null,
    val imageUrl: String? = null,
    val tableData: String? = null,
    val source: QuestionSource,
    val sourceExamId: String? = null,
    val sourceDetails: String? = null,
    val sourceYear: Int? = null,
    val difficulty: Int = 1,
    val cognitiveLevel: CognitiveLevel,
    val points: Int = 1,
    val estimatedSeconds: Int = 60,
    val feedEligible: Boolean = true,
    /**
     * When true, this question is an inline lesson checkpoint.
     * It will be surfaced by [QuestionDao.getCheckpointForSection]
     * and rendered as a gate card inside the lesson reader.
     *
     * Checkpoint questions should use type MCQ or ORDER only.
     * The [explanation] field doubles as the remediation hint shown on wrong answers.
     */
    val isCheckpoint: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)