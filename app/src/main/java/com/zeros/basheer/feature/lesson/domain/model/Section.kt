package com.zeros.basheer.feature.lesson.domain.model

import androidx.room.PrimaryKey


import com.zeros.basheer.feature.lesson.data.entity.LearningType

data class Section(
    val id: String,
    val lessonId: String,
    val title: String,
    val order: Int,
    val learningType: LearningType = LearningType.UNDERSTANDING,
    val blocks: List<Block> = emptyList(),
    /** Matches [SectionEntity.partIndex]. Sections with the same value belong to the same part. */
    val partIndex: Int = 0
)