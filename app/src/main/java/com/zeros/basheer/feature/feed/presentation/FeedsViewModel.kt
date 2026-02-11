package com.zeros.basheer.feature.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.zeros.basheer.feature.feed.data.mapper.FeedMapper
import com.zeros.basheer.feature.feed.domain.model.CardInteractionState
import com.zeros.basheer.feature.feed.domain.model.FeedCard
import com.zeros.basheer.feature.concept.domain.model.Rating
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import com.zeros.basheer.feature.feed.domain.model.InteractionType
import com.zeros.basheer.feature.feed.domain.repository.FeedRepository
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepository
import com.zeros.basheer.feature.streak.domain.usecase.RecordCardsReviewedUseCase
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedsState(
    val feedCards: List<FeedCard> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    
    // Interaction state for current card
    val cardInteractionState: CardInteractionState = CardInteractionState.Idle,
    
    // Session stats
    val cardsViewed: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val sessionComplete: Boolean = false,
    
    // Config
    val maxCardsPerSession: Int = 30
)

@HiltViewModel
class FeedsViewModel @Inject constructor(
    private val repository: LessonRepository,
    private val subjectRepository: SubjectRepository,
    private val feedRepository: FeedRepository,
    private val conceptRepository: ConceptRepository,
    private val recordCardsReviewedUseCase: RecordCardsReviewedUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedsState())
    val state: StateFlow<FeedsState> = _state.asStateFlow()

    init {
        loadFeedItems()
    }

    private fun loadFeedItems() {
        viewModelScope.launch {
            // Get first subject for now
            subjectRepository.getAllSubjects().first().firstOrNull()?.let { subject ->
                feedRepository.getFeedItemsBySubject(subject.id).collect { feedItems ->
                    val cards = FeedMapper.toFeedCards(
                        feedItems.take(_state.value.maxCardsPerSession),
                        subject
                    )
                    
                    _state.update { it.copy(
                        feedCards = cards,
                        isLoading = false
                    )}
                }
            } ?: run {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onPageChanged(index: Int) {
        _state.update { it.copy(
            currentIndex = index,
            cardInteractionState = CardInteractionState.Idle
        )}
    }

    fun onAnswer(answer: String) {
        val currentCard = _state.value.feedCards.getOrNull(_state.value.currentIndex) ?: return
        
        val isCorrect = answer.equals(currentCard.correctAnswer, ignoreCase = true)
        
        _state.update { it.copy(
            cardInteractionState = CardInteractionState.Answered(
                userAnswer = answer,
                isCorrect = isCorrect,
                explanation = currentCard.explanation
            ),
            correctAnswers = if (isCorrect) it.correctAnswers + 1 else it.correctAnswers,
            wrongAnswers = if (!isCorrect) it.wrongAnswers + 1 else it.wrongAnswers
        )}
        
        // Record review for spaced repetition
        viewModelScope.launch {
            val rating = if (isCorrect) Rating.GOOD else Rating.HARD
            conceptRepository.recordReview(currentCard.conceptId, rating)
        }
    }

    fun onContinue(): Boolean {
        val currentIndex = _state.value.currentIndex
        val totalCards = _state.value.feedCards.size
        
        _state.update { it.copy(
            cardsViewed = it.cardsViewed + 1
        )}
        
        // Record card reviewed for streak tracking
        viewModelScope.launch {
            recordCardsReviewedUseCase(1)
        }
        
        // Check if session is complete
        if (currentIndex >= totalCards - 1) {
            _state.update { it.copy(sessionComplete = true) }
            return false // Can't move to next
        }
        
        // Reset interaction state for next card
        _state.update { it.copy(
            cardInteractionState = CardInteractionState.Idle
        )}
        
        return true // Can move to next
    }

    fun canSwipeToNext(): Boolean {
        val currentCard = _state.value.feedCards.getOrNull(_state.value.currentIndex) ?: return true
        val interactionState = _state.value.cardInteractionState
        
        // Non-interactive cards can always swipe
        if (currentCard.interactionType == null || 
            currentCard.interactionType == InteractionType.TAP_CONFIRM) {
            return true
        }
        
        // Interactive cards need to be answered first
        return interactionState is CardInteractionState.Answered
    }

    fun restartSession() {
        _state.update { it.copy(
            currentIndex = 0,
            cardsViewed = 0,
            correctAnswers = 0,
            wrongAnswers = 0,
            sessionComplete = false,
            cardInteractionState = CardInteractionState.Idle
        )}
        loadFeedItems()
    }
}
