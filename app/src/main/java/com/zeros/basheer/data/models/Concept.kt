package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A key concept/definition in a subject.
 * 
 * Concepts are the CORE of the app - everything connects through them:
 * - Sections introduce concepts (via SectionConcept)
 * - Blocks can define concepts (via Block.conceptRef)
 * - Questions test concepts (via QuestionConcept)
 * - Feeds review concepts (via ConceptReview)
 * - Tags categorize concepts (via ConceptTag)
 */
@Entity(
    tableName = "concepts",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subjectId"),
        Index("type")
    ]
)
data class Concept(
    @PrimaryKey 
    val id: String,                         // e.g., "geo_c_latitude"
    val subjectId: String,
    val type: ConceptType,
    val titleAr: String,                    // "خطوط العرض"
    val titleEn: String? = null,            // "Latitude"
    val definition: String,                 // Full explanation
    val shortDefinition: String? = null,    // For flashcards/quick review
    val formula: String? = null,            // For FORMULA/LAW types (LaTeX)
    val imageUrl: String? = null,           // Optional diagram/illustration
    val difficulty: Int = 1,                // 1-5 for spaced repetition
    
    // For COMPARISON type: JSON with itemA, itemB, differences
    // For PROCESS type: JSON with steps array
    // For DATE type: the actual date/year
    val extraData: String? = null
)

enum class ConceptType {
    DEFINITION,         // Key term definition (تعريف)
    FORMULA,            // Mathematical formula (قانون/معادلة)
    DATE,               // Historical date/event (تاريخ)
    PERSON,             // Historical figure/scientist (شخصية)
    LAW,                // Scientific law/principle (قاعدة/مبدأ)
    FACT,               // Important fact (حقيقة)
    PROCESS,            // Step-by-step process (عملية/خطوات)
    COMPARISON,         // A vs B comparison (مقارنة)
    PLACE,              // Geographic location (مكان)
    CAUSE_EFFECT        // Cause and effect relationship (سبب ونتيجة)
}
