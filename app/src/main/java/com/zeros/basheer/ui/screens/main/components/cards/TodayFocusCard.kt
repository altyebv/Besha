package com.zeros.basheer.ui.screens.main.components.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.domain.model.Recommendation
import com.zeros.basheer.domain.model.ScoredRecommendation
import com.zeros.basheer.ui.screens.main.components.foundation.MainMetrics

/**
 * Today's focus card displaying AI-powered recommendation.
 *
 * Supports 6 recommendation types:
 * - Continue Lesson
 * - Complete Unit
 * - Start New Unit
 * - Quick Review
 * - Review Weak Concept
 * - Streak at Risk
 *
 * Features:
 * - Contextual icons and text
 * - Smart action buttons
 * - Dismissible
 * - Subject context display
 *
 * @param recommendation AI-scored recommendation
 * @param onActionClick Callback when action button clicked
 * @param onDismiss Callback when dismiss button clicked
 * @param modifier Standard modifier
 */
@Composable
fun TodayFocusCard(
    recommendation: ScoredRecommendation,
    onActionClick: (ScoredRecommendation) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rec = recommendation.recommendation

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Today's focus: ${getRecommendationTitle(rec)}"
            },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MainMetrics.focusCardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MainMetrics.focusCardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with dismiss button
            FocusCardHeader(onDismiss = onDismiss)

            // Recommendation content
            RecommendationContent(
                recommendation = rec,
                subject = recommendation.subject
            )

            // Action button
            ActionButton(
                recommendation = rec,
                onClick = { onActionClick(recommendation) }
            )
        }
    }
}

// ============================================================================
// SUB-COMPONENTS
// ============================================================================

/**
 * Card header with title and dismiss button
 */
@Composable
private fun FocusCardHeader(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "تركيز اليوم",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Recommendation content with title and context
 */
@Composable
private fun RecommendationContent(
    recommendation: Recommendation,
    subject: com.zeros.basheer.feature.subject.domain.model.Subject,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Main recommendation text
        Text(
            text = getRecommendationTitle(recommendation),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        // Additional context
        Text(
            text = getRecommendationContext(recommendation, subject),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

/**
 * Action button with contextual icon and text
 */
@Composable
private fun ActionButton(
    recommendation: Recommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Icon(
            imageVector = getRecommendationIcon(recommendation),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = getRecommendationActionText(recommendation),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

/**
 * Get recommendation title text
 */
private fun getRecommendationTitle(rec: Recommendation): String = when (rec) {
    is Recommendation.ContinueLesson -> "تابع درسك: ${rec.lessonTitle}"
    is Recommendation.CompleteUnit -> "أكمل وحدة: ${rec.unitTitle}"
    is Recommendation.QuickReview -> "مراجعة سريعة - ${rec.questionCount} سؤال"
    is Recommendation.StartNewUnit -> "ابدأ وحدة جديدة: ${rec.unitTitle}"
    is Recommendation.ReviewWeakConcept -> "راجع مفهوم: ${rec.conceptName}"
    is Recommendation.StreakAtRisk -> "⚠️ سلسلتك في خطر - ${rec.hoursUntilLoss} ساعات متبقية"
}

/**
 * Get recommendation context/details
 */
private fun getRecommendationContext(
    rec: Recommendation,
    subject: com.zeros.basheer.feature.subject.domain.model.Subject
): String = when (rec) {
    is Recommendation.ContinueLesson ->
        "المادة: ${subject.nameAr} • ${rec.estimatedMinutes} دقائق"
    is Recommendation.CompleteUnit ->
        "المادة: ${subject.nameAr} • ${rec.lessonsCompleted}/${rec.totalLessons} دروس"
    is Recommendation.QuickReview ->
        "المادة: ${subject.nameAr} • ${rec.estimatedMinutes} دقائق"
    is Recommendation.StartNewUnit ->
        "المادة: ${subject.nameAr} • ${rec.lessonCount} دروس"
    is Recommendation.ReviewWeakConcept ->
        "المادة: ${subject.nameAr} • نسبة النجاح: ${(rec.successRate * 100).toInt()}%"
    is Recommendation.StreakAtRisk ->
        "سلسلة ${rec.streakDays} أيام في خطر!"
}

/**
 * Get contextual icon for recommendation
 */
private fun getRecommendationIcon(rec: Recommendation): ImageVector = when (rec) {
    is Recommendation.ContinueLesson -> Icons.Default.PlayArrow
    is Recommendation.StartNewUnit -> Icons.Default.PlayCircle
    is Recommendation.QuickReview -> Icons.Default.Refresh
    is Recommendation.ReviewWeakConcept -> Icons.Default.Quiz
    is Recommendation.CompleteUnit -> Icons.Default.CheckCircle
    is Recommendation.StreakAtRisk -> Icons.Default.LocalFireDepartment
}

/**
 * Get action button text
 */
private fun getRecommendationActionText(rec: Recommendation): String = when (rec) {
    is Recommendation.ContinueLesson -> "متابعة الدرس"
    is Recommendation.StartNewUnit -> "ابدأ الآن"
    is Recommendation.QuickReview -> "ابدأ المراجعة"
    is Recommendation.ReviewWeakConcept -> "راجع المفهوم"
    is Recommendation.CompleteUnit -> "أكمل الوحدة"
    is Recommendation.StreakAtRisk -> "حافظ على السلسلة"
}