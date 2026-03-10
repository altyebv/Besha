package com.zeros.basheer.feature.user.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.feature.user.notifications.rememberNotificationPermission
import com.zeros.basheer.feature.user.presentation.onboarding.components.ConsentStep
import com.zeros.basheer.feature.user.presentation.onboarding.components.GoalsStep
import com.zeros.basheer.feature.user.presentation.onboarding.components.IdentityStep
import com.zeros.basheer.feature.user.presentation.onboarding.components.LocationStep
import com.zeros.basheer.feature.user.presentation.onboarding.components.PathStep
import com.zeros.basheer.feature.user.presentation.onboarding.components.PreferencesStep
import com.zeros.basheer.feature.user.presentation.onboarding.components.WelcomeStep
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is OnboardingEvent.NavigateToMain -> onOnboardingComplete()
            }
        }
    }

    BackHandler(enabled = state.step != OnboardingStep.WELCOME) {
        viewModel.onBack()
    }

    // ── Notification permission ───────────────────────────────────────────────
    // Wires the POST_NOTIFICATIONS system dialog to the reminder toggle in
    // PreferencesStep. When the user flips the switch on:
    //   API 33+ → request permission dialog → if granted, enable in ViewModel
    //   API < 33 → no dialog needed → enable directly
    // Denial is handled gracefully: the toggle reverts to OFF via the ViewModel.
    val notifPermission = rememberNotificationPermission(
        onGranted = { viewModel.onReminderEnabledChanged(true) },
        onDenied  = { viewModel.onReminderEnabledChanged(false) },
    )

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val progressSteps = listOf(
                OnboardingStep.IDENTITY, OnboardingStep.LOCATION,
                OnboardingStep.PATH, OnboardingStep.GOALS, OnboardingStep.PREFERENCES
            )
            if (state.step in progressSteps) {
                StepIndicator(
                    currentStep = state.step,
                    steps       = progressSteps,
                    modifier    = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                )
            }

            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    if (forward) {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    } else {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    }
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    OnboardingStep.WELCOME -> WelcomeStep(
                        onNext = viewModel::onNextFromWelcome
                    )
                    OnboardingStep.IDENTITY -> IdentityStep(
                        name            = state.name,
                        nameError       = state.nameError,
                        email           = state.email,
                        emailError      = state.emailError,
                        age             = state.age,
                        gender          = state.gender,
                        onNameChange    = viewModel::onNameChange,
                        onEmailChange   = viewModel::onEmailChange,
                        onAgeSelected   = viewModel::onAgeSelected,
                        onGenderSelected = viewModel::onGenderSelected,
                        onNext          = viewModel::onNextFromIdentity,
                        onBack          = viewModel::onBack
                    )
                    OnboardingStep.LOCATION -> LocationStep(
                        selectedState       = state.state,
                        city                = state.city,
                        schoolName          = state.schoolName,
                        schoolNameError     = state.schoolNameError,
                        isHomeSchooled      = state.isHomeSchooled,
                        selectedGrade       = state.grade,
                        onStateSelected     = viewModel::onStateSelected,
                        onCityChange        = viewModel::onCityChange,
                        onSchoolNameChange  = viewModel::onSchoolNameChange,
                        onHomeSchooledChanged = viewModel::onHomeSchooledChanged,
                        onGradeSelected     = viewModel::onGradeSelected,
                        onNext              = viewModel::onNextFromLocation,
                        onBack              = viewModel::onBack
                    )
                    OnboardingStep.PATH -> PathStep(
                        name            = state.name,
                        selectedPath    = state.selectedPath,
                        selectedTrack   = state.academicTrack,
                        isSaving        = false,
                        onPathSelected  = viewModel::onPathSelected,
                        onTrackSelected = viewModel::onAcademicTrackSelected,
                        onComplete      = viewModel::onNextFromPath,
                        onBack          = viewModel::onBack
                    )
                    OnboardingStep.GOALS -> GoalsStep(
                        selectedMajor  = state.major,
                        onMajorSelected = viewModel::onMajorSelected,
                        onNext         = viewModel::onNextFromGoals,
                        onBack         = viewModel::onBack
                    )
                    OnboardingStep.PREFERENCES -> PreferencesStep(
                        dailyStudyMinutes        = state.dailyStudyMinutes,
                        reminderEnabled          = state.reminderEnabled,
                        reminderHour             = state.reminderHour,
                        reminderMinute           = state.reminderMinute,
                        onDailyStudyMinutesChanged = viewModel::onDailyStudyMinutesChanged,
                        onReminderEnabledChanged = { enabled ->
                            if (enabled) {
                                // Always go through permission helper when enabling:
                                // it requests POST_NOTIFICATIONS on API 33+ or calls
                                // onGranted directly on lower API levels.
                                notifPermission.request()
                            } else {
                                viewModel.onReminderEnabledChanged(false)
                            }
                        },
                        onReminderTimeChanged    = viewModel::onReminderTimeChanged,
                        onNext                   = viewModel::onNextFromPreferences,
                        onBack                   = viewModel::onBack
                    )
                    OnboardingStep.CONSENT -> ConsentStep(
                        isSaving       = state.isSaving,
                        onConsentChosen = viewModel::onConsentChosen
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: OnboardingStep,
    steps: List<OnboardingStep>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        steps.forEach { step ->
            val isActive = step.ordinal <= currentStep.ordinal
            LinearProgressIndicator(
                progress = { if (isActive) 1f else 0f },
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
                color      = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}