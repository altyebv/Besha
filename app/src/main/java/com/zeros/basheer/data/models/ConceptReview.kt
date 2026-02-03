package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks spaced repetition reviews for concepts.
 * 
 * This is what drives the Feed algorithm:
 * - Concepts with nextReviewAt <= now() appear in feeds
 * - The SM-2 algorithm adjusts intervals based on user rating
 */
@Entity(
    tableName = "concept_reviews",
    foreignKeys = [
        ForeignKey(
            entity = Concept::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conceptId", unique = true),  // One review record per concept
        Index("nextReviewAt")
    ]
)
data class ConceptReview(
    @PrimaryKey
    val conceptId: String,                  // Changed from autoGenerate - one per concept
    val firstSeenAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null,
    val nextReviewAt: Long,                 // When to show again
    val reviewCount: Int = 0,               // How many times reviewed
    val correctCount: Int = 0,              // How many times answered correctly
    val intervalDays: Int = 1,              // Current interval for SM-2
    val easeFactor: Float = 2.5f            // SM-2 ease factor
)

/**
 * Rating for a concept review.
 * Maps to SM-2 algorithm quality scores.
 */
enum class Rating {
    FORGOT,         // 0 - Complete blackout
    HARD,           // 3 - Recalled with difficulty
    GOOD,           // 4 - Recalled correctly
    EASY            // 5 - Perfect recall, too easy
}
