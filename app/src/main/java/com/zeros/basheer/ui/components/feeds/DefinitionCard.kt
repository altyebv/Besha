package com.zeros.basheer.ui.components.feeds

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zeros.basheer.feature.feed.domain.model.FeedCard

/**
 * Card for definitions, facts, rules.
 * Dark-first: large centered text with a soft subject-color radial glow behind it.
 * Tap anywhere to continue.
 */
@Composable
fun DefinitionCard(
    card: FeedCard,
    subjectColor: Color,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle pulsing glow
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.07f,
        targetValue = 0.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onContinue
            ),
        contentAlignment = Alignment.Center
    ) {
        // Radial glow behind text
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.Center)
                .drawBehind {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            asFrameworkPaint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.TRANSPARENT
                                setShadowLayer(
                                    size.minDimension / 2f,
                                    0f, 0f,
                                    android.graphics.Color.argb(
                                        (glowAlpha * 255).toInt(),
                                        (subjectColor.red * 255).toInt(),
                                        (subjectColor.green * 255).toInt(),
                                        (subjectColor.blue * 255).toInt()
                                    )
                                )
                            }
                        }
                        canvas.drawCircle(
                            center = androidx.compose.ui.geometry.Offset(
                                size.width / 2f, size.height / 2f
                            ),
                            radius = size.minDimension / 2f,
                            paint = paint
                        )
                    }
                }
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            card.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                )
            }

            Text(
                text = card.contentAr,
                style = MaterialTheme.typography.headlineMedium.copy(
                    lineHeight = 46.sp,
                    fontWeight = FontWeight.Medium,
                    fontSize = 26.sp
                ),
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }

        // Pulsing hint at bottom
        Text(
            text = "اضغط للمتابعة",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.30f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}