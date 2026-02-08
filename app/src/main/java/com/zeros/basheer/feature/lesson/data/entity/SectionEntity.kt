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
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index("lessonId")]
)
data class SectionEntity(
    @PrimaryKey
    val id: String,                         // e.g., "geo_u1_l1_s1"
    val lessonId: String,
    val title: String,                      // "مقدمة", "الشرح", etc.
    val order: Int
)