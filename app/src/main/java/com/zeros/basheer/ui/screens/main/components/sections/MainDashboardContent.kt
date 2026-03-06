package com.zeros.basheer.ui.screens.main.components.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.domain.model.ScoredRecommendation
import com.zeros.basheer.ui.screens.main.MainScreenState
import com.zeros.basheer.ui.screens.main.SubjectWithProgress
import com.zeros.basheer.ui.screens.main.components.cards.*
import com.zeros.basheer.ui.screens.main.components.foundation.MainMetrics

/**
 * Main dashboard content — 4 clear zones:
 *
 *  1. HomeHeader      — compact amber strip: greeting, streak, XP, overall progress
 *  2. DailyGoalBar    — "هدف اليوم" with dot progress toward 3 lessons
 *  3. MissionCard     — primary CTA: subject-colored, uses recommendation engine
 *  4. Subject cards   — full list, each with its own color identity
 *
 * The hierarchy answers three questions immediately:
 *  "Who am I?" (header) → "What's left today?" (goal bar) → "What should I do next?" (mission)
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
        // ── Zone 1: Compact header ────────────────────────────────────────
        item(key = "home_header") {
            HomeHeader(
                userName = state.userName,
                streakDays = state.streakStatus.currentStreak,
                streakLevel = state.streakStatus.todayLevel,
                overallProgress = state.overallProgress,
                completedLessons = state.completedLessonsCount,
                totalLessons = state.totalLessonsCount,
                xpSummary = state.xpSummary
            )
        }

        // ── Zone 2: Daily goal bar ────────────────────────────────────────
        // Only rendered after the first todayActivity emission — prevents a flash
        // of the "no activity" state on launch before the DB query completes.
        if (state.isDailyActivityLoaded) {
            item(key = "daily_goal") {
                DailyGoalBar(
                    todayActivity = state.todayActivity,
                    dailyGoal = 3
                )
            }
        }

        // ── Zone 3: Mission card (only after recommendation engine has resolved) ─
        if (state.isRecommendationLoaded && !state.focusCardDismissed) {
            item(key = "mission_card") {
                MissionCard(
                    recommendation = state.topRecommendation,
                    onActionClick = onRecommendationAction,
                    onDismiss = onDismissFocus
                )
            }
        }

        // ── Zone 4: Subject cards ─────────────────────────────────────────
        subjectsSection(
            subjects = state.subjects,
            onSubjectClick = onSubjectClick
        )

        // Bottom breathing room for nav bar
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Subjects section ───────────────────────────────────────────────────────────

private fun LazyListScope.subjectsSection(
    subjects: List<SubjectWithProgress>,
    onSubjectClick: (String) -> Unit
) {
    if (subjects.isEmpty()) return

    item(key = "subjects_header") {
        SubjectsHeader()
    }

    items(
        count = subjects.size,
        key = { index -> subjects[index].subject.id }
    ) { index ->
        val s = subjects[index]
        SubjectCard(
            subjectWithProgress = s,
            onClick = { onSubjectClick(s.subject.id) },
            onContinueClick = { onSubjectClick(s.subject.id) },
            onPracticeClick = { /* TODO: navigate to practice with subject filter */ },
            subjectIndex = index
        )
    }
}

@Composable
private fun SubjectsHeader() {
    Text(
        text = "استمر في دراستك",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}