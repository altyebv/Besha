package com.zeros.basheer.feature.feed.domain.usecase

import com.zeros.basheer.feature.concept.domain.model.ConceptReview
import com.zeros.basheer.feature.feed.domain.algorithm.FeedAlgorithmConfig
import com.zeros.basheer.feature.quizbank.domain.model.CognitiveLevel
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.model.QuestionSource
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Selects the single best feed-eligible question for a concept.
 *
 * Selection pipeline:
 *  1. Fetch all questions linked to [conceptId] via question_concepts.
 *  2. Keep only feed-eligible types (TRUE_FALSE, MCQ).
 *  3. Gate on cognitive level derived from the concept's review history.
 *  4. Filter out questions shown in feed within the cooldown window.
 *  5. Score remaining candidates and return the highest scorer (or null).
 *
 * Returns null when no suitable question exists — the caller simply skips
 * appending a quiz card for that concept group.
 */
class SelectFeedQuestionUseCase @Inject constructor(
    private val quizBankRepository: QuizBankRepository
) {

    suspend operator fun invoke(
        conceptId: String,
        review: ConceptReview?
    ): Question? {
        val maxLevel  = review.toMaxCognitiveLevel()
        val cooldownMs = TimeUnit.DAYS.toMillis(FeedAlgorithmConfig.QUESTION_FEED_COOLDOWN_DAYS)
        val cutoffTime = System.currentTimeMillis() - cooldownMs

        // Pull all questions linked to this concept
        val candidates = quizBankRepository
            .getQuestionsForConcepts(listOf(conceptId), limit = 20)

        if (candidates.isEmpty()) return null

        // Load stats for all candidates in one shot
        val statsMap = quizBankRepository
            .getStatsForQuestions(candidates.map { it.id })
            .associateBy { it.questionId }

        val eligible = candidates
            .filter { it.feedEligible }
            .filter { it.type.isFeedCompatible() }
            .filter { it.cognitiveLevel <= maxLevel }

        // First pass: respect cooldown window — prefer fresh questions.
        val preferred = eligible
            .filter { question ->
                val lastShown = statsMap[question.id]?.lastShownInFeed ?: 0L
                lastShown < cutoffTime
            }
            .maxByOrNull { question -> score(question, statsMap[question.id]?.feedShowCount ?: 0) }

        if (preferred != null) return preferred

        // Second pass: all questions are within the cooldown window (user reviews daily).
        // Fall back to RECALL-level only — easier pill to re-swallow — ignoring cooldown.
        return eligible
            .filter { it.cognitiveLevel == CognitiveLevel.RECALL }
            .minByOrNull { statsMap[it.id]?.lastShownInFeed ?: 0L }  // oldest shown first
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun score(question: Question, feedShowCount: Int): Int {
        var score = question.difficulty * FeedAlgorithmConfig.SCORE_DIFFICULTY_WEIGHT

        if (question.source.isMinistry()) {
            score += FeedAlgorithmConfig.SCORE_MINISTRY_SOURCE_BONUS
        }

        if (feedShowCount == 0) {
            score += FeedAlgorithmConfig.SCORE_NEVER_IN_FEED_BONUS
        }

        return score
    }

    /**
     * Derives the highest [CognitiveLevel] we can present based on how well
     * the user knows this concept (spaced-repetition maturity).
     */
    private fun ConceptReview?.toMaxCognitiveLevel(): CognitiveLevel {
        if (this == null || reviewCount == 0) return CognitiveLevel.RECALL
        if (easeFactor >= FeedAlgorithmConfig.ANALYZE_MIN_EASE_FACTOR) return CognitiveLevel.ANALYZE
        if (easeFactor >= FeedAlgorithmConfig.APPLY_MIN_EASE_FACTOR) return CognitiveLevel.APPLY
        if (reviewCount >= FeedAlgorithmConfig.UNDERSTAND_MIN_REVIEW_COUNT) return CognitiveLevel.UNDERSTAND
        return CognitiveLevel.RECALL
    }

    private fun QuestionType.isFeedCompatible(): Boolean =
        this == QuestionType.TRUE_FALSE || this == QuestionType.MCQ

    private fun QuestionSource.isMinistry(): Boolean =
        this == QuestionSource.MINISTRY_FINAL || this == QuestionSource.MINISTRY_SEMIFINAL
}

// ── CognitiveLevel ordering ───────────────────────────────────────────────────
// Kotlin enums are comparable by ordinal (declaration order = difficulty order).
// Ensure the enum is declared: RECALL, UNDERSTAND, APPLY, ANALYZE — which matches
// the existing declaration in Question.kt.
private operator fun CognitiveLevel.compareTo(other: CognitiveLevel): Int =
    this.ordinal.compareTo(other.ordinal)