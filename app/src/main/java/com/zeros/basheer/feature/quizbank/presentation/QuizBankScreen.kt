package com.zeros.basheer.feature.quizbank.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zeros.basheer.feature.practice.domain.model.PracticeGenerationType
import com.zeros.basheer.feature.practice.domain.model.PracticeSession
import com.zeros.basheer.feature.quizbank.domain.model.Exam
import com.zeros.basheer.feature.quizbank.domain.model.QuestionCounts

/**
 * Quiz Bank Screen - Main screen for accessing exams and practice modes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizBankScreen(
    navController: NavController,
    viewModel: QuizBankViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is QuizBankViewModel.NavigationEvent.NavigateToPractice -> {
                    navController.navigate("practice/${event.sessionId}")
                }
            }
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "بنك الأسئلة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(QuizBankEvent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                QuizBankTab.values().forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.onEvent(QuizBankEvent.SelectTab(tab)) },
                        text = {
                            Text(
                                text = when (tab) {
                                    QuizBankTab.MINISTRY_EXAMS -> "امتحانات الوزارة"
                                    QuizBankTab.SCHOOL_EXAMS -> "امتحانات المدارس"
                                    QuizBankTab.PRACTICE_MODES -> "التدريب"
                                    QuizBankTab.HISTORY -> "السجل"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }

            // Content based on selected tab
            when (state.selectedTab) {
                QuizBankTab.MINISTRY_EXAMS -> MinistryExamsContent(
                    exams = state.ministryExams,
                    isLoading = state.isLoading,
                    onExamClick = { viewModel.onEvent(QuizBankEvent.StartExam(it)) }
                )
                QuizBankTab.SCHOOL_EXAMS -> SchoolExamsContent(
                    exams = state.schoolExams,
                    isLoading = state.isLoading,
                    onExamClick = { viewModel.onEvent(QuizBankEvent.StartExam(it)) }
                )
                QuizBankTab.PRACTICE_MODES -> PracticeModesContent(
                    activeSession = state.activeSession,
                    questionCounts = state.questionCounts,
                    onStartPractice = { generationType ->
                        viewModel.onEvent(
                            QuizBankEvent.StartPracticeSession(generationType = generationType)
                        )
                    },
                    onResumeSession = { sessionId ->
                        viewModel.onEvent(QuizBankEvent.ResumeSession(sessionId))
                    },
                    navController = navController
                )
                QuizBankTab.HISTORY -> HistoryContent(
                    sessions = state.recentSessions,
                    averageScore = state.averageScore,
                    completedCount = state.completedSessionCount,
                    onSessionClick = { viewModel.onEvent(QuizBankEvent.ResumeSession(it)) }
                )
            }
        }
    }
}

/**
 * Ministry Exams Tab Content
 */
@Composable
private fun MinistryExamsContent(
    exams: List<Exam>,
    isLoading: Boolean,
    onExamClick: (String) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (exams.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.School,
            title = "لا توجد امتحانات",
            message = "سيتم إضافة امتحانات الوزارة قريباً"
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Group exams by year
        val examsByYear = exams.groupBy { it.year ?: 0 }.toSortedMap(reverseOrder())

        examsByYear.forEach { (year, yearExams) ->
            item {
                Text(
                    text = if (year > 0) "سنة $year" else "أخرى",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(yearExams) { exam ->
                ExamCard(
                    exam = exam,
                    onClick = { onExamClick(exam.id) }
                )
            }
        }
    }
}

/**
 * School Exams Tab Content
 */
@Composable
private fun SchoolExamsContent(
    exams: List<Exam>,
    isLoading: Boolean,
    onExamClick: (String) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (exams.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.AccountBalance,
            title = "لا توجد امتحانات مدارس",
            message = "سيتم إضافة امتحانات المدارس قريباً"
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(exams) { exam ->
            ExamCard(
                exam = exam,
                onClick = { onExamClick(exam.id) }
            )
        }
    }
}

/**
 * Practice Modes Tab Content
 */
@Composable
private fun PracticeModesContent(
    activeSession: PracticeSession?,
    questionCounts: QuestionCounts?,
    onStartPractice: (PracticeGenerationType) -> Unit,
    navController: NavController,
    onResumeSession: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active session card (if exists)
        if (activeSession != null) {
            item {
                ActiveSessionCard(
                    session = activeSession,
                    onClick = { navController.navigate("practice/${activeSession.id}") }
                )
            }
        }

        // Question counts summary
        if (questionCounts != null) {
            item {
                QuestionCountsSummary(counts = questionCounts)
            }
        }

        // Practice modes section
        item {
            Text(
                text = "أوضاع التدريب",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Quick practice modes
        item {
            PracticeModeCard(
                icon = Icons.Outlined.Bolt,
                title = "مراجعة سريعة",
                description = "10 أسئلة عشوائية",
                color = Color(0xFFFF9800),
                onClick = { onStartPractice(PracticeGenerationType.QUICK_REVIEW) }
            )
        }

        item {
            PracticeModeCard(
                icon = Icons.Outlined.MenuBook,
                title = "حسب التقدم",
                description = "أسئلة من الدروس التي درستها",
                color = Color(0xFF2196F3),
                onClick = { onStartPractice(PracticeGenerationType.BY_PROGRESS) }
            )
        }

        item {
            PracticeModeCard(
                icon = Icons.Outlined.TrendingDown,
                title = "نقاط الضعف",
                description = "ركز على المفاهيم الصعبة",
                color = Color(0xFFF44336),
                onClick = { onStartPractice(PracticeGenerationType.WEAK_AREAS) }
            )
        }

        item {
            PracticeModeCard(
                icon = Icons.Outlined.Category,
                title = "حسب الوحدة",
                description = "اختر وحدة معينة للتدريب",
                color = Color(0xFF4CAF50),
                onClick = { onStartPractice(PracticeGenerationType.BY_UNIT) }
            )
        }

        item {
            PracticeModeCard(
                icon = Icons.Outlined.Psychology,
                title = "حسب المفهوم",
                description = "ركز على مفهوم محدد",
                color = Color(0xFF9C27B0),
                onClick = { onStartPractice(PracticeGenerationType.BY_CONCEPT) }
            )
        }

        item {
            PracticeModeCard(
                icon = Icons.Outlined.QuestionAnswer,
                title = "حسب النوع",
                description = "صح/خطأ، اختيار متعدد، إلخ",
                color = Color(0xFF00BCD4),
                onClick = { onStartPractice(PracticeGenerationType.BY_TYPE) }
            )
        }
    }
}

/**
 * History Tab Content
 */
@Composable
private fun HistoryContent(
    sessions: List<PracticeSession>,
    averageScore: Float?,
    completedCount: Int,
    onSessionClick: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Stats summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "الجلسات",
                        value = completedCount.toString()
                    )

                    Divider(
                        modifier = Modifier
                            .height(48.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )

                    StatItem(
                        label = "المعدل",
                        value = if (averageScore != null) "${averageScore.toInt()}%" else "--"
                    )
                }
            }
        }

        if (sessions.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.History,
                    title = "لا يوجد سجل",
                    message = "ابدأ أول جلسة تدريب لك!"
                )
            }
        } else {
            item {
                Text(
                    text = "الجلسات الأخيرة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(sessions) { session ->
                SessionHistoryCard(
                    session = session,
                    onClick = { onSessionClick(session.id) }
                )
            }
        }
    }
}

