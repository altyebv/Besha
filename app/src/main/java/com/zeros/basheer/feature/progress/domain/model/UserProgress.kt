package com.zeros.basheer.feature.progress.domain.model


/**
 * Domain model for user progress (no Room annotations).
 * Used by UI layer.
 */
data class UserProgress(
    val lessonId: String,
    val completed: Boolean = false,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val completedSections: String = "",
    val timeSpentSeconds: Int = 0,
    val notes: String = "",
    val progress: Float = 0f
)