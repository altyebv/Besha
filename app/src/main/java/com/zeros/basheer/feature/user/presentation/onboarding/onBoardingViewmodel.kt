package com.zeros.basheer.feature.user.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeros.basheer.feature.analytics.AnalyticsManager
import com.zeros.basheer.feature.analytics.domain.model.AnalyticsConsent
import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.user.domain.model.UserProfile
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import com.zeros.basheer.feature.user.domain.repository.UserProfileRepository
import com.zeros.basheer.feature.user.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// New flow: WELCOME → IDENTITY → LOCATION → PATH → GOALS → PREFERENCES → CONSENT
enum class OnboardingStep { WELCOME, IDENTITY, LOCATION, PATH, GOALS, PREFERENCES, CONSENT }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    // IDENTITY
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    // LOCATION
    val state: String? = null,
    val city: String = "",
    val schoolName: String = "",
    // PATH
    val selectedPath: StudentPath? = null,
    val academicTrack: String? = null,
    // GOALS
    val major: String? = null,
    // PREFERENCES
    val dailyStudyMinutes: Int = 60,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    // meta
    val isSaving: Boolean = false
)

sealed interface OnboardingEvent {
    data object NavigateToMain : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: UserProfileRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val analytics: AnalyticsManager,
    // Injected so we can schedule the alarm immediately after onboarding completes,
    // rather than waiting for the next MainActivity.onCreate() call.
    private val scheduler: ReminderScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events = _events.asSharedFlow()

    private val onboardingStartMs = System.currentTimeMillis()

    // ── Field updates ─────────────────────────────────────────────────────────

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, nameError = null) }
    }

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, emailError = null) }
    }

    fun onStateSelected(value: String) {
        _state.update { it.copy(state = value) }
    }

    fun onCityChange(value: String) {
        _state.update { it.copy(city = value) }
    }

    fun onSchoolNameChange(value: String) {
        _state.update { it.copy(schoolName = value) }
    }

    fun onPathSelected(path: StudentPath) {
        _state.update { it.copy(selectedPath = path, academicTrack = null) }
    }

    fun onAcademicTrackSelected(track: String) {
        _state.update { it.copy(academicTrack = track) }
    }

    fun onMajorSelected(major: String?) {
        _state.update { it.copy(major = major) }
    }

    fun onDailyStudyMinutesChanged(minutes: Int) {
        _state.update { it.copy(dailyStudyMinutes = minutes) }
    }

    fun onReminderEnabledChanged(enabled: Boolean) {
        _state.update { it.copy(reminderEnabled = enabled) }
    }

    fun onReminderTimeChanged(hour: Int, minute: Int) {
        _state.update { it.copy(reminderHour = hour, reminderMinute = minute) }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    fun onNextFromWelcome()      { _state.update { it.copy(step = OnboardingStep.IDENTITY) } }

    fun onNextFromIdentity() {
        val name = _state.value.name.trim()
        if (name.isBlank()) { _state.update { it.copy(nameError = "الرجاء إدخال اسمك") }; return }
        if (name.length < 2) { _state.update { it.copy(nameError = "الاسم قصير جداً") }; return }
        val email = _state.value.email.trim()
        if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(emailError = "البريد الإلكتروني غير صحيح") }; return
        }
        _state.update { it.copy(step = OnboardingStep.LOCATION) }
    }

    fun onNextFromLocation()     { _state.update { it.copy(step = OnboardingStep.PATH) } }

    fun onNextFromPath() {
        val s = _state.value
        if (s.selectedPath == null || s.academicTrack == null) return
        _state.update { it.copy(step = OnboardingStep.GOALS) }
    }

    fun onNextFromGoals()        { _state.update { it.copy(step = OnboardingStep.PREFERENCES) } }
    fun onNextFromPreferences()  { _state.update { it.copy(step = OnboardingStep.CONSENT) } }

    fun onConsentChosen(consent: AnalyticsConsent) {
        viewModelScope.launch {
            preferencesRepository.setAnalyticsConsent(consent)
            completeOnboarding(consent)
        }
    }

    fun onBack() {
        _state.update {
            it.copy(
                step = when (it.step) {
                    OnboardingStep.IDENTITY    -> OnboardingStep.WELCOME
                    OnboardingStep.LOCATION    -> OnboardingStep.IDENTITY
                    OnboardingStep.PATH        -> OnboardingStep.LOCATION
                    OnboardingStep.GOALS       -> OnboardingStep.PATH
                    OnboardingStep.PREFERENCES -> OnboardingStep.GOALS
                    OnboardingStep.CONSENT     -> OnboardingStep.PREFERENCES
                    OnboardingStep.WELCOME     -> OnboardingStep.WELCOME
                }
            )
        }
    }

    // ── Completion ────────────────────────────────────────────────────────────

    private fun completeOnboarding(consent: AnalyticsConsent) {
        val s = _state.value
        val path = s.selectedPath ?: return
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            profileRepository.saveProfile(
                UserProfile(
                    name             = s.name.trim(),
                    studentPath      = path,
                    schoolName       = s.schoolName.trim().ifBlank { null },
                    email            = s.email.trim().ifBlank { null },
                    state            = s.state,
                    city             = s.city.trim().ifBlank { null },
                    major            = s.major,
                    academicTrack    = s.academicTrack,
                    dailyStudyMinutes = s.dailyStudyMinutes
                )
            )
            preferencesRepository.setDailyGoalMinutes(s.dailyStudyMinutes)

            if (s.reminderEnabled) {
                preferencesRepository.setNotificationsEnabled(true)
                preferencesRepository.setReminderHour(s.reminderHour)
                preferencesRepository.setReminderMinute(s.reminderMinute)
                // ── Schedule the alarm NOW so it fires without requiring an app restart ──
                // Without this call, the alarm only activates on the next cold launch
                // (MainActivity.rescheduleIfEnabled), meaning first-time users would
                // miss their first reminder if they don't restart the app.
                scheduler.schedule(s.reminderHour, s.reminderMinute)
            } else {
                preferencesRepository.setNotificationsEnabled(false)
            }

            preferencesRepository.setOnboardingComplete(true)
            _state.update { it.copy(isSaving = false) }
            _events.emit(OnboardingEvent.NavigateToMain)
        }

        analytics.onboardingCompleted(
            studentPath       = path.name,
            hasSchoolName     = s.schoolName.isNotBlank(),
            hasEmail          = s.email.isNotBlank(),
            state             = s.state,
            major             = s.major,
            dailyStudyMinutes = s.dailyStudyMinutes,
            reminderEnabled   = s.reminderEnabled,
            consentTier       = consent.name,
            durationSeconds   = ((System.currentTimeMillis() - onboardingStartMs) / 1000).toInt()
        )
    }
}