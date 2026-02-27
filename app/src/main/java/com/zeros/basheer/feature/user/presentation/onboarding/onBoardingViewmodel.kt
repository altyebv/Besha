package com.zeros.basheer.feature.user.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.user.domain.model.UserProfile
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import com.zeros.basheer.feature.user.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// CONSENT added as the first real step after WELCOME
enum class OnboardingStep { WELCOME, CONSENT, IDENTITY, PATH }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val name: String = "",
    val nameError: String? = null,
    val selectedPath: StudentPath? = null,
    val isSaving: Boolean = false
)

sealed interface OnboardingEvent {
    data object NavigateToMain : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: UserProfileRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events = _events.asSharedFlow()

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, nameError = null) }
    }

    fun onPathSelected(path: StudentPath) {
        _state.update { it.copy(selectedPath = path) }
    }

    fun onNextFromWelcome() {
        _state.update { it.copy(step = OnboardingStep.CONSENT) }
    }

    fun onConsentAccepted() {
        viewModelScope.launch {
            preferencesRepository.setAnalyticsConsent(true)
            _state.update { it.copy(step = OnboardingStep.IDENTITY) }
        }
    }

    fun onConsentDeclined() {
        viewModelScope.launch {
            preferencesRepository.setAnalyticsConsent(false)
            _state.update { it.copy(step = OnboardingStep.IDENTITY) }
        }
    }

    fun onNextFromIdentity() {
        val name = _state.value.name.trim()
        if (name.isBlank()) {
            _state.update { it.copy(nameError = "الرجاء إدخال اسمك") }
            return
        }
        if (name.length < 2) {
            _state.update { it.copy(nameError = "الاسم قصير جداً") }
            return
        }
        _state.update { it.copy(step = OnboardingStep.PATH) }
    }

    fun onBack() {
        _state.update {
            it.copy(
                step = when (it.step) {
                    OnboardingStep.PATH     -> OnboardingStep.IDENTITY
                    OnboardingStep.IDENTITY -> OnboardingStep.CONSENT
                    OnboardingStep.CONSENT  -> OnboardingStep.WELCOME
                    OnboardingStep.WELCOME  -> OnboardingStep.WELCOME
                }
            )
        }
    }

    fun onCompleteOnboarding() {
        val state = _state.value
        val path = state.selectedPath ?: return
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            profileRepository.saveProfile(
                UserProfile(
                    name = state.name.trim(),
                    studentPath = path
                )
            )
            preferencesRepository.setOnboardingComplete(true)
            _state.update { it.copy(isSaving = false) }
            _events.emit(OnboardingEvent.NavigateToMain)
        }
    }
}
