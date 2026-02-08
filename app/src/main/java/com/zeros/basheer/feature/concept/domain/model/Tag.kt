package com.zeros.basheer.feature.concept.domain.model


data class Tag(
    val id: String,
    val nameAr: String,
    val nameEn: String? = null,
    val color: String? = null
)

data class ConceptTag(
    val conceptId: String,
    val tagId: String
)