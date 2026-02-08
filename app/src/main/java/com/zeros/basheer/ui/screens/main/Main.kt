package com.zeros.basheer.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zeros.basheer.feature.streak.data.entity.StreakLevel
import com.zeros.basheer.domain.model.Recommendation
import com.zeros.basheer.domain.model.ScoredRecommendation
import com.zeros.basheer.ui.viewmodels.MainViewModel
import com.zeros.basheer.ui.viewmodels.SubjectWithProgress

@Composable
fun MainScreen(
    onSubjectClick: (String) -> Unit,
    navController: NavController? = null,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Banner - Overall Stats
                item {
                    OverallStatsBanner(
                        userName = "بشير",
                        streakDays = state.streakStatus.currentStreak,
                        overallProgress = state.overallProgress,
                        completedLessons = state.completedLessonsCount,
                        totalLessons = state.totalLessonsCount,
                        todayLevel = state.streakStatus.todayLevel
                    )
                }

                // Today's Focus Card (Top Recommendation)
                if (state.topRecommendation != null && !state.focusCardDismissed) {
                    item {
                        TodayFocusCard(
                            recommendation = state.topRecommendation!!,
                            onActionClick = { rec ->
                                // Navigate based on recommendation type
                                when (val r = rec.recommendation) {
                                    is Recommendation.ContinueLesson -> {
                                        navController?.navigate("lesson/${r.lessonId}")
                                    }
                                    is Recommendation.QuickReview -> {
                                        navController?.navigate("quiz_bank")
                                    }
                                    is Recommendation.CompleteUnit -> {
                                        onSubjectClick(rec.subject.id)
                                    }
                                    else -> onSubjectClick(rec.subject.id)
                                }
                            },
                            onDismiss = { viewModel.dismissFocusCard() }
                        )
                    }
                }

                // Section Header
                item {
                    Text(
                        text = "المواد الدراسية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Subjects List (Smart-ordered)
                items(state.subjects) { subjectWithProgress ->
                    EnhancedSubjectCard(
                        subjectWithProgress = subjectWithProgress,
                        onClick = { onSubjectClick(subjectWithProgress.subject.id) },
                        onContinueClick = {
                            // Navigate to lessons screen
                            onSubjectClick(subjectWithProgress.subject.id)
                        },
                        onPracticeClick = {
                            // Navigate to practice
                            navController?.navigate("quiz_bank")
                        }
                    )
                }
            }
        }
    }
}

/**
 * Compact overall stats banner at the top
 */
@Composable
fun OverallStatsBanner(
    userName: String,
    streakDays: Int,
    overallProgress: Float,
    completedLessons: Int,
    totalLessons: Int,
    todayLevel: StreakLevel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Greeting + Progress
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "مرحباً، $userName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "$completedLessons / $totalLessons دروس • ${(overallProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            // Right: Streak Badge + Progress Circle
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (todayLevel) {
                        StreakLevel.FLAME -> Color(0xFFFF6B35).copy(alpha = 0.2f)
                        StreakLevel.SPARK -> Color(0xFFFFB347).copy(alpha = 0.2f)
                        StreakLevel.COLD -> Color.Gray.copy(alpha = 0.1f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (streakDays > 0)
                                Icons.Filled.LocalFireDepartment
                            else
                                Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = when (todayLevel) {
                                StreakLevel.FLAME -> Color(0xFFFF6B35)
                                StreakLevel.SPARK -> Color(0xFFFFB347)
                                StreakLevel.COLD -> Color.Gray
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "$streakDays",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Compact Progress Circle
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier.size(50.dp),
                        strokeWidth = 5.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "${(overallProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Today's Focus - Top recommendation with special styling
 */
@Composable
fun TodayFocusCard(
    recommendation: ScoredRecommendation,
    onActionClick: (ScoredRecommendation) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "التركيز اليوم",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                // Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = recommendation.badge.emoji + " " + recommendation.badge.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subject name
            Text(
                text = recommendation.subject.nameAr,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Recommendation content
            Text(
                text = when (val r = recommendation.recommendation) {
                    is Recommendation.ContinueLesson -> r.lessonTitle
                    is Recommendation.CompleteUnit -> "أكمل وحدة: ${r.unitTitle}"
                    is Recommendation.QuickReview -> "مراجعة سريعة - ${r.questionCount} أسئلة"
                    is Recommendation.ReviewWeakConcept -> "راجع: ${r.conceptName}"
                    is Recommendation.StartNewUnit -> "ابدأ: ${r.unitTitle}"
                    is Recommendation.StreakAtRisk -> "حافظ على سلسلتك!"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reason
            Text(
                text = recommendation.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onActionClick(recommendation) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("ابدأ الآن")
                }

                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Text("لاحقاً")
                }
            }
        }
    }
}

/**
 * Enhanced Subject Card with badges, next lesson, and quick actions
 */
@Composable
fun EnhancedSubjectCard(
    subjectWithProgress: SubjectWithProgress,
    onClick: () -> Unit,
    onContinueClick: () -> Unit,
    onPracticeClick: () -> Unit
) {
    val progress = if (subjectWithProgress.totalLessons > 0) {
        subjectWithProgress.completedLessons.toFloat() / subjectWithProgress.totalLessons
    } else {
        0f
    }

    // Determine badge
    val badge = when {
        progress >= 0.8f && progress < 1.0f -> "🎯 قارب على الانتهاء"
        subjectWithProgress.lastStudied != null &&
                System.currentTimeMillis() - subjectWithProgress.lastStudied < 24 * 60 * 60 * 1000 -> "🔥 نشط"
        subjectWithProgress.completedLessons == 0 -> "✨ جديد"
        else -> null
    }

    val progressColor = when {
        progress >= 0.8f -> Color(0xFF4CAF50) // Green
        progress >= 0.5f -> Color(0xFFFFB347) // Orange
        else -> MaterialTheme.colorScheme.primary // Blue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Subject name
                    Text(
                        text = subjectWithProgress.subject.nameAr,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Units & Lessons count
                    Text(
                        text = "${subjectWithProgress.units.size} وحدات • ${subjectWithProgress.totalLessons} دروس",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badge
                if (badge != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (subjectWithProgress.completedLessons == subjectWithProgress.totalLessons &&
                    subjectWithProgress.totalLessons > 0) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "مكتمل",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next Lesson
            if (subjectWithProgress.nextLessonTitle != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = progressColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "التالي: ${subjectWithProgress.nextLessonTitle}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "التقدم",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${subjectWithProgress.completedLessons} / ${subjectWithProgress.totalLessons}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Continue Button
                Button(
                    onClick = onContinueClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = progressColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (subjectWithProgress.nextLessonTitle != null) "متابعة" else "ابدأ",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Practice Button
                OutlinedButton(
                    onClick = onPracticeClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تدريب",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}