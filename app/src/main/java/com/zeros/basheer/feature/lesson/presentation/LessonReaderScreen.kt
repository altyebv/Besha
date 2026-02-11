package com.zeros.basheer.feature.lesson.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zeros.basheer.domain.model.LessonContent
//import com.zeros.basheer.feature.lesson.domain.model.LessonContent
import com.zeros.basheer.ui.components.blocks.BlockRenderer
import com.zeros.basheer.ui.components.common.ConceptModal
import com.zeros.basheer.ui.components.common.LessonCompleteCard
import com.zeros.basheer.ui.components.common.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonReaderScreen(
    lessonId: String,
    onBackClick: () -> Unit,
    onNextLesson: (() -> Unit)? = null,
    viewModel: LessonReaderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    
    // Track lifecycle for time tracking
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startTimeTracking()
                Lifecycle.Event.ON_PAUSE -> viewModel.pauseTimeTracking()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Calculate scroll progress
    LaunchedEffect(listState.firstVisibleItemIndex, listState.layoutInfo.totalItemsCount) {
        if (listState.layoutInfo.totalItemsCount > 0) {
            val totalItems = listState.layoutInfo.totalItemsCount
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: 0
            val progress = (lastVisibleIndex + 1).toFloat() / totalItems
            viewModel.onScrollProgressChanged(progress.coerceIn(0f, 1f))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.lessonContent?.title ?: "جاري التحميل...",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleProgressOverlay() }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "المزيد"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    ErrorContent(
                        error = state.error!!,
                        onBackClick = onBackClick,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.lessonContent != null -> {
                    LessonContent(
                        lessonContent = state.lessonContent!!,
                        hasReachedEnd = state.hasReachedEnd,
                        isCompleted = state.progress?.completed == true,
                        readingTimeSeconds = state.readingTimeSeconds,
                        onConceptClick = viewModel::onConceptClick,
                        onMarkComplete = viewModel::markAsCompleted,
                        onNextLesson = onNextLesson,
                        listState = listState
                    )
                    
                    // Progress indicator at bottom
                    LinearProgressIndicator(
                        progress = { state.scrollProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
            
            // Concept Modal
            if (state.activeConcept != null) {
                ConceptModal(
                    concept = state.activeConcept,
                    onDismiss = viewModel::dismissConceptModal
                )
            }
        }
    }
}

@Composable
private fun LessonContent(
    lessonContent: LessonContent,
    hasReachedEnd: Boolean,
    isCompleted: Boolean,
    readingTimeSeconds: Long,
    onConceptClick: (String) -> Unit,
    onMarkComplete: () -> Unit,
    onNextLesson: (() -> Unit)?,
    listState: LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Lesson summary if available
        lessonContent.summary?.let { summary ->
            item(key = "summary") {
                LessonSummaryCard(
                    summary = summary,
                    estimatedMinutes = lessonContent.estimatedMinutes
                )
            }
        }
        
        // Sections with their blocks
        lessonContent.sections.forEach { section ->
            item(key = "section_header_${section.id}") {
                SectionHeader(section = section)
            }
            
            items(
                items = section.blocks,
                key = { block -> block.id }
            ) { block ->
                BlockRenderer(
                    block = block,
                    onConceptClick = onConceptClick
                )
            }
        }
        
        // Completion card at the end
        if (hasReachedEnd) {
            item(key = "complete_card") {
                LessonCompleteCard(
                    lessonTitle = lessonContent.title,
                    readingTimeSeconds = readingTimeSeconds,
                    isAlreadyCompleted = isCompleted,
                    onMarkComplete = onMarkComplete,
                    onNextLesson = onNextLesson
                )
            }
        }
    }
}

@Composable
private fun LessonSummaryCard(
    summary: String,
    estimatedMinutes: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$estimatedMinutes دقيقة",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "حدث خطأ",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onBackClick) {
            Text("رجوع")
        }
    }
}
