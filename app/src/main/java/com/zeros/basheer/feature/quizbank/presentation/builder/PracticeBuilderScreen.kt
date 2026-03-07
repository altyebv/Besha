package com.zeros.basheer.feature.quizbank.presentation.builder

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.feature.practice.domain.model.PracticeGenerationType
import com.zeros.basheer.feature.quizbank.domain.model.QuestionType
import com.zeros.basheer.feature.quizbank.presentation.foundation.*
import com.zeros.basheer.feature.subject.domain.model.Units

/**
 * Full-screen practice session builder.
 *
 * Lets the student select:
 *   - Mode (Quick Review / By Unit / By Concept / By Type / Custom)
 *   - Unit multi-select (when BY_UNIT or CUSTOM)
 *   - Question type multi-select (when BY_TYPE or CUSTOM)
 *   - Difficulty range chips
 *   - Question count (10 / 20 / 30)
 *
 * Creates the session and navigates to [Screen.Practice] on confirm.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeBuilderScreen(
    onNavigateBack: () -> Unit,
    onSessionCreated: (Long) -> Unit,
    viewModel: PracticeBuilderViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // One-shot navigation
    LaunchedEffect(Unit) {
        viewModel.navigateToSession.collect { sessionId ->
            onSessionCreated(sessionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "بناء جلسة تدريب",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "اختر طريقة التدريب والفلاتر",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgBase)
            )
        },
        bottomBar = {
            BuilderStartBar(
                isLoading = state.isCreatingSession,
                questionCount = state.questionCount,
                onStart = { viewModel.onEvent(PracticeBuilderEvent.StartSession) }
            )
        },
        containerColor = bgBase
    ) { padding ->
        if (state.error != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.onEvent(PracticeBuilderEvent.ClearError) }) {
                        Text("حسناً")
                    }
                }
            ) { Text(state.error!!) }
        }

        if (state.noWeakQuestionsFound) {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(PracticeBuilderEvent.ClearNoWeakQuestionsFound) },
                title = { Text("لا توجد نقاط ضعف بعد") },
                text = {
                    Text(
                        "لم تُجِب على عدد كافٍ من الأسئلة بعد لتحديد نقاط الضعف. " +
                                "أكمل بعض جلسات التدريب أو الامتحانات أولاً، ثم حاول مجدداً."
                    )
                },
                confirmButton = {
                    Button(onClick = { viewModel.onEvent(PracticeBuilderEvent.ClearNoWeakQuestionsFound) }) {
                        Text("فهمت")
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Mode selection ────────────────────────────────────────────────
            item {
                BuilderSection(title = "طريقة التدريب", icon = Icons.Outlined.Bolt) {
                    ModeSelector(
                        selected = state.selectedMode,
                        onSelect = { viewModel.onEvent(PracticeBuilderEvent.SelectMode(it)) }
                    )
                }
            }

            // ── Unit filter (shown for BY_UNIT and CUSTOM) ────────────────────
            if (state.selectedMode in setOf(
                    PracticeGenerationType.BY_UNIT,
                    PracticeGenerationType.CUSTOM
                ) && state.availableUnits.isNotEmpty()
            ) {
                item {
                    BuilderSection(
                        title = "الوحدات",
                        icon = Icons.Outlined.AutoStories,
                        subtitle = if (state.selectedUnitIds.isEmpty()) "كل الوحدات" else "${state.selectedUnitIds.size} مختارة"
                    ) {
                        UnitMultiSelect(
                            units = state.availableUnits,
                            selected = state.selectedUnitIds,
                            onToggle = { viewModel.onEvent(PracticeBuilderEvent.ToggleUnit(it)) }
                        )
                    }
                }
            }

            // ── Question type filter (shown for BY_TYPE and CUSTOM) ───────────
            if (state.selectedMode in setOf(
                    PracticeGenerationType.BY_TYPE,
                    PracticeGenerationType.CUSTOM
                )
            ) {
                item {
                    BuilderSection(
                        title = "نوع الأسئلة",
                        icon = Icons.Outlined.Quiz,
                        subtitle = if (state.selectedQuestionTypes.isEmpty()) "كل الأنواع" else "${state.selectedQuestionTypes.size} مختارة"
                    ) {
                        QuestionTypeMultiSelect(
                            selected = state.selectedQuestionTypes,
                            onToggle = { viewModel.onEvent(PracticeBuilderEvent.ToggleQuestionType(it)) }
                        )
                    }
                }
            }

            // ── Difficulty ────────────────────────────────────────────────────
            item {
                BuilderSection(title = "مستوى الصعوبة", icon = Icons.Outlined.TrendingUp) {
                    DifficultyChips(
                        selected = state.selectedDifficultyRange,
                        onSelect = { viewModel.onEvent(PracticeBuilderEvent.SetDifficultyRange(it)) }
                    )
                }
            }

            // ── Question count ────────────────────────────────────────────────
            item {
                BuilderSection(title = "عدد الأسئلة", icon = Icons.Outlined.Numbers) {
                    CountSelector(
                        selected = state.questionCount,
                        onSelect = { viewModel.onEvent(PracticeBuilderEvent.SetQuestionCount(it)) }
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Mode Selector ─────────────────────────────────────────────────────────────

private data class ModeOption(
    val type: PracticeGenerationType,
    val emoji: String,
    val label: String,
    val description: String,
    val color: Color,
)

private val modeOptions = listOf(
    ModeOption(PracticeGenerationType.QUICK_REVIEW, "⚡", "مراجعة سريعة",    "أسئلة عشوائية من المادة",          Color(0xFF0EA5E9)),
    ModeOption(PracticeGenerationType.BY_UNIT,      "🗂", "حسب الوحدة",      "اختر وحدة أو أكثر للتركيز عليها", Color(0xFF10B981)),
    ModeOption(PracticeGenerationType.BY_TYPE,      "🔤", "حسب نوع السؤال",  "صح/خطأ، اختيار متعدد، تعبئة…",   Color(0xFFEC4899)),
    ModeOption(PracticeGenerationType.BY_PROGRESS,  "📈", "حسب التقدم",      "أسئلة من دروسك المكتملة فقط",     Color(0xFF8B5CF6)),
    ModeOption(PracticeGenerationType.CUSTOM,       "🎛", "مخصص",            "جمّع كل الفلاتر بنفسك",           Color(0xFFF59E0B)),
)

@Composable
private fun ModeSelector(
    selected: PracticeGenerationType,
    onSelect: (PracticeGenerationType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        modeOptions.forEach { option ->
            ModeOptionRow(
                option = option,
                isSelected = selected == option.type,
                onClick = { onSelect(option.type) }
            )
        }
    }
}

@Composable
private fun ModeOptionRow(
    option: ModeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) option.color.copy(alpha = 0.12f) else bgCard,
        animationSpec = tween(180), label = "mode_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) option.color.copy(alpha = 0.45f) else bgBorder,
        animationSpec = tween(180), label = "mode_border"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(option.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(option.emoji, fontSize = 20.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) option.color else textPrimary
            )
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(option.color),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Unit Multi-Select ─────────────────────────────────────────────────────────

@Composable
private fun UnitMultiSelect(
    units: List<Units>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(units) { unit ->
            val isSelected = unit.id in selected
            val color = AccentPractice
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) color.copy(alpha = 0.15f) else bgCard)
                    .border(
                        1.dp,
                        if (isSelected) color.copy(alpha = 0.50f) else bgBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onToggle(unit.id) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = unit.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) color else textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

// ── Question Type Multi-Select ────────────────────────────────────────────────

private fun QuestionType.toArabic() = when (this) {
    QuestionType.MCQ          -> "اختيار متعدد"
    QuestionType.TRUE_FALSE   -> "صح / خطأ"
    QuestionType.FILL_BLANK   -> "أكمل الفراغ"
    QuestionType.MATCH        -> "وصّل"
    QuestionType.SHORT_ANSWER -> "أجب بإيجاز"
    QuestionType.EXPLAIN      -> "اشرح / علل"
    QuestionType.LIST         -> "اذكر"
    QuestionType.TABLE        -> "أكمل الجدول"
    QuestionType.FIGURE       -> "من الشكل"
    QuestionType.COMPARE      -> "قارن بين"
    QuestionType.ORDER        -> "رتب"
}

@Composable
private fun QuestionTypeMultiSelect(
    selected: Set<QuestionType>,
    onToggle: (QuestionType) -> Unit,
) {
    val color = AccentExam
    // Two-column wrap layout using chunked rows
    val rows = QuestionType.entries.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { type ->
                    val isSelected = type in selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) color.copy(alpha = 0.13f) else bgCard)
                            .border(
                                1.dp,
                                if (isSelected) color.copy(alpha = 0.45f) else bgBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onToggle(type) }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.toArabic(),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) color else textSecondary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
                // Pad odd row
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// ── Difficulty Chips ──────────────────────────────────────────────────────────

private val difficultyOptions = listOf(
    Triple(1..5, "الكل",     Color(0xFF6B7280)),
    Triple(1..2, "سهل",     Color(0xFF10B981)),
    Triple(3..3, "متوسط",   Color(0xFFF59E0B)),
    Triple(4..5, "صعب",     Color(0xFFEF4444)),
)

@Composable
private fun DifficultyChips(
    selected: IntRange,
    onSelect: (IntRange) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        difficultyOptions.forEach { (range, label, color) ->
            val isSelected = selected == range
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) color.copy(alpha = 0.15f) else bgCard)
                    .border(
                        1.dp,
                        if (isSelected) color.copy(alpha = 0.50f) else bgBorder,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onSelect(range) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) color else textSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

// ── Question Count ────────────────────────────────────────────────────────────

@Composable
private fun CountSelector(
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(10, 20, 30, 40).forEach { count ->
            val isSelected = selected == count
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) AccentPractice.copy(alpha = 0.15f) else bgCard)
                    .border(
                        1.dp,
                        if (isSelected) AccentPractice.copy(alpha = 0.50f) else bgBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onSelect(count) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) AccentPractice else textPrimary
                    )
                    Text(
                        text = "سؤال",
                        style = MaterialTheme.typography.labelSmall,
                        color = textSecondary
                    )
                }
            }
        }
    }
}

// ── Section wrapper ───────────────────────────────────────────────────────────

@Composable
private fun BuilderSection(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = AccentPractice, modifier = Modifier.size(18.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            if (subtitle != null) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentPractice
                )
            }
        }
        content()
    }
}

// ── Bottom start bar ──────────────────────────────────────────────────────────

@Composable
private fun BuilderStartBar(
    isLoading: Boolean,
    questionCount: Int,
    onStart: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 12.dp,
        tonalElevation = 2.dp,
        color = bgCard
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = onStart,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPractice)
            ) {
                AnimatedContent(
                    targetState = isLoading,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                    label = "builder_cta"
                ) { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                            Text(
                                text = "ابدأ جلسة $questionCount سؤال",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}