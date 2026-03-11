package com.zeros.basheer.ui.screens.main.components.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
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
    listState: LazyListState,
    onSubjectClick: (String) -> Unit,
    onRecommendationAction: (ScoredRecommendation) -> Unit,
    onDismissFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Measured height of the floating header in pixels — updated via onSizeChanged.
    // Initialised to 0; the LazyColumn will recompose once with the real value
    // after the first layout pass (typically one frame).
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp by remember(headerHeightPx) {
        derivedStateOf { with(density) { headerHeightPx.toDp() } }
    }

    // True as soon as the user has scrolled any content under the header —
    // drives the shadow animation in HomeHeader.
    val hasScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        // ── Scrollable content — top padding reserves space for the header ──
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                // Use the measured header height; fall back to a safe estimate
                // (status bar + ~72 dp) on the very first frame before measurement.
                top    = max(headerHeightDp, MainMetrics.verticalPadding),
                bottom = MainMetrics.verticalPadding,
                start  = MainMetrics.contentPadding,
                end    = MainMetrics.contentPadding
            ),
            verticalArrangement = Arrangement.spacedBy(MainMetrics.cardSpacing)
        ) {
            // ── Zone 2: Daily goal bar ──────────────────────────────────────
            // (Zone 1 / HomeHeader is now the floating overlay below)
            if (state.isDailyActivityLoaded) {
                item(key = "daily_goal") {
                    DailyGoalBar(
                        todayActivity = state.todayActivity,
                        dailyGoal = 3
                    )
                }
            }

            // ── Zone 3: Mission card ────────────────────────────────────────
            if (state.isRecommendationLoaded && !state.focusCardDismissed) {
                item(key = "mission_card") {
                    MissionCard(
                        recommendation = state.topRecommendation,
                        onActionClick = onRecommendationAction,
                        onDismiss = onDismissFocus
                    )
                }
            }

            // ── Zone 4: Subject cards ───────────────────────────────────────
            subjectsSection(
                subjects = state.subjects,
                onSubjectClick = onSubjectClick
            )

            // Bottom breathing room for nav bar
            item { Spacer(Modifier.height(24.dp)) }
        }

        // ── Floating sticky header overlay ───────────────────────────────────
        // Lives outside the LazyColumn so the list viewport never changes size
        // when it appears. onSizeChanged feeds back the rendered height so the
        // LazyColumn's top contentPadding always matches precisely.
        HomeHeader(
            userName         = state.userName,
            streakDays       = state.streakStatus.currentStreak,
            streakLevel      = state.streakStatus.todayLevel,
            overallProgress  = state.overallProgress,
            completedLessons = state.completedLessonsCount,
            totalLessons     = state.totalLessonsCount,
            xpSummary        = state.xpSummary,
            hasScrolled      = hasScrolled,
            modifier         = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { headerHeightPx = it.height }
        )
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