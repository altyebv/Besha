package com.zeros.basheer.feature.subject.domain.model


import com.zeros.basheer.feature.subject.data.entity.StudentPath

/**
 * Clean domain model for Subject (no Room annotations)
 */
data class Subject(
    val id: String,
    val nameAr: String,
    val nameEn: String? = null,
    val path: StudentPath,
    val isMajor: Boolean = false,
    val order: Int = 0,
    val iconRes: String? = null,
    val colorHex: String? = null
)