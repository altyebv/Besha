package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.zeros.basheer.feature.lesson.data.entity.SectionEntity

/**
 * Junction table linking Sections to Concepts.
 * 
 * This tracks which concepts are TAUGHT in which sections.
 * Different from Block.conceptRef which tracks which block DEFINES a concept.
 * 
 * Example:
 * - Section "مقدمة في الإحداثيات" teaches concepts: latitude, longitude, equator
 * - The HIGHLIGHT_BOX block that defines latitude has conceptRef = "latitude"
 */
@Entity(
    tableName = "section_concepts",
    primaryKeys = ["sectionId", "conceptId"],
    foreignKeys = [
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Concept::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sectionId"),
        Index("conceptId")
    ]
)
data class SectionConcept(
    val sectionId: String,
    val conceptId: String,
    val isPrimary: Boolean = false,         // Is this a key concept for this section?
    val order: Int = 0                      // Order of introduction in the section
)
