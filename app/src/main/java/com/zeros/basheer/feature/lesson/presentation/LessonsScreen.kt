package com.zeros.basheer.feature.lesson.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.feature.lesson.domain.model.LessonDomain
import com.zeros.basheer.feature.lesson.presentation.components.cards.LessonCard
import com.zeros.basheer.feature.lesson.presentation.components.cards.OverallProgressCard
import com.zeros.basheer.feature.lesson.presentation.components.cards.UnitHeader
import com.zeros.basheer.feature.lesson.presentation.components.foundation.LessonAnimations
import com.zeros.basheer.feature.lesson.presentation.components.foundation.LessonMetrics
import com.zeros.basheer.feature.lesson.presentation.components.states.EmptyState
import com.zeros.basheer.feature.lesson.presentation.components.states.LoadingState
import com.zeros.basheer.feature.lesson.presentation.components.topbar.LessonsTopBar
import com.zeros.basheer.feature.subject.domain.model.Units
import kotlinx.coroutines.delay

/**
 * Main lessons screen showing all lessons grouped by units.
 *
 * Refactored to use modular components:
 * - LessonsTopBar (search functionality)
 * - OverallProgressCard (progress summary)
 * - UnitHeader (unit sections)
 * - LessonCard (individual lessons)
 * - EmptyState (no lessons/no results)
 * - LoadingState (loading indicator)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    subjectId: String,
    onLessonClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LessonsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Load lessons when subjectId changes
    LaunchedEffect(subjectId) {
        viewModel.loadLessons(subjectId)
    }

    // Filter lessons based on search
    val filteredUnits = remember(state.units, searchQuery) {
        if (searchQuery.isBlank()) {
            state.units
        } else {
            state.units.mapNotNull { (unit, lessons) ->
                val filtered = lessons.filter {
                    it.title.contains(searchQuery, ignoreCase = true)
                }
                if (filtered.isNotEmpty()) unit to filtered else null
            }
        }
    }

    Scaffold(
        topBar = {
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
        }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingState(modifier = Modifier.padding(padding))
            }

            filteredUnits.isEmpty() && searchQuery.isNotBlank() -> {
                EmptyState(
                    icon = Icons.Default.SearchOff,
                    message = "لا توجد نتائج",
                    description = "جرب كلمات بحث مختلفة",
                    modifier = Modifier.padding(padding)
                )
            }

            state.units.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.MenuBook,
                    message = "لا توجد دروس متاحة",
                    description = "سيتم إضافة الدروس قريباً",
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                LessonsContent(
                    units = filteredUnits,
                    completedLessonIds = state.completedLessonIds,
                    onLessonClick = onLessonClick,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

/**
 * Content section with scrollable lesson list.
 * Extracted for clarity and testability.
 */
@Composable
private fun LessonsContent(
    units: List<Pair<Units, List<LessonDomain>>>,
    completedLessonIds: List<String>,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = rememberLazyListState(),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(LessonMetrics.contentPadding),
        verticalArrangement = Arrangement.spacedBy(LessonMetrics.cardSpacing)
    ) {
        // Overall progress card
        item(key = "progress_card") {
            var visible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(LessonAnimations.INITIAL_DELAY)
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = LessonAnimations.progressCardEntry
            ) {
                OverallProgressCard(
                    totalLessons = units.sumOf { it.second.size },
                    completedLessons = completedLessonIds.size,
                    totalUnits = units.size
                )
            }
        }

        // Units with their lessons
        units.forEach { (unit, lessons) ->
            item(key = "unit_${unit.id}") {
                var visible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(LessonAnimations.SECOND_WAVE_DELAY)
                    visible = true
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = LessonAnimations.headerEntry
                ) {
                    UnitHeader(
                        unit = unit,
                        completedLessons = lessons.count { completedLessonIds.contains(it.id) },
                        totalLessons = lessons.size
                    )
                }
            }

            items(
                items = lessons,
                key = { lesson -> lesson.id }
            ) { lesson ->
                var visible by remember { mutableStateOf(false) }
                val index = lessons.indexOf(lesson)

                LaunchedEffect(Unit) {
                    delay(LessonAnimations.cardEntryDelay(index).toLong())
                    visible = true
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = LessonAnimations.cardEntry(index)
                ) {
                    LessonCard(
                        lesson = lesson,
                        isCompleted = completedLessonIds.contains(lesson.id),
                        onClick = { onLessonClick(lesson.id) }
                    )
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(LessonMetrics.sectionSpacing))
        }
    }
}