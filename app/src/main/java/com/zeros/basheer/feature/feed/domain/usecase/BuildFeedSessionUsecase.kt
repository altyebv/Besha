package com.zeros.basheer.feature.feed.domain.usecase

import com.zeros.basheer.feature.feed.domain.algorithm.AnchorSubjectResolver
import com.zeros.basheer.feature.feed.domain.algorithm.ConceptGroupBuilder
import com.zeros.basheer.feature.feed.domain.algorithm.FeedAlgorithmConfig
import com.zeros.basheer.feature.feed.domain.model.FeedCard
import com.zeros.basheer.feature.feed.domain.model.FeedItem
import com.zeros.basheer.feature.feed.domain.repository.FeedRepository
import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import com.zeros.basheer.feature.user.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Result wrapper so callers can distinguish a valid empty session from an error.
 */
sealed class FeedSessionResult {
    data class Success(val cards: List<FeedCard>) : FeedSessionResult()
    object NoSubjects : FeedSessionResult()  // profile has no subjects yet
    object NoContent  : FeedSessionResult()  // subjects exist but feed is empty (pre-seed)
}

/**
 * Builds a complete, ordered list of [FeedCard]s for a single feed session.
 *
 * ┌──────────────────────────────────────────────────────────────────┐
 * │  Algorithm overview                                               │
 * │                                                                   │
 * │  1. Resolve anchor subject (most-recently active).                │
 * │                                                                   │
 * │  2. Fetch three buckets for the anchor subject:                   │
 * │     A) SR due reviews          (~30 % of anchor)                 │
 * │     B) Recently learned        (~40 % of anchor)                 │
 * │     C) High-priority discovery (~30 % of anchor)                 │
 * │                                                                   │
 * │  3. Fetch SR-due reviews for maintenance subjects (separately,    │
 * │     per subject — prevents anchor from starving them).            │
 * │                                                                   │
 * │  4. Deduplicate anchor buckets (A > B > C).                      │
 * │                                                                   │
 * │  5. Check backlog — if unseen discovery items exceed threshold,   │
 * │     extend session by up to MAX_EXTENSION_SIZE extra cards.      │
 * │                                                                   │
 * │  6. Group anchor by concept; append quiz-bank tail per group.     │
 * │                                                                   │
 * │  7. Interleave maintenance cards at every Nth anchor slot.        │
 * │                                                                   │
 * │  8. Cap at BASE_SESSION_SIZE + extension.                         │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * All tunable constants live in [FeedAlgorithmConfig].
 * All quiz-placement logic lives in [ConceptGroupBuilder].
 */
