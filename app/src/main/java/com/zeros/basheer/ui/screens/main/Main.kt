package com.zeros.basheer.ui.screens.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.data.models.StreakLevel
import com.zeros.basheer.data.models.StreakStatus
import com.zeros.basheer.ui.viewmodels.MainViewModel
import com.zeros.basheer.ui.viewmodels.SubjectWithProgress

@Composable
fun MainScreen(
    onSubjectClick: (String) -> Unit,
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
                // Streak Banner (NEW)
                item {
                    StreakBanner(
                        streakStatus = state.streakStatus
                    )
                }
                
                // User Stats Banner (Updated)
                item {
                    UserStatsBanner(
                        userName = "بشير",
                        completedLessons = state.completedLessonsCount,
                        totalLessons = state.subjects.sumOf { it.totalLessons },
                        streakStatus = state.streakStatus
                    )
                }
                
                // Today's Recommendation (NEW - placeholder for smart routing)
                if (state.streakStatus.todayLevel == StreakLevel.COLD && state.subjects.isNotEmpty()) {
                    item {
                        TodayRecommendation(
                            isAtRisk = state.streakStatus.isAtRisk,
                            currentStreak = state.streakStatus.currentStreak,
                            onStartClick = {
                                // Navigate to first subject for now
                                state.subjects.firstOrNull()?.let { 
                                    onSubjectClick(it.subject.id) 
                                }
                            }
                        )
                    }
                }

                // Subjects List
                items(state.subjects) { subjectWithProgress ->
                    SubjectCard(
                        subjectWithProgress = subjectWithProgress,
                        onClick = {
                            onSubjectClick(subjectWithProgress.subject.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StreakBanner(
    streakStatus: StreakStatus,
    modifier: Modifier = Modifier
) {
    val flameColor by animateColorAsState(
        targetValue = when (streakStatus.todayLevel) {
            StreakLevel.FLAME -> Color(0xFFFF6B35)  // Bright orange
            StreakLevel.SPARK -> Color(0xFFFFB347)  // Warm yellow
            StreakLevel.COLD -> Color(0xFF9E9E9E)   // Gray
        },
        animationSpec = tween(500),
        label = "flameColor"
    )
    
    val bgColor by animateColorAsState(
        targetValue = when (streakStatus.todayLevel) {
            StreakLevel.FLAME -> Color(0xFFFF6B35).copy(alpha = 0.1f)
            StreakLevel.SPARK -> Color(0xFFFFB347).copy(alpha = 0.1f)
            StreakLevel.COLD -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(500),
        label = "bgColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Streak info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Flame icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(flameColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (streakStatus.currentStreak > 0) 
                            Icons.Filled.LocalFireDepartment 
                        else 
                            Icons.Outlined.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = flameColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column {
                    Text(
                        text = "${streakStatus.currentStreak} يوم",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (streakStatus.todayLevel) {
                            StreakLevel.FLAME -> "🔥 أحسنت! واصل التقدم"
                            StreakLevel.SPARK -> "✨ نشاط خفيف اليوم"
                            StreakLevel.COLD -> if (streakStatus.isAtRisk) 
                                "⚠️ سلسلتك في خطر!" 
                            else 
                                "ابدأ التعلم اليوم"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Longest streak badge
            if (streakStatus.longestStreak > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${streakStatus.longestStreak}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "أطول سلسلة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodayRecommendation(
    isAtRisk: Boolean,
    currentStreak: Int,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isAtRisk) {
        Color(0xFFFF6B35).copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAtRisk) "حافظ على سلسلتك!" else "مرحباً بعودتك",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAtRisk) Color(0xFFFF6B35) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isAtRisk) {
                        "أكمل درساً أو راجع ${10} بطاقات للحفاظ على سلسلة $currentStreak يوم"
                    } else {
                        "ابدأ درساً جديداً أو راجع ما تعلمته"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = onStartClick,
                colors = if (isAtRisk) {
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B35)
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(if (isAtRisk) "ابدأ الآن" else "تابع")
            }
        }
    }
}

@Composable
fun UserStatsBanner(
    userName: String,
    completedLessons: Int,
    totalLessons: Int,
    streakStatus: StreakStatus
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Greeting
                Column {
                    Text(
                        text = "مرحباً،",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(
                        label = "الدروس المكتملة",
                        value = "$completedLessons / $totalLessons"
                    )

                    // Progress Circle
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { if (totalLessons > 0) completedLessons.toFloat() / totalLessons else 0f },
                            modifier = Modifier.size(50.dp),
                            strokeWidth = 5.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${if (totalLessons > 0) (completedLessons * 100 / totalLessons) else 0}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun SubjectCard(
    subjectWithProgress: SubjectWithProgress,
    onClick: () -> Unit
) {
    val progress = if (subjectWithProgress.totalLessons > 0) {
        subjectWithProgress.completedLessons.toFloat() / subjectWithProgress.totalLessons
    } else {
        0f
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
            // Subject Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subjectWithProgress.subject.nameAr,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${subjectWithProgress.units.size} وحدات • ${subjectWithProgress.totalLessons} دروس",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Completion Badge
                if (subjectWithProgress.completedLessons == subjectWithProgress.totalLessons &&
                    subjectWithProgress.totalLessons > 0) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "مكتمل",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Units Preview (show first 3 units)
            if (subjectWithProgress.units.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subjectWithProgress.units.take(3).forEach { unit ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )
                            Text(
                                text = unit.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (subjectWithProgress.units.size > 3) {
                        Text(
                            text = "و ${subjectWithProgress.units.size - 3} وحدات أخرى...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 14.dp)
                        )
                    }
                }
            }
        }
    }
}
