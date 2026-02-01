package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_progress",
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
data class UserProgress(
    @PrimaryKey val lessonId: String,
    val completed: Boolean = false,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val highlightedSections: String = "", // JSON array of highlighted text ranges
    val notes: String = "",
    val completedAt: Long? = null
)