class BuildFeedSessionUseCase @Inject constructor(
    private val feedRepository: FeedRepository,
    private val subjectRepository: SubjectRepository,
    private val userProfileRepository: UserProfileRepository,
    private val anchorSubjectResolver: AnchorSubjectResolver,
    private val conceptGroupBuilder: ConceptGroupBuilder
) {

    suspend operator fun invoke(): FeedSessionResult {

        // ── 1. Subjects ───────────────────────────────────────────────────────
        val profile    = userProfileRepository.getProfileOnce()
        val pathFilter = profile?.subjectsFilter ?: listOf(StudentPath.COMMON)
        val subjects   = subjectRepository.getSubjectsByPathFilter(pathFilter).first()

        if (subjects.isEmpty()) return FeedSessionResult.NoSubjects

        val subjectMap = subjects.associateBy { it.id }
        val subjectIds = subjects.map { it.id }

        // ── 2. Anchor resolution ──────────────────────────────────────────────
        val anchorId       = anchorSubjectResolver.resolve(subjectIds)
            ?: return FeedSessionResult.NoSubjects
        val maintenanceIds = subjectIds.filter { it != anchorId }

        // ── 3. Anchor SR bucket (fetched independently — never bleeds into maintenance quota) ──
        val anchorReviewItems = feedRepository
            .getFeedItemsDueForReview(
                currentTime = System.currentTimeMillis(),
                limit       = FeedAlgorithmConfig.ANCHOR_SR_FETCH_LIMIT
            )
            .first()
            .filter { it.subjectId == anchorId }

        // ── 4. Maintenance SR buckets — per subject, then cap total ───────────
        // Per-subject fetching gives every old subject a fair chance regardless
        // of how many overdue items the anchor has.
        val maintenanceReviewItems = buildList {
            for (subjectId in maintenanceIds) {
                if (size >= FeedAlgorithmConfig.MAINTENANCE_SR_TOTAL_CAP) break
                val remaining = FeedAlgorithmConfig.MAINTENANCE_SR_TOTAL_CAP - size
                val items = feedRepository
                    .getFeedItemsDueForReview(
                        currentTime = System.currentTimeMillis(),
                        limit       = minOf(FeedAlgorithmConfig.MAINTENANCE_SR_FETCH_LIMIT, remaining)
                    )
                    .first()
                    .filter { it.subjectId == subjectId }
                addAll(items)
            }
        }

        // ── 5. Remaining anchor buckets ───────────────────────────────────────
        val learnedItems = feedRepository
            .getFeedItemsForLearnedConcepts(
                subjectId = anchorId,
                limit     = FeedAlgorithmConfig.BUCKET_LEARNED_TARGET
            )
            .first()

        // Fetch discovery at full ceiling (base + max extension) so we can
        // measure the backlog without a second DB round-trip.
        val discoveryFetchLimit = FeedAlgorithmConfig.BUCKET_DISCOVERY_TARGET +
                FeedAlgorithmConfig.MAX_EXTENSION_SIZE
        val allDiscoveryItems   = feedRepository
            .getHighPriorityFeedItems(anchorId, discoveryFetchLimit)
            .first()

        val baseDiscoveryItems = allDiscoveryItems.take(FeedAlgorithmConfig.BUCKET_DISCOVERY_TARGET)

        // ── 6. Deduplicate anchor (A > B > C) ─────────────────────────────────
        val anchorItems = deduplicate(anchorReviewItems, learnedItems, baseDiscoveryItems)

        // ── 7. Fallback: user has no progress yet — serve discovery directly ──
        val finalAnchorItems = if (anchorItems.isEmpty()) {
            allDiscoveryItems.take(FeedAlgorithmConfig.ANCHOR_CARD_TARGET)
        } else {
            anchorItems
        }

        if (finalAnchorItems.isEmpty()) return FeedSessionResult.NoContent

        // ── 8. Session extension: ease in backlog of unseen concepts ──────────
        // surplus = items returned beyond the base discovery quota.
        // Scale: +1 extension slot per 5 pending items, capped at MAX_EXTENSION_SIZE.
        // Nothing happens until the backlog exceeds EXTENSION_THRESHOLD, so
        // occasional leftover items don't artificially inflate the session.
        val surplus       = (allDiscoveryItems.size - FeedAlgorithmConfig.BUCKET_DISCOVERY_TARGET)
            .coerceAtLeast(0)
        val extensionSize = when {
            surplus < FeedAlgorithmConfig.EXTENSION_THRESHOLD -> 0
            else -> (surplus / 5).coerceAtMost(FeedAlgorithmConfig.MAX_EXTENSION_SIZE)
        }

        val extensionItems = allDiscoveryItems
            .drop(FeedAlgorithmConfig.BUCKET_DISCOVERY_TARGET)
            .take(extensionSize)

        val sessionLimit = FeedAlgorithmConfig.BASE_SESSION_SIZE + extensionSize

        // ── 9. Build anchor cards with concept-grouped quiz injection ──────────
        val anchorCards = conceptGroupBuilder.build(
            items      = finalAnchorItems + extensionItems,
            subjectMap = subjectMap,
            quizBudget = FeedAlgorithmConfig.MAX_QUIZZES_PER_SESSION
        )

        // ── 10. Map maintenance cards (no quiz injection) ──────────────────────
        val maintenanceCards = com.zeros.basheer.feature.feed.data.mapper.FeedMapper
            .toFeedCards(maintenanceReviewItems, subjectMap)

        // ── 11. Interleave and cap ─────────────────────────────────────────────
        val session = interleave(
            anchor      = anchorCards,
            maintenance = maintenanceCards,
            interval    = FeedAlgorithmConfig.MAINTENANCE_SLOT_INTERVAL
        ).take(sessionLimit)

        return FeedSessionResult.Success(session)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Merges buckets in priority order, deduplicating by [FeedItem.id].
     */
    private fun deduplicate(vararg buckets: List<FeedItem>): List<FeedItem> {
        val seen   = mutableSetOf<String>()
        val result = mutableListOf<FeedItem>()
        for (bucket in buckets) {
            for (item in bucket) {
                if (seen.add(item.id)) result.add(item)
            }
        }
        return result
    }

    /**
     * Inserts a maintenance card every [interval] anchor cards.
     * [A][A][A][M][A][A][A][M]…
     * Remaining maintenance appends after anchor is exhausted.
     */
    private fun interleave(
        anchor: List<FeedCard>,
        maintenance: List<FeedCard>,
        interval: Int
    ): List<FeedCard> {
        if (maintenance.isEmpty()) return anchor

        val result      = mutableListOf<FeedCard>()
        val anchorIter  = anchor.iterator()
        val maintIter   = maintenance.iterator()
        var anchorCount = 0

        while (anchorIter.hasNext()) {
            result.add(anchorIter.next())
            anchorCount++
            if (anchorCount % interval == 0 && maintIter.hasNext()) {
                result.add(maintIter.next())
            }
        }
        while (maintIter.hasNext()) result.add(maintIter.next())

        return result
    }
}