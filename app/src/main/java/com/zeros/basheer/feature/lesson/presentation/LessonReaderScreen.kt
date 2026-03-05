package com.zeros.basheer.feature.lesson.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zeros.basheer.core.math.KatexRenderer
import com.zeros.basheer.domain.model.LessonContent
import com.zeros.basheer.domain.model.SectionUiModel
import com.zeros.basheer.feature.lesson.domain.model.LessonMetadata
import com.zeros.basheer.feature.lesson.presentation.components.CheckpointCard
import com.zeros.basheer.feature.lesson.presentation.components.HookOrientationCard
import com.zeros.basheer.ui.components.blocks.BlockRenderer
import com.zeros.basheer.ui.components.common.ConceptModal
import com.zeros.basheer.ui.components.common.ExitConfirmationDialog
import com.zeros.basheer.ui.components.common.ExitContext
import com.zeros.basheer.ui.components.common.LessonCompletionScreen
import com.zeros.basheer.ui.components.common.SectionHeader

// ── Brand tokens ─────────────────────────────────────────────────────────────
private val Amber = Color(0xFFF59E0B)
private val AmberDeep = Color(0xFF78350F)
private val AmberLight = Color(0xFFFEF3C7)
private val Coral = Color(0xFFF43F5E)
private val Success = Color(0xFF10B981)

