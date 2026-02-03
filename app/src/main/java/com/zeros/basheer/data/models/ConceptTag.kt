package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table linking Concepts to Tags.
 * 
 * Enables queries like:
 *   - "Which concepts have the 'maps' tag?"
 *   - "What tags does this concept have?"
 */
@Entity(
    tableName = "concept_tags",
    primaryKeys = ["conceptId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = Concept::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conceptId"),
        Index("tagId")
    ]
)
data class ConceptTag(
    val conceptId: String,
    val tagId: String
)
