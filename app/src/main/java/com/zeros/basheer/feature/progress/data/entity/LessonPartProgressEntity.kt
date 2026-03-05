package com.zeros.basheer.feature.progress.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity

/**
 * Tracks completion of individual lesson parts.
 *
 * A lesson is only marked complete once ALL parts have a completed entry here.
 * id is "${lessonId}_part_${partIndex}" — naturally unique, safe to IGNORE on conflict.
 */
@Entity(
    tableName = "lesson_part_progress",
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
data class LessonPartProgressEntity(
    @PrimaryKey val id: String,          // "${lessonId}_part_${partIndex}"
    val lessonId: String,
    val partIndex: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val timeSpentSeconds: Int = 0
) {
    companion object {
        fun makeId(lessonId: String, partIndex: Int) = "${lessonId}_part_$partIndex"
        fun create(lessonId: String, partIndex: Int, timeSpent: Int = 0) =
            LessonPartProgressEntity(
                id = makeId(lessonId, partIndex),
                lessonId = lessonId,
                partIndex = partIndex,
                timeSpentSeconds = timeSpent
            )
    }
}