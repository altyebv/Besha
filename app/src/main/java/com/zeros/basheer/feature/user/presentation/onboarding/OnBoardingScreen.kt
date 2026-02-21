package com.zeros.basheer.feature.user.presentation.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.feature.user.presentation.onboarding.components.IdentityStep
import com.zeros.basheer.feature.user.presentation.onboarding.components.PathStep
import com.zeros.basheer.feature.user.presentation.onboarding.components.WelcomeStep
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Collect one-shot navigation events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is OnboardingEvent.NavigateToMain -> onOnboardingComplete()
            }
        }
    }

    // Handle system back button
    BackHandler(enabled = state.step != OnboardingStep.WELCOME) {
        viewModel.onBack()
    }

    // Progress indicator at the top (hidden on welcome)
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Step progress dots
            if (state.step != OnboardingStep.WELCOME) {
                StepIndicator(
                    currentStep = state.step,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                )
            }

            // Animated step content
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
                        name = state.name,
                        nameError = state.nameError,
                        onNameChange = viewModel::onNameChange,
                        onNext = viewModel::onNextFromIdentity,
                        onBack = viewModel::onBack
                    )
                    OnboardingStep.PATH -> PathStep(
                        name = state.name,
                        selectedPath = state.selectedPath,
                        isSaving = state.isSaving,
                        onPathSelected = viewModel::onPathSelected,
                        onComplete = viewModel::onCompleteOnboarding,
                        onBack = viewModel::onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: OnboardingStep,
    modifier: Modifier = Modifier
) {
    // Only 2 real steps to show (IDENTITY + PATH), welcome has no indicator
    val steps = listOf(OnboardingStep.IDENTITY, OnboardingStep.PATH)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        steps.forEach { step ->
            val isActive = step.ordinal <= currentStep.ordinal
            LinearProgressIndicator(
                progress = { if (isActive) 1f else 0f },
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}