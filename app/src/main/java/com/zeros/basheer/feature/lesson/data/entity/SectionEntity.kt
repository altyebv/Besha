package com.zeros.basheer.feature.lesson.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A logical section within a lesson.
 *
 * Sections group related blocks together and link to concepts.
 * Example sections: "مقدمة", "الشرح", "أمثلة", "ملخص"
 */
@Entity(
    tableName = "sections",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lessonId")]
)
data class SectionEntity(
    @PrimaryKey
    val id: String,                         // e.g., "geo_u1_l1_s1"
    val lessonId: String,
    val title: String,                      // "مقدمة", "الشرح", etc.
    val order: Int,
    val learningType: LearningType = LearningType.UNDERSTANDING,
    /**
     * Groups sections into lesson parts (0-indexed).
     *
     * Sections sharing the same [partIndex] belong to the same part.
     * The reader derives the part-progress pills from distinct partIndex values.
     *
     * Default 0 means all existing sections are implicitly Part 1 —
     */
    val partIndex: Int = 0
)