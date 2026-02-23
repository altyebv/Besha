package com.zeros.basheer.feature.lesson.data.entity

/**
 * Describes the primary cognitive challenge of a section.
 *
 * Used to drive downstream UX decisions:
 * - UNDERSTANDING → surfaces Lab and Practice sessions
 * - MEMORIZATION  → queues section content into Feed cards
 * - HYBRID        → both behaviors applied
 */
enum class LearningType {
    UNDERSTANDING,
    MEMORIZATION,
    HYBRID
}