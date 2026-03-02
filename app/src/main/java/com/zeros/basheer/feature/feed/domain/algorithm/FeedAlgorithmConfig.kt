package com.zeros.basheer.feature.feed.domain.algorithm

/**
 * Single source of truth for all feed algorithm constants.
 * Edit here to tune behaviour without touching logic.
 */
object FeedAlgorithmConfig {

    // ── Session sizing ────────────────────────────────────────────────────────
    // Base session: 30 cards of high-priority material (review + learned + quizzes).
    // If the user has a backlog of unseen concepts above the threshold, the session
    // extends by up to MAX_EXTENSION_SIZE to ease them in gradually.
    // Hard ceiling is BASE_SESSION_SIZE + MAX_EXTENSION_SIZE.
    const val BASE_SESSION_SIZE    = 30
    const val MAX_EXTENSION_SIZE   = 10   // up to 40 cards total
    // Minimum pending new items before we start extending.
    const val EXTENSION_THRESHOLD  = 15

    // ── Anchor vs maintenance split (relative to BASE_SESSION_SIZE) ──────────
    // Anchor subject gets the bulk; maintenance subjects only surface overdue SR.
    const val ANCHOR_CARD_TARGET      = 21   // ~70 % of base 30
    const val MAINTENANCE_CARD_TARGET = 9    // ~30 % of base 30

    // How often a maintenance card is inserted into the anchor stream.
    // e.g. 3 → [A][A][A][M][A][A][A][M]…
    const val MAINTENANCE_SLOT_INTERVAL = 3

    // ── Anchor bucket sizes (must sum ≤ ANCHOR_CARD_TARGET) ──────────────────
    // Bucket A: concepts whose spaced-repetition review is due
    const val BUCKET_REVIEW_TARGET    = 6    // ~30 % of anchor
    // Bucket B: feed items tied to recently completed lessons
    const val BUCKET_LEARNED_TARGET   = 9    // ~40 % of anchor
    // Bucket C: high-priority unseen items
    const val BUCKET_DISCOVERY_TARGET = 6    // ~30 % of anchor

    // Per-subject SR fetch limits — prevents the anchor from monopolising the
    // global due-review queue and starving maintenance subjects.
    const val ANCHOR_SR_FETCH_LIMIT      = BUCKET_REVIEW_TARGET           // 6
    const val MAINTENANCE_SR_FETCH_LIMIT = 3   // per maintenance subject
    const val MAINTENANCE_SR_TOTAL_CAP   = MAINTENANCE_CARD_TARGET        // 9 total

    // ── Quiz placement ────────────────────────────────────────────────────────
    const val MAX_QUIZZES_PER_SESSION = 6

    // ── Question selection ────────────────────────────────────────────────────
    // Primary cooldown: question is hidden from feed for this many days.
    const val QUESTION_FEED_COOLDOWN_DAYS = 7L

    // Cognitive-level unlock thresholds (ConceptReview maturity).
    const val UNDERSTAND_MIN_REVIEW_COUNT = 2
    const val APPLY_MIN_EASE_FACTOR       = 2.5f
    const val ANALYZE_MIN_EASE_FACTOR     = 3.0f

    // Scoring bonuses for ranking candidate questions.
    const val SCORE_MINISTRY_SOURCE_BONUS = 2   // MINISTRY_FINAL / MINISTRY_SEMIFINAL
    const val SCORE_NEVER_IN_FEED_BONUS   = 3   // feedShowCount == 0
    const val SCORE_DIFFICULTY_WEIGHT     = 1   // × question.difficulty (1–5)
}