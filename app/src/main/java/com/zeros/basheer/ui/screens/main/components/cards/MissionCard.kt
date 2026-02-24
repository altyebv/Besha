package com.zeros.basheer.ui.screens.main.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
 * Design intent:
 * Each mission is visually branded with the subject's own accent color.
 * The badge (URGENT / HOT_STREAK / QUICK_WIN etc.) anchors context.
 * The `reason` from the recommendation engine is used as the subtitle —
 * it tells the student *why* this is recommended, not just what it is.
 *
 * Empty state (no recommendation): warm "all caught up" encouragement.
 */
@Composable
fun MissionCard(
    recommendation: ScoredRecommendation?,
    onActionClick: (ScoredRecommendation) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (recommendation == null) {
        AllCaughtUpCard(modifier = modifier)
        return
    }

    val subjectColor = MainColors.subjectColorByName(
        recommendation.subject.nameAr, 0
    )
    val subjectEmoji = MainColors.subjectEmoji(recommendation.subject.nameAr)
    val rec = recommendation.recommendation

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = subjectColor.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp, subjectColor.copy(alpha = 0.2f)
        )
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
                MissionBadgeChip(
                    badge = recommendation.badge,
                    color = subjectColor
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
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
                // Subject emoji in a tinted circle
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
                    // Subject name — small label above title
                    Text(
                        text = recommendation.subject.nameAr,
                        style = MaterialTheme.typography.labelMedium,
                        color = subjectColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Mission title — the main action
                    Text(
                        text = missionTitle(rec),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Reason — why this is recommended (from engine)
                    Text(
                        text = recommendation.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Progress indicator for continue-type missions ─────────────
            if (rec is Recommendation.ContinueLesson) {
                LinearProgressIndicator(
                    progress = { rec.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = subjectColor,
                    trackColor = subjectColor.copy(alpha = 0.15f)
                )
            }
            if (rec is Recommendation.CompleteUnit) {
                LinearProgressIndicator(
                    progress = { rec.percentComplete },
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
                Text(
                    text = missionCta(rec),
                    style = MaterialTheme.typography.labelLarge
                )
                // Time estimate on the right when available
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

@Composable
private fun AllCaughtUpCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = SuccessContainer),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp, Success.copy(alpha = 0.3f)
        )
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
    is Recommendation.ContinueLesson   -> rec.lessonTitle
    is Recommendation.CompleteUnit     -> "أكمل: ${rec.unitTitle}"
    is Recommendation.StartNewUnit     -> "ابدأ: ${rec.unitTitle}"
    is Recommendation.QuickReview      -> "مراجعة سريعة — ${rec.questionCount} سؤال"
    is Recommendation.ReviewWeakConcept -> rec.conceptName
    is Recommendation.StreakAtRisk     -> "سلسلتك في خطر! ${rec.hoursUntilLoss} ساعات متبقية"
}

private fun missionCta(rec: Recommendation): String = when (rec) {
    is Recommendation.ContinueLesson   -> "متابعة الدرس"
    is Recommendation.StartNewUnit     -> "ابدأ الآن"
    is Recommendation.CompleteUnit     -> "أكمل الوحدة"
    is Recommendation.QuickReview      -> "ابدأ المراجعة"
    is Recommendation.ReviewWeakConcept -> "راجع المفهوم"
    is Recommendation.StreakAtRisk     -> "حافظ على السلسلة"
}

private fun missionIcon(rec: Recommendation): ImageVector = when (rec) {
    is Recommendation.ContinueLesson   -> Icons.Default.PlayArrow
    is Recommendation.StartNewUnit     -> Icons.Default.PlayCircle
    is Recommendation.CompleteUnit     -> Icons.Default.CheckCircle
    is Recommendation.QuickReview      -> Icons.Default.Refresh
    is Recommendation.ReviewWeakConcept -> Icons.Default.Quiz
    is Recommendation.StreakAtRisk     -> Icons.Default.LocalFireDepartment
}

private fun missionTimeLabel(rec: Recommendation): String? = when (rec) {
    is Recommendation.ContinueLesson   -> "${rec.estimatedMinutes} د"
    is Recommendation.QuickReview      -> "${rec.estimatedMinutes} د"
    else -> null
}