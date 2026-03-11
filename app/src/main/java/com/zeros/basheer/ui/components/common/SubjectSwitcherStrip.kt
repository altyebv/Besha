package com.zeros.basheer.ui.components.common



//
// Replaces the full-screen LessonsSubjectPicker gate.
// A horizontal scrollable row of subject chips — always visible at the top
// of Lessons and QuizBank screens. Tapping a chip instantly switches the
// subject without leaving the screen.
//


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.subject.domain.model.Subject

// ─────────────────────────────────────────────────────────────────────────────
// Public API
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Horizontal scrollable strip of subject chips.
 *
 * @param subjects      The list of subjects available to this student (7 max).
 * @param activeSubjectId  Currently selected subject id, or null if none yet.
 * @param onSubjectSelect  Called when the user taps a chip.
 * @param modifier      Outer modifier — typically fillMaxWidth.
 */
@Composable
fun SubjectSwitcherStrip(
    subjects: List<Subject>,
    activeSubjectId: String?,
    onSubjectSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Auto-scroll to active chip on first composition / subject change
    val activeIndex = subjects.indexOfFirst { it.id == activeSubjectId }
    LaunchedEffect(activeSubjectId) {
        if (activeIndex > 2) {
            // Rough scroll — each chip is ~100–130dp wide
            scrollState.animateScrollTo(activeIndex * 108)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            subjects.forEach { subject ->
                SubjectChip(
                    subject = subject,
                    isActive = subject.id == activeSubjectId,
                    onClick = { onSubjectSelect(subject.id) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Internal chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SubjectChip(
    subject: Subject,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Parse subject color — fallback to primary amber
    val subjectColor = remember(subject.colorHex) {
        subject.colorHex?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
        } ?: Color(0xFFF59E0B)
    }

    // Animate background and border
    val bgColor by animateColorAsState(
        targetValue = if (isActive) subjectColor else Color.Transparent,
        animationSpec = tween(200),
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) subjectColor else subjectColor.copy(alpha = 0.4f),
        animationSpec = tween(200),
        label = "chipBorder"
    )
    val textColor by animateColorAsState(
        targetValue = when {
            isActive -> if (subjectColor.luminance() > 0.4f) Color(0xFF1C1917) else Color.White
            else     -> subjectColor
        },
        animationSpec = tween(200),
        label = "chipText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                width = if (isActive) 0.dp else 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = subject.nameAr,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
            color = textColor,
            fontSize = 13.sp,
            maxLines = 1
        )
    }
}