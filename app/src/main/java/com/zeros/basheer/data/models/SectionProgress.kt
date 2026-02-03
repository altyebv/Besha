package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks user progress at the SECTION level.
 *
 * This enables:
 * - Granular progress tracking (which sections completed within a lesson)
 * - Resume functionality (open lesson at last viewed section)
 * - Time analytics per section
 * - Partial lesson completion
 *
 * Note: UserProgress still tracks lesson-level data (notes, total time).
 * SectionProgress handles the granular section-by-section tracking.
 */
@Entity(
    tableName = "section_progress",
    foreignKeys = [
        ForeignKey(
            entity = Section::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Lesson::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sectionId", unique = true),   // One progress record per section
        Index("lessonId"),                   // For querying all sections in a lesson
        Index("completed")                   // For filtering completed sections
    ]
)
data class SectionProgress(
    @PrimaryKey
    val sectionId: String,                   // The section being tracked
    val lessonId: String,                    // Denormalized for easier queries
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val firstAccessedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val timeSpentSeconds: Int = 0,           // Time spent on this section
    val scrollPosition: Float = 0f           // 0.0 to 1.0, for resume functionality
)