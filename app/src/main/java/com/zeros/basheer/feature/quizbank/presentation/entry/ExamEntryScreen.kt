package com.zeros.basheer.feature.quizbank.presentation.entry

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.feature.quizbank.domain.model.ExamAttemptStatus
import com.zeros.basheer.feature.quizbank.domain.model.ExamSection
import com.zeros.basheer.feature.quizbank.domain.model.ExamSource
import com.zeros.basheer.feature.quizbank.domain.model.ExamType
import com.zeros.basheer.feature.quizbank.domain.model.QuizAttempt

/**
 * Exam entry/briefing screen shown before starting an exam.
 * Displays: exam info, rules, section breakdown, last attempt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamEntryScreen(
    onNavigateBack: () -> Unit,
    onStartExam: (String) -> Unit,
    viewModel: ExamEntryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val navigateToSession by viewModel.navigateToSession.collectAsState()

    // Handle navigation trigger
    LaunchedEffect(navigateToSession) {
        navigateToSession?.let { examId ->
            viewModel.onNavigationHandled()
            onStartExam(examId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            if (!state.isLoading && state.error == null) {
                StartExamBar(
                    onStart = { viewModel.onEvent(ExamEntryEvent.StartExam) }
                )
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center)
                }
            }

            state.exam != null -> {
                ExamEntryContent(
                    state = state,
                    onToggleSections = { viewModel.onEvent(ExamEntryEvent.ToggleSections) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun ExamEntryContent(
    state: ExamEntryState,
    onToggleSections: () -> Unit,
    modifier: Modifier = Modifier
) {
    val exam = state.exam!!

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Hero header ──────────────────────────────────────────────────────
        item {
            ExamHeroHeader(
                title = exam.titleAr,
                examType = exam.examType,
                source = exam.source,
                year = exam.year
            )
        }

        // ── Key stats ────────────────────────────────────────────────────────
        item {
            KeyStatsRow(
                duration = exam.duration,
                totalPoints = exam.totalPoints,
                questionCount = state.questionCount,
                sectionCount = state.sections.size,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // ── Last attempt banner ──────────────────────────────────────────────
        if (state.lastAttempt != null) {
            item {
                LastAttemptBanner(
                    attempt = state.lastAttempt,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // ── Sections breakdown ───────────────────────────────────────────────
        if (state.sections.isNotEmpty()) {
            item {
                SectionBreakdownHeader(
                    sectionCount = state.sections.size,
                    expanded = state.expandedSections,
                    onClick = onToggleSections,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (state.expandedSections) {
                itemsIndexed(state.sections) { index, section ->
                    SectionRow(
                        index = index,
                        section = section,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // ── Rules card ───────────────────────────────────────────────────────
        item {
            ExamRulesCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

// ── Hero Header ──────────────────────────────────────────────────────────────

@Composable
private fun ExamHeroHeader(
    title: String,
    examType: ExamType?,
    source: ExamSource,
    year: Int?
) {
    // Gradient based on exam type
    val gradientColors = when (examType) {
        ExamType.FINAL -> listOf(Color(0xFF1565C0), Color(0xFF0D47A1), Color(0xFF01579B))
        ExamType.SEMI_FINAL -> listOf(Color(0xFF6A1B9A), Color(0xFF4A148C), Color(0xFF311B92))
        ExamType.MONTHLY -> listOf(Color(0xFF00695C), Color(0xFF004D40), Color(0xFF1B5E20))
        null -> listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(gradientColors))
            .padding(top = 8.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Exam type icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (examType) {
                        ExamType.FINAL -> Icons.Outlined.EmojiEvents
                        ExamType.SEMI_FINAL -> Icons.Outlined.School
                        ExamType.MONTHLY -> Icons.Outlined.AccessTime
                        null -> Icons.Outlined.Description
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (examType != null) {
                    BadgeChip(
                        text = examTypeLabel(examType),
                        backgroundColor = Color.White.copy(alpha = 0.2f),
                        textColor = Color.White
                    )
                }
                BadgeChip(
                    text = sourceLabel(source),
                    backgroundColor = Color.White.copy(alpha = 0.2f),
                    textColor = Color.White
                )
                if (year != null) {
                    BadgeChip(
                        text = year.toString(),
                        backgroundColor = Color.White.copy(alpha = 0.2f),
                        textColor = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(text: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium,
            color = textColor, fontWeight = FontWeight.Medium)
    }
}

// ── Key Stats Row ─────────────────────────────────────────────────────────────

@Composable
private fun KeyStatsRow(
    duration: Int?,
    totalPoints: Int?,
    questionCount: Int,
    sectionCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (duration != null) {
                StatItem(
                    icon = Icons.Outlined.Timer,
                    value = "${duration}د",
                    label = "المدة",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (totalPoints != null) {
                StatItem(
                    icon = Icons.Outlined.EmojiEvents,
                    value = totalPoints.toString(),
                    label = "الدرجة",
                    color = Color(0xFFFF9800)
                )
            }
            StatItem(
                icon = Icons.Outlined.QuestionAnswer,
                value = questionCount.toString(),
                label = "سؤال",
                color = Color(0xFF9C27B0)
            )
            if (sectionCount > 0) {
                StatItem(
                    icon = Icons.Outlined.AutoStories,
                    value = sectionCount.toString(),
                    label = "قسم",
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Last Attempt Banner ───────────────────────────────────────────────────────

@Composable
private fun LastAttemptBanner(attempt: QuizAttempt, modifier: Modifier = Modifier) {
    val percentage = attempt.percentage
    val color = when {
        percentage == null -> MaterialTheme.colorScheme.surfaceVariant
        percentage >= 75 -> Color(0xFF4CAF50).copy(alpha = 0.15f)
        percentage >= 50 -> Color(0xFFFF9800).copy(alpha = 0.15f)
        else -> Color(0xFFF44336).copy(alpha = 0.15f)
    }
    val textColor = when {
        percentage == null -> MaterialTheme.colorScheme.onSurfaceVariant
        percentage >= 75 -> Color(0xFF2E7D32)
        percentage >= 50 -> Color(0xFFE65100)
        else -> Color(0xFFC62828)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.History, null, tint = textColor, modifier = Modifier.size(20.dp))
            Text(
                text = "آخر محاولة",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            if (percentage != null) {
                Text(
                    text = String.format("%.0f%%", percentage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            } else {
                Text("غير مكتملة", style = MaterialTheme.typography.bodySmall, color = textColor)
            }
        }
    }
}

// ── Sections Breakdown ────────────────────────────────────────────────────────

@Composable
private fun SectionBreakdownHeader(
    sectionCount: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.AutoStories, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(
                    text = "الأقسام ($sectionCount)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionRow(index: Int, section: ExamSection, modifier: Modifier = Modifier) {
    val sectionColor = sectionColors[index % sectionColors.size]

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = sectionColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(sectionColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = sectionColor
                )
            }
            Text(
                text = section.title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${section.questionIds.size} س",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (section.points != null) {
                    Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${section.points} د",
                        style = MaterialTheme.typography.labelMedium,
                        color = sectionColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ── Rules Card ────────────────────────────────────────────────────────────────

private val examRules = listOf(
    Pair(Icons.Outlined.Timer, "يبدأ المؤقت فور الضغط على ابدأ الامتحان"),
    Pair(Icons.Outlined.Warning, "لا تغادر التطبيق أثناء الامتحان"),
    Pair(Icons.Outlined.Flag, "يمكنك تعليم الأسئلة للمراجعة لاحقاً"),
    Pair(Icons.Default.GridOn, "تنقل بين الأسئلة بحرية عبر شبكة التنقل"),
    Pair(Icons.Outlined.CheckCircle, "راجع إجاباتك قبل التسليم النهائي"),
)

@Composable
private fun ExamRulesCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Info,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "تعليمات الامتحان",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

            examRules.forEach { (icon, rule) ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        icon, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Text(
                        text = rule,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

// ── Bottom Start Bar ──────────────────────────────────────────────────────────

@Composable
private fun StartExamBar(onStart: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 12.dp,
        tonalElevation = 3.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ابدأ الامتحان",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private val sectionColors = listOf(
    Color(0xFF2196F3),
    Color(0xFF9C27B0),
    Color(0xFF4CAF50),
    Color(0xFFFF9800),
    Color(0xFFE91E63),
    Color(0xFF00BCD4)
)

private fun examTypeLabel(type: ExamType) = when (type) {
    ExamType.FINAL -> "نهائي"
    ExamType.SEMI_FINAL -> "نصف سنوي"
    ExamType.MONTHLY -> "شهري"
}

private fun sourceLabel(source: ExamSource) = when (source) {
    ExamSource.MINISTRY -> "وزارة التربية"
    ExamSource.SCHOOL -> "امتحان مدرسة"
    ExamSource.PRACTICE -> "تدريب"
    ExamSource.CUSTOM -> "مخصص"
}