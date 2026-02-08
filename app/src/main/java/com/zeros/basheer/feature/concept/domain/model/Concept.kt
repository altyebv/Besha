package com.zeros.basheer.feature.concept.domain.model


data class Concept(
    val id: String,
    val subjectId: String,
    val type: ConceptType,
    val titleAr: String,
    val titleEn: String? = null,
    val definition: String,
    val shortDefinition: String? = null,
    val formula: String? = null,
    val imageUrl: String? = null,
    val difficulty: Int = 1,
    val extraData: String? = null
)

enum class ConceptType {
    DEFINITION,
    FORMULA,
    DATE,
    PERSON,
    LAW,
    FACT,
    PROCESS,
    COMPARISON,
    PLACE,
    CAUSE_EFFECT
}