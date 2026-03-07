package com.zeros.basheer.feature.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.analytics.AnalyticsManager
import com.zeros.basheer.feature.analytics.ErrorTracker
import com.zeros.basheer.feature.analytics.domain.model.FeedInteraction
import com.zeros.basheer.feature.feed.domain.model.CardInteractionState
import com.zeros.basheer.feature.feed.domain.model.FeedCard
import com.zeros.basheer.feature.feed.domain.model.FeedItemType
import com.zeros.basheer.feature.feed.domain.model.InteractionType
import com.zeros.basheer.feature.feed.domain.usecase.BuildFeedSessionUseCase
import com.zeros.basheer.feature.feed.domain.usecase.FeedSessionResult
import com.zeros.basheer.feature.concept.domain.model.Rating
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import com.zeros.basheer.feature.streak.domain.usecase.RecordCardsReviewedUseCase
import com.zeros.basheer.feature.user.domain.model.XpSource
import com.zeros.basheer.feature.user.domain.usecase.AwardXpUseCase
import com.zeros.basheer.feature.quizbank.domain.model.QuestionStats
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FeedEmptyReason { NO_SUBJECTS, NO_CONTENT }

data class FeedsState(
    val feedCards: List<FeedCard> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val emptyReason: FeedEmptyReason? = null,

    // Interaction state for current card
    val cardInteractionState: CardInteractionState = CardInteractionState.Idle,

    // Session stats
    val cardsViewed: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val sessionComplete: Boolean = false,

    // Tracks when the current card became visible — used for per-card time measurement
    val cardShownAtMs: Long = System.currentTimeMillis(),
)