// ─────────────────────────────────────────────────────────────────────────────
// Root screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonReaderScreen(
    lessonId: String,
    initialPartIndex: Int = 0,
    onBackClick: () -> Unit,
    onNavigateToNextPart: ((Int) -> Unit)? = null,
    viewModel: LessonReaderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    var showExitDialog by remember { mutableStateOf(false) }

    // ── Back handler ─────────────────────────────────────────────────────────
    BackHandler(enabled = true) {
        if (state.isPartAlreadyComplete || state.isLessonComplete) {
            viewModel.pauseTimeTracking(); onBackClick()
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            context = ExitContext.LESSON,
            forwardPull = state.forwardPull,
            onConfirm = { showExitDialog = false; viewModel.pauseTimeTracking(); onBackClick() },
            onDismiss = { showExitDialog = false }
        )
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startTimeTracking()
                Lifecycle.Event.ON_PAUSE  -> viewModel.pauseTimeTracking()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── Detect end-of-list (and scroll-back) ─────────────────────────────────
    val layoutInfo = listState.layoutInfo
    LaunchedEffect(layoutInfo) {
        val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@LaunchedEffect
        val totalItems = layoutInfo.totalItemsCount
        if (totalItems > 0 && lastVisibleIndex >= totalItems - 1) {
            viewModel.onReachedEnd()
        } else {
            // User has scrolled back up — hide the finish bar
            viewModel.onScrolledAwayFromEnd()
        }
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LessonTopBar(
                title = state.lessonContent?.title ?: "جاري التحميل...",
                currentPartIndex = state.currentPartIndex,
                totalParts = state.totalParts,
                completedPartIndices = state.completedPartIndices,
                onBack = {
                    if (state.isPartAlreadyComplete || state.isLessonComplete) {
                        viewModel.pauseTimeTracking(); onBackClick()
                    } else {
                        showExitDialog = true
                    }
                }
            )
        },
        bottomBar = {
            // Sticky finish bar — hidden once completion screen is showing
            AnimatedVisibility(
                visible = state.hasScrolledToEnd && !state.showCompletionModal,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                PartFinishBar(
                    currentPartIndex = state.currentPartIndex,
                    totalParts = state.totalParts,
                    isPartAlreadyComplete = state.isPartAlreadyComplete,
                    forwardPull = state.forwardPull,
                    nextLessonTitle = state.nextLessonTitle,
                    onFinish = { viewModel.markPartComplete() }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.error != null -> ErrorContent(
                    error = state.error!!,
                    onBackClick = onBackClick,
                    modifier = Modifier.align(Alignment.Center)
                )

                state.lessonContent != null -> {
                    // LessonBody always stays rendered — when completion is visible,
                    // it blurs + dims behind the card rather than being swapped out.
                    // This preserves the immersive feel and keeps spatial context.
                    val blurRadius by animateDpAsState(
                        targetValue   = if (state.showCompletionModal) 8.dp else 0.dp,
                        animationSpec = tween(300),
                        label         = "bodyBlur"
                    )
                    val bodyAlpha by animateFloatAsState(
                        targetValue   = if (state.showCompletionModal) 0.35f else 1f,
                        animationSpec = tween(300),
                        label         = "bodyAlpha"
                    )

                    // ── Layer 1: lesson content (always present) ──────────────
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(blurRadius)
                            .alpha(bodyAlpha)
                            // Block all touches while card is showing
                            .then(
                                if (state.showCompletionModal)
                                    Modifier.pointerInput(Unit) { /* consume */ }
                                else Modifier
                            )
                    ) {
                        LessonBody(
                            lessonContent        = state.lessonContent!!,
                            sections             = state.currentPartSections,
                            partIndex            = state.currentPartIndex,
                            totalParts           = state.totalParts,
                            checkpoints          = state.checkpoints,
                            onConceptClick       = viewModel::onConceptClick,
                            onCheckpointSelect   = viewModel::onCheckpointSelect,
                            onCheckpointSubmit   = viewModel::onCheckpointSubmit,
                            onCheckpointContinue = viewModel::onCheckpointContinue,
                            listState            = listState,
                            katexRenderer        = viewModel.katexRenderer
                        )
                    }

                    // ── Layer 2: scrim ────────────────────────────────────────
                    AnimatedVisibility(
                        visible = state.showCompletionModal,
                        enter   = fadeIn(tween(300)),
                        exit    = fadeOut(tween(200))
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f))
                        )
                    }

                    // ── Layer 3: floating completion card ─────────────────────
                    AnimatedVisibility(
                        visible = state.showCompletionModal,
                        enter   = slideInVertically(tween(350)) { it / 3 } + fadeIn(tween(350)),
                        exit    = slideOutVertically(tween(250)) { it / 4 } + fadeOut(tween(200))
                    ) {
                        val isLastPart = state.currentPartIndex >= state.totalParts - 1
                        Box(
                            modifier        = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 24.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            LessonCompletionScreen(
                                lessonTitle        = state.lessonContent!!.title,
                                xpEarned           = state.xpEarned,
                                readingTimeSeconds = state.readingTimeSeconds,
                                isRepeatCompletion = state.isRepeatCompletion,
                                isLastPart         = isLastPart,
                                currentPartIndex   = state.currentPartIndex,
                                totalParts         = state.totalParts,
                                checkpointScore    = state.checkpointScore,
                                nextLessonTitle    = if (isLastPart) state.nextLessonTitle else null,
                                onContinue = {
                                    viewModel.dismissCompletionModal()
                                    if (!isLastPart) {
                                        onNavigateToNextPart?.invoke(state.currentPartIndex + 1)
                                            ?: onBackClick()
                                    } else {
                                        onBackClick()
                                    }
                                },
                                onBackToLessons = {
                                    viewModel.dismissCompletionModal(); onBackClick()
                                }
                            )
                        }
                    }
                }
            }

            if (state.activeConcept != null) {
                ConceptModal(
                    concept   = state.activeConcept,
                    onDismiss = viewModel::dismissConceptModal
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top bar with part tabs — edge-to-edge, flush with the status bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonTopBar(
    title: String,
    currentPartIndex: Int,
    totalParts: Int,
    completedPartIndices: Set<Int>,
    onBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (totalParts > 1) 4.dp else 2.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Extend behind the status bar — the top padding below will push
                // content below it, so nothing is obscured.
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // ── Navigation + title row ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (totalParts > 1) {
                        AnimatedContent(
                            targetState = currentPartIndex,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "partLabel"
                        ) { idx ->
                            Text(
                                text = "الجزء ${idx + 1} من $totalParts",
                                style = MaterialTheme.typography.labelSmall,
                                color = Amber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ── Part tabs (only when multi-part) ─────────────────────────────
            if (totalParts > 1) {
                PartTabsRow(
                    totalParts = totalParts,
                    currentPartIndex = currentPartIndex,
                    completedPartIndices = completedPartIndices
                )
            }
        }
    }
}

/**
 * Horizontal row of part tabs.
 *
 * - Completed parts: solid amber pill with ✓ icon — tappable (future: navigate back)
 * - Current part: amber outline pill — active
 * - Future parts: muted pill with lock icon — not tappable
 */
@Composable
private fun PartTabsRow(
    totalParts: Int,
    currentPartIndex: Int,
    completedPartIndices: Set<Int>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalParts) { index ->
            val isCompleted = completedPartIndices.contains(index)
            val isCurrent = index == currentPartIndex
            val isFuture = !isCompleted && !isCurrent

            PartTab(
                number = index + 1,
                isCompleted = isCompleted,
                isCurrent = isCurrent,
                isFuture = isFuture,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PartTab(
    number: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isFuture: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isCompleted -> Amber
        isCurrent   -> Amber.copy(alpha = 0.12f)
        else        -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val borderColor = when {
        isCompleted -> Amber
        isCurrent   -> Amber
        else        -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }
    val contentColor = when {
        isCompleted -> AmberDeep
        isCurrent   -> Amber
        else        -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(3.dp))
            } else if (isFuture) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(3.dp))
            }
            Text(
                text = "$number",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                fontSize = 11.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Lesson body — renders only the current part's sections
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LessonBody(
    lessonContent: LessonContent,
    sections: List<SectionUiModel>,
    partIndex: Int,
    totalParts: Int,
    checkpoints: Map<String, CheckpointUiState>,
    onConceptClick: (String) -> Unit,
    onCheckpointSelect: (sectionId: String, answer: String) -> Unit,
    onCheckpointSubmit: (sectionId: String) -> Unit,
    onCheckpointContinue: (sectionId: String) -> Unit,
    listState: LazyListState,
    katexRenderer: KatexRenderer
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)  // space for sticky bar
    ) {
        // ── Hook / Orientation card (scoped to part) ──────────────────────────
        item(key = "hook_orientation") {
            if (partIndex == 0) {
                // Full hook + orientation on the first part
                HookOrientationCard(
                    title = lessonContent.title,
                    estimatedMinutes = lessonContent.estimatedMinutes,
                    metadata = lessonContent.metadata
                )
            } else {
                // Lighter "Part N" header for subsequent parts
                PartOrientationHeader(
                    partNumber = partIndex + 1,
                    totalParts = totalParts,
                    metadata = lessonContent.metadata
                )
            }
        }

        // ── Sections + checkpoints ────────────────────────────────────────────
        sections.forEach { section ->
            item(key = "section_header_${section.id}") {
                SectionHeader(section = section)
            }

            items(items = section.blocks, key = { it.id }) { block ->
                BlockRenderer(
                    block = block,
                    onConceptClick = onConceptClick,
                    katexRenderer = katexRenderer
                )
            }

            checkpoints[section.id]?.let { cpState ->
                item(key = "checkpoint_${section.id}") {
                    CheckpointCard(
                        sectionTitle = section.title,
                        state = cpState,
                        onSelect = { answer -> onCheckpointSelect(section.id, answer) },
                        onSubmit = { onCheckpointSubmit(section.id) },
                        onContinue = { onCheckpointContinue(section.id) }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Part orientation header (parts 2, 3… — no full hook, just context)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PartOrientationHeader(
    partNumber: Int,
    totalParts: Int,
    metadata: LessonMetadata?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = Amber.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Amber.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Part pill
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Amber.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$partNumber",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Amber
                )
            }
            Column {
                Text(
                    text = "الجزء $partNumber من $totalParts",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AmberDeep
                )
                Text(
                    text = "تابع من حيث توقفت",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sticky finish bar
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Sticky bottom bar — appears after user scrolls to the end of the current part.
 *
 * Shows:
 * - "إنهاء الجزء N" if more parts remain
 * - "إنهاء الدرس" on the final part
 * - forwardPull teaser text above the button
 */
@Composable
fun PartFinishBar(
    currentPartIndex: Int,
    totalParts: Int,
    isPartAlreadyComplete: Boolean,
    forwardPull: String?,
    nextLessonTitle: String?,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLastPart = currentPartIndex >= totalParts - 1
    val buttonLabel = when {
        isLastPart -> "إنهاء الدرس"
        else       -> "إنهاء الجزء ${currentPartIndex + 1} ←"
    }
    val pullText = when {
        !isLastPart -> "الجزء ${currentPartIndex + 2} من $totalParts في انتظارك"
        forwardPull != null -> forwardPull
        nextLessonTitle != null -> "التالي: $nextLessonTitle"
        else -> null
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Forward pull teaser
            if (pullText != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Amber.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = pullText,
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberDeep,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Finish button
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLastPart) Amber else MaterialTheme.colorScheme.primary,
                    contentColor = if (isLastPart) AmberDeep else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (isPartAlreadyComplete) "تمت المراجعة ✓" else buttonLabel,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Error
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(error: String, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("حدث خطأ", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
        Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onBackClick) { Text("رجوع") }
    }
}