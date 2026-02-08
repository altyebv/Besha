package com.zeros.basheer.feature.concept.data.entity


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "concept_reviews",
    foreignKeys = [
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conceptId", unique = true),
        Index("nextReviewAt")
    ]
)
data class ConceptReviewEntity(
    @PrimaryKey
    val conceptId: String,
    val firstSeenAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null,
    val nextReviewAt: Long,
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f
)