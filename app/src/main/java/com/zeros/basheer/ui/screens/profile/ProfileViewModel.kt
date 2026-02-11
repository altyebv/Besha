package com.zeros.basheer.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.data.repository.LessonRepository
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalLessons: Int = 0,
    val totalCards: Int = 0,
    val totalQuestions: Int = 0,
    val totalMinutes: Long = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: LessonRepository,
    private val streakRepository: StreakRepository,
) : ViewModel() {
    
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    
    init {
        loadStats()
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            // Observe streak status
            streakRepository.getStreakStatusFlow().collect { status ->
                _state.update { it.copy(
                    currentStreak = status.currentStreak,
                    longestStreak = status.longestStreak
                )}
            }
        }
        
        viewModelScope.launch {
            // Total lessons completed
            streakRepository.getTotalLessonsCompleted().collect { count ->
                _state.update { it.copy(totalLessons = count) }
            }
        }
        
        viewModelScope.launch {
            // Total cards reviewed
            streakRepository.getTotalCardsReviewed().collect { count ->
                _state.update { it.copy(totalCards = count) }
            }
        }
        
        viewModelScope.launch {
            // Total questions answered
            streakRepository.getTotalQuestionsAnswered().collect { count ->
                _state.update { it.copy(totalQuestions = count) }
            }
        }
        
        viewModelScope.launch {
            // Total time spent (convert to minutes)
            streakRepository.getTotalTimeSpent().collect { seconds ->
                _state.update { it.copy(
                    totalMinutes = seconds / 60,
                    isLoading = false
                )}
            }
        }
    }
}
