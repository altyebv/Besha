package com.zeros.basheer.feature.lesson.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LockOpen
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
import com.zeros.basheer.core.ui.theme.Amber
import com.zeros.basheer.core.ui.theme.AmberContainer
import com.zeros.basheer.core.ui.theme.AmberDeep
import com.zeros.basheer.core.ui.theme.Coral

/**
 * Mini celebration card rendered between two lesson parts.
 *
 * Shows the completed part number, a motivational message, and a CTA
 * that advances the reader to the next part.
 *
 * @param completedPartNumber  1-indexed number of the part just finished (e.g. 1 for Part 1).
 * @param nextPartNumber       1-indexed number of the part about to start.
 * @param totalParts           Total number of parts in this lesson.
 * @param onContinue           Called when the user taps the CTA — triggers [advancePart] in VM.
 */
@Composable
fun PartTransitionCard(
    completedPartNumber: Int,
    nextPartNumber: Int,
    totalParts: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse the check icon once on appearance
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggered = true }
    val iconScale by animateFloatAsState(
        targetValue = if (triggered) 1f else 0.4f,
        animationSpec = tween(durationMillis = 350),
        label = "iconScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Amber.copy(alpha = 0.14f),
                            Coral.copy(alpha = 0.08f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // ── Check icon ────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .scale(iconScale)
                        .clip(CircleShape)
                        .background(Amber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Amber,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // ── Text ──────────────────────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "أتممت الجزء $completedPartNumber! 🎯",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberDeep,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "الجزء $nextPartNumber من $totalParts في انتظارك",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // ── Part progress pills (mini recap) ──────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(totalParts) { index ->
                        val isCompleted = index < completedPartNumber
                        val isCurrent = index == completedPartNumber
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isCurrent) 28.dp else 20.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> Amber
                                        isCurrent -> Amber.copy(alpha = 0.35f)
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    }
                                )
                        )
                    }
                }

                // ── CTA button ────────────────────────────────────────────────
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber,
                        contentColor = AmberDeep
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "ابدأ الجزء $nextPartNumber",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}