package com.zeros.basheer.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zeros.basheer.domain.model.Recommendation
import com.zeros.basheer.domain.model.ScoredRecommendation
import com.zeros.basheer.feature.lesson.presentation.components.states.LoadingState
import com.zeros.basheer.ui.screens.main.components.sections.MainDashboardContent
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main dashboard screen - entry point.
 *
 * Displays:
 * - Pull-to-refresh
 * - Overall stats with streak
 * - Today's AI recommendation
 * - Subject cards
 *
 * Architecture: Clean orchestrator pattern.
 * All content delegated to MainDashboardContent component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSubjectClick: (String) -> Unit,
    navController: NavController? = null,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            isRefreshing = true
            viewModel.refreshData()
            scope.launch {
                delay(1500)
                isRefreshing = false
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            when {
                state.isLoading && !isRefreshing -> {
                    LoadingState(modifier = Modifier.padding(padding))
                }

                else -> {
                    MainDashboardContent(
                        state = state,
                        onSubjectClick = onSubjectClick,
                        onRecommendationAction = { rec ->
                            handleRecommendationAction(rec, navController, onSubjectClick)
                        },
                        onDismissFocus = { viewModel.dismissFocusCard() },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

/**
 * Handle recommendation action navigation.
 * Routes user to appropriate screen based on recommendation type.
 */
private fun handleRecommendationAction(
    rec: ScoredRecommendation,
    navController: NavController?,
    onSubjectClick: (String) -> Unit
) {
    when (val r = rec.recommendation) {
        is Recommendation.ContinueLesson -> {
            navController?.navigate("lesson/${r.lessonId}")
        }
        is Recommendation.QuickReview -> {
            navController?.navigate("quiz_bank")
        }
        is Recommendation.ReviewWeakConcept -> {
            navController?.navigate("practice/${rec.subject.id}/${r.conceptId}")
        }
        is Recommendation.CompleteUnit -> {
            onSubjectClick(rec.subject.id)
        }
        is Recommendation.StartNewUnit -> {
            onSubjectClick(rec.subject.id)
        }
        is Recommendation.StreakAtRisk -> {
            onSubjectClick(rec.subject.id)
        }
    }
}