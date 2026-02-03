package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Curated feed content for spaced repetition and quick review.
 *
 * FeedItems are MANUALLY created - you decide what's "feed-worthy":
 * - Key definitions that must stick
 * - Important formulas
 * - Historical dates
 * - Common exam concepts
 *
 * The feed algorithm shows items based on:
 * 1. Concepts the student has seen (via SectionConcept)
 * 2. ConceptReview.nextReviewAt for spaced repetition
 * 3. FeedItem.priority for importance weighting
 */
@Entity(
    tableName = "feed_items",
    foreignKeys = [
        ForeignKey(
            entity = Concept::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("conceptId"),
        Index("subjectId"),
        Index("type")
    ]
)
data class FeedItem(
    @PrimaryKey
    val id: String,                          // e.g., "geo_feed_latitude_def"
    val conceptId: String,                   // Links to the concept being reviewed
    val subjectId: String,                   // Denormalized for easier filtering
    val type: FeedItemType,

    // Content
    val contentAr: String,                   // The actual content to display (Arabic)
    val contentEn: String? = null,           // Optional English version
    val imageUrl: String? = null,            // Optional image/diagram

    // For interactive types (MINI_QUIZ)
    val interactionType: InteractionType? = null,
    val correctAnswer: String? = null,       // For T/F: "true"/"false", MCQ: correct option
    val options: String? = null,             // JSON array for MCQ options
    val explanation: String? = null,         // Why this answer is correct

    // Metadata
    val priority: Int = 1,                   // 1-5, higher = show more often
    val order: Int = 0                       // Order within same concept's feed items
)

/**
 * Types of feed items for display purposes.
 */
enum class FeedItemType {
    DEFINITION,      // تعريف - Key term, tap to confirm understanding
    FORMULA,         // معادلة - Mathematical formula
    DATE,            // تاريخ - Historical date/event
    FACT,            // حقيقة - Important fact to remember
    RULE,            // قاعدة - Rule or principle
    TIP,             // نصيحة - Study tip or mnemonic
    MINI_QUIZ        // سؤال سريع - Interactive question
}

/**
 * How the user interacts with feed items.
 */
enum class InteractionType {
    TAP_CONFIRM,     // Just tap "Got it" / "فهمت"
    SWIPE_TF,        // Swipe left (خطأ) / right (صح) for True/False
    MCQ,             // Multiple choice selection
    MATCH            // Match items (drag and drop)
}