package com.zeros.basheer.feature.quizbank.presentation.foundation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.zeros.basheer.feature.practice.domain.model.PracticeGenerationType

// ─── Brand Colors (static — intentional identity, never theme-derived) ────────

internal val AccentExam     = Color(0xFFF59E0B)   // Amber — official, high-stakes
internal val AccentPractice = Color(0xFF0EA5E9)   // Sky Blue — dynamic, flexible

internal val ScoreHigh      = Color(0xFF10B981)   // ≥75%
internal val ScoreMid       = Color(0xFFF59E0B)   // ≥50%
internal val ScoreLow       = Color(0xFFEF4444)   // <50%

// ─── Theme-Adaptive Surface / Text Colors ─────────────────────────────────────
// Delegates to MaterialTheme.colorScheme — resolves correctly in both
// light mode (warm cream) and dark mode (warm near-black).

internal val bgBase: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background

internal val bgCard: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface

internal val bgBorder: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline

internal val textPrimary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onBackground

internal val textSecondary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

internal val textMuted: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)

// ─── Helpers ──────────────────────────────────────────────────────────────────

internal fun scoreColor(score: Float): Color = when {
    score >= 75f -> ScoreHigh
    score >= 50f -> ScoreMid
    else         -> ScoreLow
}

internal fun practiceTypeLabel(type: PracticeGenerationType): String = when (type) {
    PracticeGenerationType.FULL_EXAM    -> "امتحان كامل"
    PracticeGenerationType.BY_UNIT      -> "حسب الوحدة"
    PracticeGenerationType.BY_LESSON    -> "حسب الدرس"
    PracticeGenerationType.BY_CONCEPT   -> "حسب المفهوم"
    PracticeGenerationType.BY_PROGRESS  -> "حسب التقدم"
    PracticeGenerationType.WEAK_AREAS   -> "نقاط الضعف"
    PracticeGenerationType.QUICK_REVIEW -> "مراجعة سريعة"
    PracticeGenerationType.BY_TYPE      -> "حسب النوع"
    PracticeGenerationType.BY_SOURCE    -> "حسب المصدر"
    PracticeGenerationType.CUSTOM       -> "مخصص"
}