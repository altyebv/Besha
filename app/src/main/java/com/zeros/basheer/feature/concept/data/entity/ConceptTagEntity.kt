package com.zeros.basheer.feature.concept.data.entity


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "concept_tags",
    primaryKeys = ["conceptId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
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
data class ConceptTagEntity(
    val conceptId: String,
    val tagId: String
)