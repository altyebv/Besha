package com.zeros.basheer.feature.user.data.repository

import com.zeros.basheer.feature.streak.data.entity.StreakLevel
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import com.zeros.basheer.feature.user.data.dao.XpDao
import com.zeros.basheer.feature.user.data.entity.XpTransactionEntity
import com.zeros.basheer.feature.user.domain.model.XpSource
import com.zeros.basheer.feature.user.domain.model.XpSummary
import com.zeros.basheer.feature.user.domain.model.XpTransaction
import com.zeros.basheer.feature.user.domain.repository.XpRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class XpRepositoryImpl @Inject constructor(
    private val dao: XpDao,
    private val streakRepository: StreakRepository
) : XpRepository {

    override fun observeXpSummary(): Flow<XpSummary> =
        dao.observeTotalXp().map { XpSummary.Companion.from(it) }

    override suspend fun awardXp(
        source: XpSource,
        referenceId: String?,
        streakMultiplier: Float
    ): XpTransaction? {
        // ── Deduplication ─────────────────────────────────────────────────────
        // For sources that support deduplication, check if we've already awarded
        // a COMPLETE/first-time variant. If yes, downgrade to repeat or skip.
        val effectiveSource = if (referenceId != null) {
            resolveEffectiveSource(source, referenceId)
        } else {
            source
        }

        // Some repeat sources still earn XP (LESSON_REPEAT), others are skipped
        effectiveSource ?: return null

        // ── Multiplier calculation ─────────────────────────────────────────────
        val streakLevel = streakRepository.getCurrentStreakLevel()
        val streakBonus = when (streakLevel) {
            StreakLevel.FLAME  -> 1.5f
            StreakLevel.SPARK -> 1.2f
            StreakLevel.COLD -> 1.0f
        }
        val combined = streakMultiplier * streakBonus

        val baseXp = effectiveSource.baseXp
        val finalXp = (baseXp * combined).toInt().coerceAtLeast(1)

        // ── Write transaction ──────────────────────────────────────────────────
        val entity = XpTransactionEntity(
            amount = finalXp,
            baseAmount = baseXp,
            source = effectiveSource,
            multiplier = combined,
            referenceId = referenceId
        )
        val id = dao.insert(entity)

        return XpTransaction(
            id = id,
            amount = finalXp,
            baseAmount = baseXp,
            source = effectiveSource,
            multiplier = combined,
            referenceId = referenceId
        )
    }

    override suspend fun getTotalXp(): Int = dao.getTotalXp()

    override suspend fun getXpToday(): Int {
        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return dao.getXpToday(startOfDay)
    }

    override suspend fun hasBeenAwarded(source: XpSource, referenceId: String): Boolean =
        dao.countTransactions(source, referenceId) > 0

    // ── Deduplication logic ────────────────────────────────────────────────────

    /**
     * Resolves whether an action earns full, reduced, or no XP based on history.
     *
     * Rules:
     * - LESSON_COMPLETE: if already awarded → downgrade to LESSON_REPEAT
     * - EXAM_COMPLETE: if already awarded for this attemptId → skip (null)
     * - PRACTICE_COMPLETE: if already awarded for this sessionId → skip (null)
     * - CARD_* sources: no deduplication (feed cards are ephemeral)
     *
     * Returns the effective XpSource to use, or null to skip entirely.
     */
    private suspend fun resolveEffectiveSource(
        source: XpSource,
        referenceId: String
    ): XpSource? = when (source) {
        XpSource.LESSON_COMPLETE -> {
            val alreadyCompleted = dao.countTransactions(XpSource.LESSON_COMPLETE, referenceId) > 0
            if (alreadyCompleted) XpSource.LESSON_REPEAT else XpSource.LESSON_COMPLETE
        }
        XpSource.EXAM_COMPLETE -> {
            val alreadyAwarded = dao.countTransactions(XpSource.EXAM_COMPLETE, referenceId) > 0
            if (alreadyAwarded) null else XpSource.EXAM_COMPLETE
        }
        XpSource.PRACTICE_COMPLETE -> {
            val alreadyAwarded = dao.countTransactions(XpSource.PRACTICE_COMPLETE, referenceId) > 0
            if (alreadyAwarded) null else XpSource.PRACTICE_COMPLETE
        }
        else -> source // CARD_REVIEWED, CARD_CORRECT — no deduplication
    }
}