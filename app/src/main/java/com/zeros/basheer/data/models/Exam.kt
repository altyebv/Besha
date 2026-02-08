package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.subject.data.entity.SubjectEntity
import com.zeros.basheer.feature.subject.domain.model.Subject

/**
 * Exam metadata - represents a complete exam (ministry, school, practice).
 * 
 * Questions are linked via ExamQuestion junction table to allow:
 * - Same question appearing in multiple exams
 * - Custom question ordering per exam
 * - Point values can vary per exam
 */
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
data class Exam(
    @PrimaryKey
    val id: String,                         // e.g., "geo_exam_2023_ministry"
    val subjectId: String,
    val titleAr: String,                    // "امتحان الشهادة السودانية 2023"
    val titleEn: String? = null,
    val source: ExamSource,
    val year: Int? = null,                  // Year of the exam
    val schoolName: String? = null,         // For SCHOOL source
    val duration: Int? = null,              // Duration in minutes
    val totalPoints: Int? = null,           // Total possible points
    val description: String? = null
)

enum class ExamSource {
    MINISTRY,       // Official ministry exams (الشهادة السودانية)
    SCHOOL,         // School exams (من مدارس مختلفة)
    PRACTICE,       // Practice/sample exams (تدريبي)
    CUSTOM          // User-created or curated exams
}
