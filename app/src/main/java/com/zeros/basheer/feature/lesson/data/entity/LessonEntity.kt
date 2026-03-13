package com.zeros.basheer.feature.lesson.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.subject.data.entity.UnitEntity

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("unitId")]
)
data class LessonEntity(
    @PrimaryKey val id: String,
    val unitId: String,
    val title: String,
    val order: Int,
    val estimatedMinutes: Int = 15,
    val summary: String? = null,
    /**
     * JSON blob for optional lesson-level metadata.
     * Parsed into [LessonMetadata] by LessonMapper.
     *
     * Shape:
     * {
     *   "hook": "هل تساءلت يوماً كيف...",
     *   "orientation": ["ستفهم...", "ستحسب..."],
     *   "forwardPull": "درس واحد يفصلك عن إكمال وحدة..."
     * }
     *
     * All fields are optional — a null metadata column is valid.
     */
    val metadata: String? = null
)