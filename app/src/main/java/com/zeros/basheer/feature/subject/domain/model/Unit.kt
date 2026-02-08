package com.zeros.basheer.feature.subject.domain.model


/**
 * Clean domain model for Unit (no Room annotations)
 */
data class Unit(
    val id: String,
    val subjectId: String,
    val title: String,
    val order: Int,
    val description: String? = null,
    val estimatedHours: Float? = null
)