@HiltViewModel
class FeedsViewModel @Inject constructor(
    private val buildFeedSession: BuildFeedSessionUseCase,
    private val conceptRepository: ConceptRepository,
    private val quizBankRepository: QuizBankRepository,
    private val recordCardsReviewedUseCase: RecordCardsReviewedUseCase,
    private val awardXpUseCase: AwardXpUseCase,
    private val analyticsManager: AnalyticsManager,
    private val errorTracker: ErrorTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedsState())
    val state: StateFlow<FeedsState> = _state.asStateFlow()

    init {
        loadFeedSession()
    }

    private fun loadFeedSession() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, emptyReason = null) }
            when (val result = buildFeedSession()) {
                is FeedSessionResult.Success    ->
                    _state.update { it.copy(feedCards = result.cards, isLoading = false, cardShownAtMs = System.currentTimeMillis()) }
                is FeedSessionResult.NoSubjects ->
                    _state.update { it.copy(feedCards = emptyList(), isLoading = false, emptyReason = FeedEmptyReason.NO_SUBJECTS) }
                is FeedSessionResult.NoContent  ->
                    _state.update { it.copy(feedCards = emptyList(), isLoading = false, emptyReason = FeedEmptyReason.NO_CONTENT) }
            }
        }
    }

    fun onPageChanged(index: Int) {
        _state.update {
            it.copy(
                currentIndex = index,
                cardInteractionState = CardInteractionState.Idle,
                cardShownAtMs = System.currentTimeMillis()
            )
        }
    }

    fun onAnswer(answer: String) {
        val currentCard = _state.value.feedCards.getOrNull(_state.value.currentIndex) ?: return
        val isCorrect   = answer.equals(currentCard.correctAnswer, ignoreCase = true)
        val timeSpent   = ((System.currentTimeMillis() - _state.value.cardShownAtMs) / 1000).toInt()

        _state.update {
            it.copy(
                cardInteractionState = CardInteractionState.Answered(
                    userAnswer  = answer,
                    isCorrect   = isCorrect,
                    explanation = currentCard.explanation
                ),
                correctAnswers = if (isCorrect) it.correctAnswers + 1 else it.correctAnswers,
                wrongAnswers   = if (!isCorrect) it.wrongAnswers + 1 else it.wrongAnswers
            )
        }

        viewModelScope.launch {
            val rating = if (isCorrect) Rating.GOOD else Rating.HARD
            conceptRepository.recordReview(currentCard.conceptId, rating)

            // Track feed exposure on quiz-bank questions
            if (currentCard.type == FeedItemType.MINI_QUIZ) {
                recordQuizFeedExposure(currentCard.id)
            }

            if (isCorrect) awardXpUseCase(XpSource.CARD_CORRECT)

            // ── Error tracking ────────────────────────────────────────────────
            // Only record quiz-type cards — definition/formula cards don't have
            // a correct/wrong answer so they're not meaningful error signals.
            if (currentCard.type == FeedItemType.MINI_QUIZ && currentCard.correctAnswer != null) {
                val questionId = currentCard.id.removePrefix("quiz_")
                val srInterval = conceptRepository.getReviewByConcept(currentCard.conceptId)
                    ?.intervalDays ?: 1

                errorTracker.feedQuestionAnswered(
                    questionId            = questionId,
                    feedCardId            = currentCard.id,
                    subjectId             = currentCard.subjectId,
                    conceptId             = currentCard.conceptId,
                    questionType          = currentCard.interactionType?.name ?: "MCQ",
                    userAnswer            = answer,
                    correctAnswer         = currentCard.correctAnswer,
                    isCorrect             = isCorrect,
                    timeSpentSeconds      = timeSpent,
                    cardPositionInSession = _state.value.currentIndex,
                    srIntervalDaysBefore  = srInterval,
                )
            }
            // ─────────────────────────────────────────────────────────────────
        }
    }

    fun onFlip() {
        _state.update { it.copy(cardInteractionState = CardInteractionState.Flipped) }
    }

    fun onSelfRate(knew: Boolean) {
        val currentCard = _state.value.feedCards.getOrNull(_state.value.currentIndex) ?: return
        val timeSpent   = ((System.currentTimeMillis() - _state.value.cardShownAtMs) / 1000).toInt()

        _state.update {
            it.copy(
                cardInteractionState = CardInteractionState.Answered(
                    userAnswer  = if (knew) "knew" else "didnt_know",
                    isCorrect   = knew,
                    explanation = null
                ),
                correctAnswers = if (knew) it.correctAnswers + 1 else it.correctAnswers,
                wrongAnswers   = if (!knew) it.wrongAnswers + 1 else it.wrongAnswers
            )
        }

        viewModelScope.launch {
            val rating = if (knew) Rating.GOOD else Rating.HARD
            conceptRepository.recordReview(currentCard.conceptId, rating)
            if (knew) awardXpUseCase(XpSource.CARD_CORRECT)

            // ── Error tracking — flash card self-rate ─────────────────────────
            // FLASH_CARD self-rating is a valid learning signal: "didn't know"
            // on a flash card is conceptually equivalent to a wrong answer.
            // We use the conceptId as the questionId since flash cards don't
            // originate from the quiz bank.
            if (currentCard.type == FeedItemType.FLASH_CARD) {
                val srInterval = conceptRepository.getReviewByConcept(currentCard.conceptId)
                    ?.intervalDays ?: 1

                errorTracker.feedQuestionAnswered(
                    questionId            = currentCard.conceptId,  // concept as surrogate id
                    feedCardId            = currentCard.id,
                    subjectId             = currentCard.subjectId,
                    conceptId             = currentCard.conceptId,
                    questionType          = "FLASH_CARD_SELF_RATE",
                    userAnswer            = if (knew) "knew" else "didnt_know",
                    correctAnswer         = "knew",                 // "knew" is always the target
                    isCorrect             = knew,
                    timeSpentSeconds      = timeSpent,
                    cardPositionInSession = _state.value.currentIndex,
                    srIntervalDaysBefore  = srInterval,
                )
            }
            analyticsManager.feedCardInteracted(
                cardId      = currentCard.id,
                subjectId   = currentCard.subjectId,
                cardType    = currentCard.type.name,
                interaction = if (knew) FeedInteraction.ANSWERED_CORRECT
                else      FeedInteraction.ANSWERED_WRONG,
                wasCorrect  = knew,
            )
            // ─────────────────────────────────────────────────────────────────
        }
    }

    fun onContinue(): Boolean {
        val currentIndex = _state.value.currentIndex
        val totalCards   = _state.value.feedCards.size
        val currentCard  = _state.value.feedCards.getOrNull(currentIndex)

        _state.update { it.copy(cardsViewed = it.cardsViewed + 1) }

        viewModelScope.launch {
            recordCardsReviewedUseCase(1)
            awardXpUseCase(XpSource.CARD_REVIEWED)
        }
        if (currentCard != null &&
            _state.value.cardInteractionState is CardInteractionState.Idle) {
            analyticsManager.feedCardInteracted(
                cardId      = currentCard.id,
                subjectId   = currentCard.subjectId,
                cardType    = currentCard.type.name,
                interaction = FeedInteraction.REVIEWED,
                wasCorrect  = null,
            )
        }

        if (currentIndex >= totalCards - 1) {
            _state.update { it.copy(sessionComplete = true) }
            return false
        }

        _state.update { it.copy(cardInteractionState = CardInteractionState.Idle) }
        return true
    }

    fun canSwipeToNext(): Boolean {
        val currentCard      = _state.value.feedCards.getOrNull(_state.value.currentIndex) ?: return true
        val interactionState = _state.value.cardInteractionState

        if (currentCard.interactionType == null ||
            currentCard.interactionType == InteractionType.TAP_CONFIRM) return true

        return interactionState is CardInteractionState.Answered
    }

    fun restartSession() {
        _state.update {
            it.copy(
                currentIndex         = 0,
                cardsViewed          = 0,
                correctAnswers       = 0,
                wrongAnswers         = 0,
                sessionComplete      = false,
                emptyReason          = null,
                cardInteractionState = CardInteractionState.Idle,
                cardShownAtMs        = System.currentTimeMillis()
            )
        }
        loadFeedSession()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Updates [QuestionStats.lastShownInFeed] and [QuestionStats.feedShowCount]
     * after a quiz-bank card is shown in the feed.
     *
     * Card ids for quiz-bank questions are prefixed with "quiz_" by
     * [ConceptGroupBuilder] — strip the prefix to recover the real question id.
     */
    private suspend fun recordQuizFeedExposure(cardId: String) {
        val questionId = cardId.removePrefix("quiz_")
        val existing   = quizBankRepository.getStatsForQuestion(questionId)
            ?: QuestionStats.forNewQuestion(questionId)

        quizBankRepository.updateStats(
            existing.copy(
                lastShownInFeed = System.currentTimeMillis(),
                feedShowCount   = existing.feedShowCount + 1,
                updatedAt       = System.currentTimeMillis()
            )
        )
    }
}