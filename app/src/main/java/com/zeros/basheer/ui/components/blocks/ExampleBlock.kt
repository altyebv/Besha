package com.zeros.basheer.ui.components.blocks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.domain.model.BlockMetadata
import com.zeros.basheer.domain.model.BlockUiModel

/**
 * Renders a worked example block in two modes:
 *
 * **Static** — [BlockMetadata.Example.interactive] = false (or no metadata).
 *   Content is shown as a single body of text.
 *
 * **Interactive** — [BlockMetadata.Example.interactive] = true.
 *   Steps are revealed one at a time. Each tap on "الخطوة التالية" animates
 *   the next step into view. All revealed steps stay visible, accumulating
 *   downward so the student can follow the full chain of reasoning.
 *   A progress pill (e.g. "٢ / ٤") tracks position.
 */
@Composable
fun ExampleBlock(
    block: BlockUiModel,
    modifier: Modifier = Modifier
) {
    val exampleMeta = block.metadata as? BlockMetadata.Example

    if (exampleMeta?.interactive == true && exampleMeta.steps.isNotEmpty()) {
        InteractiveExampleBlock(
            title = block.caption ?: "مثال",
            steps = exampleMeta.steps,
            modifier = modifier
        )
    } else {
        StaticExampleBlock(
            title = block.caption ?: "مثال",
            content = block.content,
            modifier = modifier
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATIC
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StaticExampleBlock(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExampleHeader(title = title)
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INTERACTIVE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InteractiveExampleBlock(
    title: String,
    steps: List<String>,
    modifier: Modifier = Modifier
) {
    // revealedCount = how many steps are currently visible (starts at 1)
    var revealedCount by remember { mutableIntStateOf(1) }
    val totalSteps = steps.size
    val allRevealed = revealedCount >= totalSteps

    val borderColor by animateColorAsState(
        targetValue = if (allRevealed)
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
        else
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
        animationSpec = tween(400),
        label = "exampleBorder"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header row with title + progress pill ───────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExampleHeader(title = title)
                StepProgressPill(
                    current = revealedCount,
                    total = totalSteps
                )
            }

            // ── Step dots row ───────────────────────────────────────────────
            StepDotsRow(
                total = totalSteps,
                revealed = revealedCount
            )

            // ── Steps (each animates in) ────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                steps.forEachIndexed { index, stepText ->
                    AnimatedVisibility(
                        visible = index < revealedCount,
                        enter = expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            expandFrom = Alignment.Top
                        ) + fadeIn(animationSpec = tween(300))
                    ) {
                        StepRow(index = index, text = stepText, isLatest = index == revealedCount - 1)
                    }
                }
            }

            // ── CTA button ──────────────────────────────────────────────────
            if (!allRevealed) {
                NextStepButton(
                    onClick = { revealedCount++ },
                    stepNumber = revealedCount + 1,
                    total = totalSteps
                )
            } else {
                CompletionBadge()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SUB-COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExampleHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.School,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun StepProgressPill(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = toArabicNumerals(current),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 11.sp
            )
            Text(
                text = "/",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
            Text(
                text = toArabicNumerals(total),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun StepDotsRow(
    total: Int,
    revealed: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(total) { index ->
            val isFilled = index < revealed
            val dotColor by animateColorAsState(
                targetValue = if (isFilled)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                animationSpec = tween(300),
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(dotColor)
            )
        }
    }
}

@Composable
private fun StepRow(
    index: Int,
    text: String,
    isLatest: Boolean,
    modifier: Modifier = Modifier
) {
    val accentAlpha = if (isLatest) 1f else 0.55f

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Step number badge
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isLatest) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = toArabicNumerals(index + 1),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (isLatest)
                    MaterialTheme.colorScheme.onSecondary
                else
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = accentAlpha),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NextStepButton(
    onClick: () -> Unit,
    stepNumber: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "الخطوة ${toArabicNumerals(stepNumber)} من ${toArabicNumerals(total)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CompletionBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "✓  اكتمل المثال",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Converts an [Int] to its Eastern Arabic numeral string.
 * e.g. 3 → "٣"
 */
private fun toArabicNumerals(number: Int): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { c ->
        if (c.isDigit()) arabicDigits[c - '0'] else c
    }.joinToString("")
}