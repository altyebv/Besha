package com.zeros.basheer.ui.screens.main.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.core.ui.theme.Success
import com.zeros.basheer.core.ui.theme.SuccessContainer
import com.zeros.basheer.domain.model.Recommendation
import com.zeros.basheer.domain.model.RecommendationBadge
import com.zeros.basheer.domain.model.ScoredRecommendation
import com.zeros.basheer.ui.screens.main.components.foundation.MainColors

/**
 * Mission card — the primary call-to-action on the home screen.
 *
 * Only rendered after [isRecommendationLoaded] is true (gated in MainDashboardContent),
 * so [recommendation] == null here means the engine genuinely found nothing to suggest
 * (all caught up), never a loading race condition.
 *
 * Design intent:
 * Each mission is visually branded with the subject's own accent color.
 * The badge (URGENT / HOT_STREAK / QUICK_WIN etc.) anchors context.
 * The `reason` from the recommendation engine is used as the subtitle —
 * it tells the student *why* this is recommended, not just what it is.
 */
@Composable
fun MissionCard(
    recommendation: ScoredRecommendation?,
    onActionClick: (ScoredRecommendation) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate in on first composition — gives the card a satisfying entrance
    // after the engine resolves rather than just popping in.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(350)) { it / 4 }
    ) {
        if (recommendation == null) {
            AllCaughtUpCard(modifier = modifier)
        } else {
            RecommendationCard(
                recommendation = recommendation,
                onActionClick = onActionClick,
                onDismiss = onDismiss,
                modifier = modifier
            )
        }
    }
}

// ── Active recommendation ──────────────────────────────────────────────────────

@Composable
private fun RecommendationCard(
    recommendation: ScoredRecommendation,
    onActionClick: (ScoredRecommendation) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectColor = MainColors.subjectColorByName(recommendation.subject.nameAr, 0)
    val subjectEmoji = MainColors.subjectEmoji(recommendation.subject.nameAr)
    val rec = recommendation.recommendation

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = subjectColor.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, subjectColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Top row: badge chip + dismiss ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MissionBadgeChip(badge = recommendation.badge, color = subjectColor)
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "تجاهل",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // ── Subject identity + mission title ──────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(subjectColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = subjectEmoji, fontSize = 24.sp)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = recommendation.subject.nameAr,
                        style = MaterialTheme.typography.labelMedium,
                        color = subjectColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = missionTitle(rec),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = recommendation.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Progress bar for continue/complete missions ───────────────
            val progress: Float? = when (rec) {
                is Recommendation.ContinueLesson -> rec.progress
                is Recommendation.CompleteUnit   -> rec.percentComplete
                else                             -> null
            }
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = subjectColor,
                    trackColor = subjectColor.copy(alpha = 0.15f)
                )
            }

            // ── CTA button ────────────────────────────────────────────────
            Button(
                onClick = { onActionClick(recommendation) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = subjectColor,
                    contentColor = if (subjectColor.luminance() > 0.4f) Color(0xFF1C1917)
                    else Color.White
                )
            ) {
                Icon(
                    imageVector = missionIcon(rec),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = missionCta(rec), style = MaterialTheme.typography.labelLarge)
                missionTimeLabel(rec)?.let { time ->
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalContentColor.current.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

// ── All caught up empty state ──────────────────────────────────────────────────
// Only shown when the recommendation engine genuinely has nothing to suggest —
// never as a loading placeholder (that case is gated at the parent level).

@Composable
private fun AllCaughtUpCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = SuccessContainer),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Success.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Success.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✅", fontSize = 22.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "أنجزت كل شيء!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Success
                )
                Text(
                    text = "لا توجد مهام معلقة الآن. تصفح موادك أو راجع باستخدام بطاقات المراجعة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Badge chip ─────────────────────────────────────────────────────────────────

@Composable
private fun MissionBadgeChip(badge: RecommendationBadge, color: Color) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = badge.emoji, fontSize = 12.sp)
            Text(
                text = badge.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

// ── Copy helpers ───────────────────────────────────────────────────────────────

private fun missionTitle(rec: Recommendation): String = when (rec) {
    is Recommendation.ContinueLesson    -> rec.lessonTitle
    is Recommendation.CompleteUnit      -> "أكمل: ${rec.unitTitle}"
    is Recommendation.StartNewUnit      -> "ابدأ: ${rec.unitTitle}"
    is Recommendation.QuickReview       -> "مراجعة سريعة — ${rec.questionCount} سؤال"
    is Recommendation.ReviewWeakConcept -> rec.conceptName
    is Recommendation.StreakAtRisk      -> "سلسلتك في خطر! ${rec.hoursUntilLoss} ساعات متبقية"
}

private fun missionCta(rec: Recommendation): String = when (rec) {
    is Recommendation.ContinueLesson    -> "متابعة الدرس"
    is Recommendation.StartNewUnit      -> "ابدأ الآن"
    is Recommendation.CompleteUnit      -> "أكمل الوحدة"
    is Recommendation.QuickReview       -> "ابدأ المراجعة"
    is Recommendation.ReviewWeakConcept -> "راجع المفهوم"
    is Recommendation.StreakAtRisk      -> "حافظ على السلسلة"
}

private fun missionIcon(rec: Recommendation): ImageVector = when (rec) {
    is Recommendation.ContinueLesson    -> Icons.Default.PlayArrow
    is Recommendation.StartNewUnit      -> Icons.Default.PlayCircle
    is Recommendation.CompleteUnit      -> Icons.Default.CheckCircle
    is Recommendation.QuickReview       -> Icons.Default.Refresh
    is Recommendation.ReviewWeakConcept -> Icons.Default.Quiz
    is Recommendation.StreakAtRisk      -> Icons.Default.LocalFireDepartment
}

private fun missionTimeLabel(rec: Recommendation): String? = when (rec) {
    is Recommendation.ContinueLesson -> "${rec.estimatedMinutes} د"
    is Recommendation.QuickReview    -> "${rec.estimatedMinutes} د"
    else                             -> null
}