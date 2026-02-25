package com.zeros.basheer.feature.lesson.presentation.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.core.ui.theme.Success
import com.zeros.basheer.feature.lesson.presentation.components.foundation.LessonMetrics
import com.zeros.basheer.feature.lesson.presentation.components.foundation.calculateProgress
import com.zeros.basheer.feature.subject.domain.model.Units

/**
 * Collapsible unit section header.
 *
 * Layout:
 *   [unit number]  [title + description]  [progress badge]  [chevron]
 *   [──────────────────── progress bar (when in progress) ────────────]
 *
 * States:
 * - Collapsed: chevron points down (▼), lessons hidden
 * - Expanded: chevron rotated (▲), lessons visible
 * - Complete: green badge with ✓ instead of n/n
 */
@Composable
fun UnitHeader(
    unit: Units,
    unitNumber: Int,
    completedLessons: Int,
    totalLessons: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = calculateProgress(completedLessons, totalLessons)
    val isComplete = completedLessons >= totalLessons && totalLessons > 0

    val chevronAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(250),
        label = "chevron"
    )

    val headerBg by animateColorAsState(
        targetValue = if (isExpanded)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else
            Color.Transparent,
        animationSpec = tween(200),
        label = "header_bg"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                if (isExpanded) collapse { onToggle(); true }
                else expand { onToggle(); true }
            }
    ) {
        // ── Clickable header row ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(headerBg)
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (isExpanded) "طي الوحدة" else "فتح الوحدة"
                ) { onToggle() }
                .padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Unit number badge
            UnitNumberBadge(
                number = unitNumber,
                isComplete = isComplete
            )

            // Title + description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = unit.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                unit.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            // Progress badge
            UnitProgressBadge(
                completed = completedLessons,
                total = totalLessons,
                isComplete = isComplete
            )

            // Chevron
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(chevronAngle)
            )
        }

        // ── Progress bar (only when in progress, always visible below header) ──
        if (!isComplete && totalLessons > 0 && completedLessons > 0) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

// ── Unit number circle ─────────────────────────────────────────────────────────

@Composable
private fun UnitNumberBadge(number: Int, isComplete: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
                if (isComplete) Success.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isComplete) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Success,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Text(
                text = "$number",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ── Progress badge (n/n or ✓) ──────────────────────────────────────────────────

@Composable
private fun UnitProgressBadge(completed: Int, total: Int, isComplete: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = if (isComplete) Success.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = if (isComplete) "✓ مكتمل" else "$completed/$total",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isComplete) Success
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}