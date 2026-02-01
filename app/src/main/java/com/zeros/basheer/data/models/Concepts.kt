package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    indices = [Index("subjectId"), Index("type")]
)
data class Concept(
    @PrimaryKey val id: String, // e.g., "geo_concept_latitude"
    val subjectId: String,
    val type: ConceptType,
    val title: String, // "خطوط العرض" or "Latitude"
    val content: String, // The actual definition/explanation
    val formula: String? = null, // For math/physics formulas
    val imageUrl: String? = null, // Optional diagram/illustration
    val tags: String = "", // "coordinates,maps,navigation"
    val difficulty: Int = 1, // 1-5 scale for spaced repetition
    val relatedLessonIds: String = "" // Comma-separated lesson IDs
)

enum class ConceptType {
    DEFINITION,     // Key term definition
    FORMULA,        // Mathematical formula
    DATE,           // Historical date/event
    PERSON,         // Historical figure/scientist
    LAW,            // Scientific law/principle
    FACT,           // Important fact
    PROCESS,        // Step-by-step process
    COMPARISON      // A vs B comparison
}