/**
 * Exam Card Component
 */
@Composable
private fun ExamCard(
    exam: Exam,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.titleAr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (exam.duration != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${exam.duration} دقيقة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (exam.totalPoints != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${exam.totalPoints} نقطة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (exam.schoolName != null) {
                    Text(
                        text = exam.schoolName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Active Session Card Component
 */
@Composable
private fun ActiveSessionCard(
    session: PracticeSession,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جلسة نشطة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("متابعة")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress
            val progress = session.currentQuestionIndex.toFloat() / session.questionCount
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "السؤال ${session.currentQuestionIndex + 1} من ${session.questionCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Text(
                    text = "${session.correctCount} صحيحة | ${session.wrongCount} خاطئة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

/**
 * Question Counts Summary Component
 */
@Composable
private fun QuestionCountsSummary(counts: QuestionCounts) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "إحصائيات بنك الأسئلة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${counts.total}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "إجمالي الأسئلة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column {
                    Text(
                        text = "${counts.feedEligible}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "للمراجعة السريعة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Practice Mode Card Component
 */
@Composable
private fun PracticeModeCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = color
            )
        }
    }
}

/**
 * Session History Card Component
 */
@Composable
private fun SessionHistoryCard(
    session: PracticeSession,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getGenerationTypeLabel(session.generationType),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${session.questionCount} سؤال",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (session.score != null) {
                    Text(
                        text = "النتيجة: ${session.score.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            session.score >= 80 -> Color(0xFF4CAF50)
                            session.score >= 60 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        },
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (session.score != null) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            when {
                                session.score >= 80 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                session.score >= 60 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                else -> Color(0xFFF44336).copy(alpha = 0.1f)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${session.score.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            session.score >= 80 -> Color(0xFF4CAF50)
                            session.score >= 60 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Stat Item Component
 */
@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

/**
 * Empty State Component
 */
@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Helper function to get practice generation type label in Arabic
 */
private fun getGenerationTypeLabel(type: PracticeGenerationType): String {
    return when (type) {
        PracticeGenerationType.FULL_EXAM -> "امتحان كامل"
        PracticeGenerationType.BY_UNIT -> "حسب الوحدة"
        PracticeGenerationType.BY_LESSON -> "حسب الدرس"
        PracticeGenerationType.BY_CONCEPT -> "حسب المفهوم"
        PracticeGenerationType.BY_PROGRESS -> "حسب التقدم"
        PracticeGenerationType.WEAK_AREAS -> "نقاط الضعف"
        PracticeGenerationType.QUICK_REVIEW -> "مراجعة سريعة"
        PracticeGenerationType.BY_TYPE -> "حسب النوع"
        PracticeGenerationType.BY_SOURCE -> "حسب المصدر"
        PracticeGenerationType.CUSTOM -> "مخصص"
    }
}