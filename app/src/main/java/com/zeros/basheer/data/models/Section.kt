package com.zeros.basheer.data.models

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
            entity = Lesson::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lessonId")]
)
data class Section(
    @PrimaryKey 
    val id: String,                         // e.g., "geo_u1_l1_s1"
    val lessonId: String,
    val title: String,                      // "مقدمة", "الشرح", etc.
    val order: Int
)
