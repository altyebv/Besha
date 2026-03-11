package com.zeros.basheer.feature.lesson.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.core.ui.theme.Amber
import com.zeros.basheer.core.ui.theme.AmberContainer
import com.zeros.basheer.core.ui.theme.AmberDeep
import com.zeros.basheer.core.ui.theme.Success
import com.zeros.basheer.feature.lesson.domain.model.LessonMetadata

// Brand amber — sourced from the app's central Color.kt, not redeclared locally

/**
 * Replaces the flat LessonSummaryCard with a two-zone entry experience.
 *
 * Zone 1 — Hook (amber gradient, emotional pull, shown when [metadata.hook] is present)
 * Zone 2 — Orientation (what you'll learn list + estimated time, always shown)
 *
 * Collapses gracefully: if [metadata] is null the card degrades to a plain
 * time chip + divider so no layout shift for existing lessons.
 */
@Composable
fun HookOrientationCard(
    title: String,
    estimatedMinutes: Int,
    metadata: LessonMetadata?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Zone 1: Hook ─────────────────────────────────────────────────────
        if (metadata?.hook != null) {
            HookZone(hook = metadata.hook)
        }

        // ── Zone 2: Orientation ──────────────────────────────────────────────
        OrientationZone(
            estimatedMinutes = estimatedMinutes,
            objectives = metadata?.orientation ?: emptyList()
        )
    }
}

// ── Hook zone ────────────────────────────────────────────────────────────────

@Composable
private fun HookZone(hook: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Amber.copy(alpha = 0.18f),
                            Amber.copy(alpha = 0.06f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Amber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("؟", fontSize = 14.sp, color = Amber, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "هل تساءلت...",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberDeep.copy(alpha = 0.8f)
                    )
                }

                // Hook text
                Text(
                    text = hook,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = AmberDeep,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

// ── Orientation zone ─────────────────────────────────────────────────────────

@Composable
private fun OrientationZone(
    estimatedMinutes: Int,
    objectives: List<String>
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Time chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$estimatedMinutes دقيقة",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Objectives list — only shown when authored
            if (objectives.isNotEmpty()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )

                Text(
                    text = "ستتعلم في هذا الدرس",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                objectives.forEach { objective ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text = objective,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}