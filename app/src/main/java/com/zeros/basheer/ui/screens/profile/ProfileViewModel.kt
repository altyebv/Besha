package com.zeros.basheer.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.streak.domain.model.DailyActivity
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import com.zeros.basheer.feature.user.domain.model.UserProfile
import com.zeros.basheer.feature.user.domain.model.XpSummary
import com.zeros.basheer.feature.user.domain.repository.UserProfileRepository
import com.zeros.basheer.feature.user.domain.usecase.GetUserXpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val profile: UserProfile? = null,
    val xpSummary: XpSummary? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalLessons: Int = 0,
    val totalCards: Int = 0,
    val totalQuestions: Int = 0,
    val totalMinutes: Long = 0,
    val recentActivity: List<DailyActivity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val streakRepository: StreakRepository,
    private val userProfileRepository: UserProfileRepository,
    private val getUserXpUseCase: GetUserXpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
        loadXp()
        loadStats()
        loadRecentActivity()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userProfileRepository.getProfile().collect { profile ->
                _state.update { it.copy(profile = profile) }
            }
        }
    }

    private fun loadXp() {
        viewModelScope.launch {
            getUserXpUseCase().collect { summary ->
                _state.update { it.copy(xpSummary = summary) }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            streakRepository.getStreakStatusFlow().collect { status ->
                _state.update {
                    it.copy(
                        currentStreak = status.currentStreak,
                        longestStreak = status.longestStreak
                    )
                }
            }
        }
        viewModelScope.launch {
            streakRepository.getTotalLessonsCompleted().collect { count ->
                _state.update { it.copy(totalLessons = count) }
            }
        }
        viewModelScope.launch {
            streakRepository.getTotalCardsReviewed().collect { count ->
                _state.update { it.copy(totalCards = count) }
            }
        }
        viewModelScope.launch {
            streakRepository.getTotalQuestionsAnswered().collect { count ->
                _state.update { it.copy(totalQuestions = count) }
            }
        }
        viewModelScope.launch {
            streakRepository.getTotalTimeSpent().collect { seconds ->
                _state.update {
                    it.copy(
                        totalMinutes = seconds / 60,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadRecentActivity() {
        viewModelScope.launch {
            streakRepository.getRecentActivity(days = 28).collect { activities ->
                _state.update { it.copy(recentActivity = activities) }
            }
        }
    }
}