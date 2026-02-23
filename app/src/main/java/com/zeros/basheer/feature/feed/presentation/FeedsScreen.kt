package com.zeros.basheer.feature.feed.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.feature.feed.domain.model.CardInteractionState
import com.zeros.basheer.ui.components.feeds.FeedCardRenderer
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedsScreen(
    onClose: () -> Unit = {},
    viewModel: FeedsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { state.feedCards.size }
    )
    val coroutineScope = rememberCoroutineScope()

    // Sync pager with viewmodel
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            state.feedCards.isEmpty() -> {
                EmptyFeedsContent(
                    onClose = onClose,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            state.sessionComplete -> {
                SessionCompleteContent(
                    cardsViewed = state.cardsViewed,
                    correctAnswers = state.correctAnswers,
                    wrongAnswers = state.wrongAnswers,
                    onRestart = { viewModel.restartSession() },
                    onClose = onClose
                )
            }

            else -> {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = viewModel.canSwipeToNext(),
                    beyondViewportPageCount = 1
                ) { page ->
                    val card = state.feedCards[page]
                    val isCurrentPage = page == pagerState.currentPage

                    FeedCardRenderer(
                        card = card,
                        interactionState = if (isCurrentPage) state.cardInteractionState else CardInteractionState.Idle,
                        onAnswer = { answer -> viewModel.onAnswer(answer) },
                        onContinue = {
                            if (viewModel.onContinue()) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page + 1)
                                }
                            }
                        },
                        onFlip = { viewModel.onFlip() },
                        onKnewIt = {
                            viewModel.onSelfRate(knew = true)
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(300)
                                if (viewModel.onContinue()) {
                                    pagerState.animateScrollToPage(page + 1)
                                }
                            }
                        },
                        onDidntKnow = {
                            viewModel.onSelfRate(knew = false)
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(300)
                                if (viewModel.onContinue()) {
                                    pagerState.animateScrollToPage(page + 1)
                                }
                            }
                        }
                    )
                }

                // Progress indicator
                LinearProgressIndicator(
                    progress = { (pagerState.currentPage + 1).toFloat() / state.feedCards.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                // Close button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Card counter
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${state.feedCards.size}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFeedsContent(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "لا توجد بطاقات متاحة",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "ابدأ بدراسة بعض الدروس أولاً",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onClose) {
            Text("رجوع")
        }
    }
}

@Composable
private fun SessionCompleteContent(
    cardsViewed: Int,
    correctAnswers: Int,
    wrongAnswers: Int,
    onRestart: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalAnswered = correctAnswers + wrongAnswers
    val accuracy = if (totalAnswered > 0) {
        (correctAnswers * 100) / totalAnswered
    } else {
        100
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "أحسنت!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "أكملت جلسة المراجعة",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Stats
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatRow(label = "البطاقات المراجعة", value = "$cardsViewed")
                StatRow(label = "الإجابات الصحيحة", value = "$correctAnswers", color = MaterialTheme.colorScheme.primary)
                StatRow(label = "الإجابات الخاطئة", value = "$wrongAnswers", color = MaterialTheme.colorScheme.error)
                HorizontalDivider()
                StatRow(label = "نسبة النجاح", value = "$accuracy%", isBold = true)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Actions
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("جلسة جديدة")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("رجوع للرئيسية")
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}