package com.zeros.basheer.feature.feed.domain.algorithm

import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Resolves which subject should be the "anchor" for a feed session.
 *
 * The anchor subject receives the full algorithm (new discovery + learned
 * reinforcement + spaced-repetition review).  All other subjects only surface
 * cards whose spaced-repetition review is already overdue — no new material.
 *
 * Resolution order:
 *  1. Subject with the most-recent lesson activity (lastAccessedAt).
 *  2. Tie → subject with the lowest completion count (most to learn).
 *  3. No progress at all → first subject in the provided list.
 */
class AnchorSubjectResolver @Inject constructor(
    private val progressRepository: ProgressRepository
) {

    suspend fun resolve(subjectIds: List<String>): String? {
        if (subjectIds.isEmpty()) return null
        if (subjectIds.size == 1) return subjectIds.first()

        data class SubjectStats(
            val subjectId: String,
            val mostRecentAccess: Long,
            val completedCount: Int
        )

        val stats = subjectIds.map { subjectId ->
            val completedLessons = progressRepository
                .getCompletedLessonsBySubject(subjectId)
                .firstOrNull() ?: emptyList()

            SubjectStats(
                subjectId    = subjectId,
                mostRecentAccess = completedLessons.maxOfOrNull { it.lastAccessedAt } ?: 0L,
                completedCount   = completedLessons.size
            )
        }

        // Prefer the subject the user was most recently active in
        val mostRecent = stats.filter { it.mostRecentAccess > 0 }
            .maxByOrNull { it.mostRecentAccess }

        if (mostRecent != null) return mostRecent.subjectId

        // No progress anywhere yet → fall back to first subject
        return subjectIds.first()
    }
}