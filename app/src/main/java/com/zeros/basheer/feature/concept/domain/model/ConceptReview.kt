package com.zeros.basheer.feature.concept.domain.model


data class ConceptReview(
    val conceptId: String,
    val firstSeenAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null,
    val nextReviewAt: Long,
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f
)

enum class Rating {
    FORGOT,    // 0 - Complete blackout
    HARD,      // 3 - Recalled with difficulty
    GOOD,      // 4 - Recalled correctly
    EASY       // 5 - Perfect recall, too easy
}