package com.zeros.basheer.ui.screens.main.components.sections


import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zeros.basheer.domain.model.ScoredRecommendation
import com.zeros.basheer.ui.components.common.SectionHeader
import com.zeros.basheer.ui.screens.main.MainScreenState
import com.zeros.basheer.ui.screens.main.components.cards.OverallStatsBanner
import com.zeros.basheer.ui.screens.main.components.cards.SubjectCard
import com.zeros.basheer.ui.screens.main.components.cards.TodayFocusCard
import com.zeros.basheer.ui.screens.main.components.foundation.MainAnimations
import com.zeros.basheer.ui.screens.main.components.foundation.MainMetrics

/**
 * Main dashboard content with scrollable sections.
 *
 * Sections:
 * - Overall stats banner
 * - Today's focus card (if available)
 * - Subjects grid
 *
 * @param state Main screen state
 * @param onSubjectClick Callback when subject clicked
 * @param onRecommendationAction Callback when recommendation action clicked
 * @param onDismissFocus Callback when focus card dismissed
 * @param modifier Standard modifier
 */
@Composable
fun MainDashboardContent(
    state: MainScreenState,
    onSubjectClick: (String) -> Unit,
    onRecommendationAction: (ScoredRecommendation) -> Unit,
    onDismissFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = rememberLazyListState(),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = MainMetrics.contentPadding,
            vertical = MainMetrics.verticalPadding
        ),
        verticalArrangement = Arrangement.spacedBy(MainMetrics.cardSpacing)
    ) {
        // Overall stats banner
        item(key = "stats_banner") {
            AnimatedContent(
                targetState = state.isLoading,
                transitionSpec = { MainAnimations.bannerTransition },
                label = "stats_banner"
            ) { loading ->
                if (!loading) {
                    OverallStatsBanner(
                        userName = state.userName,
                        streakDays = state.streakStatus.currentStreak,
                        streakLevel = state.streakStatus.todayLevel,
                        overallProgress = state.overallProgress,
                        completedLessons = state.completedLessonsCount,
                        totalLessons = state.totalLessonsCount,
                        xpSummary = state.xpSummary
                    )
                }
            }
        }

        // Today's focus card
        if (state.topRecommendation != null && !state.focusCardDismissed) {
            item(key = "focus_card") {
                TodayFocusCard(
                    recommendation = state.topRecommendation!!,
                    onActionClick = onRecommendationAction,
                    onDismiss = onDismissFocus
                )
            }
        }

        // Subjects section
        subjectsSection(
            subjects = state.subjects,
            onSubjectClick = onSubjectClick
        )

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Subjects section within LazyColumn
 */
private fun LazyListScope.subjectsSection(
    subjects: List<com.zeros.basheer.ui.screens.main.SubjectWithProgress>,
    onSubjectClick: (String) -> Unit
) {
    // Section header
    item(key = "subjects_header") {
        SectionHeader(
            title = "المواد الدراسية",
            count = subjects.size
        )
    }

    // Subject cards — no delayed visibility; starting invisible and flipping to true
    // after a LaunchedEffect causes cards to briefly disappear whenever the list
    // recomposes (e.g. after recommendation scores reorder subjects).
    items(
        count = subjects.size,
        key = { index -> subjects[index].subject.id }
    ) { index ->
        val subjectWithProgress = subjects[index]
        SubjectCard(
            subjectWithProgress = subjectWithProgress,
            onClick = { onSubjectClick(subjectWithProgress.subject.id) },
            onContinueClick = { onSubjectClick(subjectWithProgress.subject.id) },
            onPracticeClick = { /* Navigate to quiz bank */ }
        )
    }
}