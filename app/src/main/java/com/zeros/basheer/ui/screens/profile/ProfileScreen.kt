package com.zeros.basheer.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.ui.screens.profile.components.*

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Hero ──────────────────────────────────────────────────────────────
        item(key = "hero") {
            ProfileHeroHeader(
                profile      = state.profile,
                xpSummary    = state.xpSummary,
                currentStreak = state.currentStreak,
                onEditClick  = onEditProfile
            )
        }

        // ── Stats ─────────────────────────────────────────────────────────────
        item(key = "stats") {
            Spacer(Modifier.height(ProfileMetrics.sectionSpacing))
            ProfileStatsSection(
                currentStreak  = state.currentStreak,
                longestStreak  = state.longestStreak,
                totalLessons   = state.totalLessons,
                totalCards     = state.totalCards,
                totalQuestions = state.totalQuestions,
                totalMinutes   = state.totalMinutes,
                modifier = Modifier.padding(horizontal = ProfileMetrics.screenPadding)
            )
        }

        // ── XP Progress ───────────────────────────────────────────────────────
        state.xpSummary?.let { xp ->
            item(key = "xp") {
                Spacer(Modifier.height(ProfileMetrics.cardSpacing))
                XpProgressCard(
                    xpSummary = xp,
                    modifier  = Modifier.padding(horizontal = ProfileMetrics.screenPadding)
                )
            }
        }

        // ── Activity Calendar ─────────────────────────────────────────────────
        item(key = "calendar") {
            Spacer(Modifier.height(ProfileMetrics.cardSpacing))
            ActivityCalendarCard(
                recentActivity = state.recentActivity,
                currentStreak  = state.currentStreak,
                modifier       = Modifier.padding(horizontal = ProfileMetrics.screenPadding)
            )
        }

        // ── Quick Links ───────────────────────────────────────────────────────
        item(key = "links") {
            Spacer(Modifier.height(ProfileMetrics.cardSpacing))
            ProfileQuickLinks(
                onEditProfile  = onEditProfile,
                onOpenSettings = onOpenSettings,
                modifier       = Modifier.padding(horizontal = ProfileMetrics.screenPadding)
            )
        }

        // ── App Info Footer ───────────────────────────────────────────────────
        item(key = "footer") {
            Spacer(Modifier.height(ProfileMetrics.cardSpacing))
            AppInfoFooter(
                modifier = Modifier.padding(horizontal = ProfileMetrics.screenPadding)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FOOTER — lives here since it's screen-local, not reused elsewhere
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppInfoFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "بشير",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "الإصدار 0.1.0 — للشهادة السودانية",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}