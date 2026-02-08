package com.zeros.basheer.feature.lesson.domain.model

import androidx.room.PrimaryKey

data class LessonDomain(
    val id: String,
    val unitId: String,
    val title: String,
    val order: Int,
    val estimatedMinutes: Int,
    val summary: String?,
    val isCompleted: Boolean = false,
    val progress: Float = 0f
)