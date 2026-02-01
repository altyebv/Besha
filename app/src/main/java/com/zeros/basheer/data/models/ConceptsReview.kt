package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks when user saw a concept in feeds and how they interacted with it.
 * Used for spaced repetition algorithm.
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
    indices = [Index("conceptId"), Index("nextReviewAt")]
)
data class ConceptReview(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conceptId: String,
    val reviewedAt: Long = System.currentTimeMillis(),
    val userRating: Rating, // How well they knew it
    val nextReviewAt: Long, // When to show it again (spaced repetition)
    val reviewCount: Int = 1, // How many times they've seen it
    val intervalDays: Int = 1 // Current interval for spaced repetition
)

enum class Rating {
    FORGOT,      // Didn't remember at all (show again soon)
    HARD,        // Struggled to remember (shorter interval)
    GOOD,        // Remembered correctly (normal interval)
    EASY         // Very easy (longer interval)
}