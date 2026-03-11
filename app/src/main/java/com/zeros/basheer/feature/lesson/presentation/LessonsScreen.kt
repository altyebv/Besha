package com.zeros.basheer.feature.lesson.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.core.ui.theme.Success
import com.zeros.basheer.feature.lesson.domain.model.LessonDomain
import com.zeros.basheer.feature.lesson.presentation.components.cards.LessonRow
import com.zeros.basheer.feature.lesson.presentation.components.cards.UnitHeader
import com.zeros.basheer.feature.lesson.presentation.components.foundation.LessonMetrics
import com.zeros.basheer.feature.lesson.presentation.components.states.EmptyState
import com.zeros.basheer.feature.lesson.presentation.components.states.LoadingState
import com.zeros.basheer.feature.lesson.presentation.components.topbar.LessonsTopBar
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.domain.model.Units
import com.zeros.basheer.ui.components.common.SubjectSwitcherStrip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    // Now nullable — when coming from bottom nav with no prior selection,
    // the strip lets the user pick inline rather than hitting a gate screen.
    initialSubjectId: String?,
    allSubjects: List<Subject>,
    onLessonClick: (String, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: LessonsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // ── Active subject — starts from nav arg, switches via the strip ──────────
    var activeSubjectId by remember(initialSubjectId) {
        mutableStateOf(initialSubjectId ?: allSubjects.firstOrNull()?.id)
    }

    // Load whenever the active subject changes
    LaunchedEffect(activeSubjectId) {
        activeSubjectId?.let { viewModel.loadLessons(it) }
    }

    // Clear search when switching subjects so stale results don't flash
    LaunchedEffect(activeSubjectId) {
        searchQuery = ""
        isSearchActive = false
    }

    // Build filtered units
    val filteredUnits = remember(state.units, searchQuery) {
        if (searchQuery.isBlank()) state.units
        else state.units.mapNotNull { (unit, lessons) ->
            val filtered = lessons.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }
            if (filtered.isNotEmpty()) unit to filtered else null
        }
    }

    // Expand state per unit: default = first incomplete unit open, rest closed
    val expandedUnits = remember(state.units) {
        val firstIncompleteIdx = state.units.indexOfFirst { (_, lessons) ->
            lessons.any { !state.completedLessonIds.contains(it.id) }
        }
        mutableStateMapOf<String, Boolean>().apply {
            state.units.forEachIndexed { idx, (unit, _) ->
                put(unit.id, idx == firstIncompleteIdx || firstIncompleteIdx == -1)
            }
        }
    }

    // When searching, expand all matching units automatically
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            filteredUnits.forEach { (unit, _) -> expandedUnits[unit.id] = true }
        }
    }

    Scaffold(
        topBar = {
            Column {
                LessonsTopBar(
                    subjectName = state.subjectName,
                    totalLessons = state.units.sumOf { it.second.size },
                    searchQuery = searchQuery,
                    isSearchActive = isSearchActive,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchToggle = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) searchQuery = ""
                    },
                    onBack = onBack
                )

                // ── Subject switcher — only shown when there are multiple subjects ──
                if (allSubjects.size > 1) {
                    SubjectSwitcherStrip(
                        subjects = allSubjects,
                        activeSubjectId = activeSubjectId,
                        onSubjectSelect = { newId ->
                            if (newId != activeSubjectId) {
                                activeSubjectId = newId
                            }
                        }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))

            filteredUnits.isEmpty() && searchQuery.isNotBlank() -> EmptyState(
                icon = Icons.Default.SearchOff,
                message = "لا توجد نتائج",
                description = "جرب كلمات بحث مختلفة",
                modifier = Modifier.padding(padding)
            )

            state.units.isEmpty() -> EmptyState(
                icon = Icons.Default.MenuBook,
                message = "لا توجد دروس متاحة",
                description = "سيتم إضافة الدروس قريباً",
                modifier = Modifier.padding(padding)
            )

            else -> LessonsContent(
                units = filteredUnits,
                completedLessonIds = state.completedLessonIds,
                nextPartByLesson = state.nextPartByLesson,
                expandedUnits = expandedUnits,
                onToggleUnit = { unitId ->
                    expandedUnits[unitId] = !(expandedUnits[unitId] ?: false)
                },
                onLessonClick = onLessonClick,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

// ── Content ────────────────────────────────────────────────────────────────────

@Composable
private fun LessonsContent(
    units: List<Pair<Units, List<LessonDomain>>>,
    completedLessonIds: List<String>,
    nextPartByLesson: Map<String, Int>,
    expandedUnits: Map<String, Boolean>,
    onToggleUnit: (String) -> Unit,
    onLessonClick: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalLessons = units.sumOf { it.second.size }
    val completedCount = completedLessonIds.size

    LazyColumn(
        state = rememberLazyListState(),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = LessonMetrics.contentPadding,
            vertical = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Compact subject progress strip
        item(key = "subject_strip") {
            SubjectProgressStrip(
                completed = completedCount,
                total = totalLessons
            )
            Spacer(Modifier.height(12.dp))
        }

        // Units
        units.forEachIndexed { index, (unit, lessons) ->
            val isExpanded = expandedUnits[unit.id] ?: false
            val completedInUnit = lessons.count { completedLessonIds.contains(it.id) }

            // First incomplete lesson = "next"
            val nextLessonId = lessons
                .sortedBy { it.order }
                .firstOrNull { !completedLessonIds.contains(it.id) }?.id

            unitSection(
                unit = unit,
                unitNumber = index + 1,
                lessons = lessons.sortedBy { it.order },
                completedLessonIds = completedLessonIds,
                nextPartByLesson = nextPartByLesson,
                nextLessonId = nextLessonId,
                completedInUnit = completedInUnit,
                isExpanded = isExpanded,
                onToggle = { onToggleUnit(unit.id) },
                onLessonClick = onLessonClick
            )
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ── Unit section (header + animated lessons) ───────────────────────────────────

private fun LazyListScope.unitSection(
    unit: Units,
    unitNumber: Int,
    lessons: List<LessonDomain>,
    completedLessonIds: List<String>,
    nextPartByLesson: Map<String, Int>,
    nextLessonId: String?,
    completedInUnit: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onLessonClick: (String, Int) -> Unit
) {
    item(key = "unit_header_${unit.id}") {
        UnitHeader(
            unit = unit,
            unitNumber = unitNumber,
            completedLessons = completedInUnit,
            totalLessons = lessons.size,
            isExpanded = isExpanded,
            onToggle = onToggle,
            modifier = Modifier.padding(top = if (unitNumber > 1) 12.dp else 0.dp)
        )
    }

    item(key = "unit_lessons_${unit.id}") {
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 2.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                lessons.forEachIndexed { lessonIndex, lesson ->
                    val completedPartCount = if (completedLessonIds.contains(lesson.id)) {
                        lesson.partCount
                    } else {
                        nextPartByLesson[lesson.id] ?: 0
                    }

                    LessonRow(
                        lesson = lesson,
                        lessonNumber = lessonIndex + 1,
                        isCompleted = completedLessonIds.contains(lesson.id),
                        isNext = lesson.id == nextLessonId,
                        onClick = {
                            val targetPart = if (completedLessonIds.contains(lesson.id)) {
                                0 // re-read from beginning
                            } else {
                                nextPartByLesson[lesson.id] ?: 0
                            }
                            onLessonClick(lesson.id, targetPart)
                        },
                        completedPartCount = completedPartCount
                    )
                    // Subtle divider between rows — not after last
                    if (lessonIndex < lessons.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 42.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

// ── Compact subject progress strip ────────────────────────────────────────────

@Composable
private fun SubjectProgressStrip(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    if (total == 0) return

    val progress = completed.toFloat() / total
    val isComplete = completed >= total

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Progress bar + count
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(MaterialTheme.shapes.small),
                color = if (isComplete) Success else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )
            Text(
                text = "$completed من $total درس",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(16.dp))

        // Percent badge
        Text(
            text = if (isComplete) "✓ مكتمل"
            else "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isComplete) Success else MaterialTheme.colorScheme.primary
        )
    }
}