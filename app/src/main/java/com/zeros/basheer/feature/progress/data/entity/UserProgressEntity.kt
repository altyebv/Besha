package com.zeros.basheer.feature.progress.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity

/**
 * Room entity for tracking user progress at the LESSON level.
 *
 * Also tracks which sections have been completed for partial progress.
 */
@Entity(
    tableName = "user_progress",
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
data class UserProgressEntity(
    @PrimaryKey
    val lessonId: String,
    val completed: Boolean = false,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val completedSections: String = "",     // Comma-separated section IDs
    val timeSpentSeconds: Int = 0,          // Total time spent on this lesson
    val notes: String = "",                  // User's personal notes
    val progress: Float = 0f
)