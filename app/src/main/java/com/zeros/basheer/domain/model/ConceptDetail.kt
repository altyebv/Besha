package com.zeros.basheer.domain.model

import com.zeros.basheer.feature.concept.domain.model.ConceptType


/**
 * UI-ready concept detail for modal display.
 */
data class ConceptDetail(
    val id: String,
    val type: ConceptType,
    val titleAr: String,
    val titleEn: String?,
    val definition: String,
    val shortDefinition: String?,
    val formula: String?,
    val imageUrl: String?,
    val difficulty: Int,
    val tags: List<TagUiModel> = emptyList(),
    val variants: List<VariantUiModel> = emptyList()
)

data class TagUiModel(
    val id: String,
    val nameAr: String,
    val color: String?
)

data class VariantUiModel(
    val id: String,
    val type: String,
    val source: String,
    val contentAr: String,
    val authorName: String?,
    val authorTitle: String?
)
