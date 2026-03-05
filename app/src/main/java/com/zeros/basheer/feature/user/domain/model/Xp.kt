package com.zeros.basheer.feature.user.domain.model

/**
 * Every action that can earn XP.
 * Also carries the base XP value for that action.
 */
enum class XpSource(val baseXp: Int) {
    LESSON_COMPLETE(20),       // First time completing a lesson (all parts done)
    LESSON_PART_COMPLETE(5),   // First time completing a single mid-lesson part
    LESSON_REPEAT(5),          // Completing an already-completed lesson
    CARD_REVIEWED(2),          // Reviewing a feed card (any answer)
    CARD_CORRECT(3),           // Answering a feed card correctly (stacks with CARD_REVIEWED)
    PRACTICE_COMPLETE(15),     // Completing a practice session
    EXAM_COMPLETE(30),         // Completing a full exam
}

/**
 * A single XP award event — immutable once written.
 */
data class XpTransaction(
    val id: Long = 0,
    val amount: Int,               // Final XP after multipliers
    val baseAmount: Int,           // Raw base before multipliers
    val source: XpSource,
    val multiplier: Float,         // Combined multiplier applied
    val referenceId: String?,      // lessonId / sessionId etc. for deduplication
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Computed summary derived from all transactions.
 */
data class XpSummary(
    val totalXp: Int,
    val level: Int,
    val xpInCurrentLevel: Int,   // XP accumulated within the current level
    val xpToNextLevel: Int,      // XP still needed to reach next level
    val progressInLevel: Float   // 0f–1f for UI progress bar
) {
    companion object {
        /** XP required to reach a given level. Level 1 = 0 XP. */
        fun xpRequiredForLevel(level: Int): Int = if (level <= 1) 0 else 100 * (level - 1) * (level - 1)

        /** Compute full summary from a raw total. */
        fun from(totalXp: Int): XpSummary {
            var level = 1
            while (xpRequiredForLevel(level + 1) <= totalXp) level++

            val xpForCurrentLevel = xpRequiredForLevel(level)
            val xpForNextLevel = xpRequiredForLevel(level + 1)
            val xpInLevel = totalXp - xpForCurrentLevel
            val xpNeeded = xpForNextLevel - xpForCurrentLevel

            return XpSummary(
                totalXp = totalXp,
                level = level,
                xpInCurrentLevel = xpInLevel,
                xpToNextLevel = xpForNextLevel - totalXp,
                progressInLevel = if (xpNeeded > 0) xpInLevel.toFloat() / xpNeeded else 1f
            )
        }
    }
}