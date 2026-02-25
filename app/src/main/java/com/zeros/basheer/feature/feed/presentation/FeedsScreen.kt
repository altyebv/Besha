package com.zeros.basheer.feature.feed.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.core.ui.theme.BackgroundDark
import com.zeros.basheer.core.ui.theme.Primary
import com.zeros.basheer.core.ui.theme.Secondary
import com.zeros.basheer.core.ui.theme.Success
import com.zeros.basheer.feature.feed.domain.model.CardInteractionState
import com.zeros.basheer.ui.components.feeds.FeedCardRenderer
import com.zeros.basheer.ui.screens.main.components.foundation.MainColors
import kotlinx.coroutines.launch

// Always dark — feeds are immersive, like a night-mode reading experience
private val FeedsBg = Color(0xFF0F0D09)
private val FeedsOnBg = Color.White.copy(alpha = 0.92f)
private val FeedsSubtle = Color.White.copy(alpha = 0.35f)

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

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FeedsBg)
    ) {
        when {
            state.isLoading -> {
                LoadingContent(modifier = Modifier.align(Alignment.Center))
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
                // ─── Pager ────────────────────────────────────────────────
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
                        interactionState = if (isCurrentPage) state.cardInteractionState
                        else CardInteractionState.Idle,
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

                // ─── Subject-colored progress bar ─────────────────────────
                val currentCard = state.feedCards.getOrNull(pagerState.currentPage)
                val accentColor = currentCard?.let {
                    MainColors.subjectColorByName(it.subjectName, 0)
                } ?: Primary

                val progress = (pagerState.currentPage + 1).toFloat() / state.feedCards.size

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.TopCenter)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = progress)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(accentColor, accentColor.copy(alpha = 0.7f))
                                )
                            )
                    )
                }

                // ─── Close button (top-start for RTL) ────────────────────
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 4.dp, top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = FeedsSubtle
                    )
                }

                // ─── Card counter pill ────────────────────────────────────
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.09f)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${state.feedCards.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FeedsSubtle,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Loading
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier,
        color = Primary
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyFeedsContent(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📚", fontSize = 56.sp)
        Text(
            text = "لا توجد بطاقات متاحة",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = FeedsOnBg
        )
        Text(
            text = "ابدأ بدراسة بعض الدروس أولاً",
            style = MaterialTheme.typography.bodyMedium,
            color = FeedsSubtle,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onClose,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
            border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.5f))
        ) {
            Text("رجوع")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Session complete — celebration screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SessionCompleteContent(
    cardsViewed: Int,
    correctAnswers: Int,
    wrongAnswers: Int,
    onRestart: () -> Unit,
    onClose: () -> Unit
) {
    val totalAnswered = correctAnswers + wrongAnswers
    val accuracy = if (totalAnswered > 0) (correctAnswers * 100) / totalAnswered else 100

    // Pulsing emoji scale
    val infiniteTransition = rememberInfiniteTransition(label = "celebrate")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emojiScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1500),   // deep amber-black
                        FeedsBg
                    )
                )
            )
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (accuracy >= 80) "🏆" else "💪",
            fontSize = 72.sp,
            modifier = Modifier.scale(emojiScale)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = if (accuracy >= 80) "ممتاز!" else "أحسنت!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Primary
        )

        Text(
            text = "أكملت جلسة المراجعة",
            style = MaterialTheme.typography.bodyLarge,
            color = FeedsSubtle,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(40.dp))

        // Stat cards in a row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatPill(
                label = "المراجعة",
                value = "$cardsViewed",
                color = Primary,
                modifier = Modifier.weight(1f)
            )
            StatPill(
                label = "صحيح",
                value = "$correctAnswers",
                color = Success,
                modifier = Modifier.weight(1f)
            )
            StatPill(
                label = "خطأ",
                value = "$wrongAnswers",
                color = Secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Accuracy bar
        AccuracyBar(accuracy = accuracy)

        Spacer(Modifier.height(48.dp))

        // Actions
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = Color(0xFF1C1917)
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("جلسة جديدة", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = FeedsSubtle),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            shape = MaterialTheme.shapes.large
        ) {
            Text("رجوع للرئيسية")
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AccuracyBar(accuracy: Int) {
    val accuracyColor = when {
        accuracy >= 80 -> Success
        accuracy >= 50 -> Primary
        else -> Secondary
    }

    // Animated fill
    val animatedFraction by animateFloatAsState(
        targetValue = accuracy / 100f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "accuracyFill"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "نسبة النجاح",
                style = MaterialTheme.typography.labelMedium,
                color = FeedsSubtle
            )
            Text(
                text = "$accuracy%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accuracyColor
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedFraction)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(accuracyColor, accuracyColor.copy(alpha = 0.6f))
                        )
                    )
            )
        }
    }
}