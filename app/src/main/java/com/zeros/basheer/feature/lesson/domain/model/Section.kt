package com.zeros.basheer.feature.lesson.domain.model

import androidx.room.PrimaryKey


data class Section(
    val id: String,
    val lessonId: String,
    val title: String,
    val order: Int,
    val blocks: List<Block> = emptyList()
)