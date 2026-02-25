package com.zeros.basheer.feature.practice.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CorrectGreen  = Color(0xFF22C55E)
private val IncorrectRed  = Color(0xFFEF4444)
private val PanelBg       = Color(0xFF18160F)   // Warm near-black, slightly lighter than feed bg
private val PanelBgWrong  = Color(0xFF1A0F0F)   // Warm red-tinted for wrong
private val PanelBgRight  = Color(0xFF0F1A12)   // Warm green-tinted for correct

/**
 * Slide-up answer panel for feeds — appears above the bottom of the card,
 * covers ~45% of the screen. Never affects parent layout height.
 *
 * Shows:
 *   • Verdict row  (icon + "إجابة صحيحة / خاطئة")
 *   • Correct answer chip  (only when wrong, so student sees the right answer clearly)
 *   • Explanation text  (if available)
 *   • Continue button
 */
@Composable
fun FeedAnswerPanel(
    isCorrect: Boolean,
    correctAnswerLabel: String?,
    explanation: String?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isCorrect) CorrectGreen else IncorrectRed
    val panelBg     = if (isCorrect) PanelBgRight else PanelBgWrong
    val shape       = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(panelBg, PanelBg),
                    startY = 0f,
                    endY = 300f
                )
            )
            // Colored top border line drawn behind content
            .drawBehind {
                drawRect(
                    color = accentColor,
                    size = androidx.compose.ui.geometry.Size(size.width, 2.dp.toPx()),
                    topLeft = androidx.compose.ui.geometry.Offset.Zero
                )
            }
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Verdict row ────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Colored circle with icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = if (isCorrect) "إجابة صحيحة!" else "إجابة خاطئة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                if (!isCorrect && correctAnswerLabel != null) {
                    Text(
                        text = "الإجابة الصحيحة ↓",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.40f)
                    )
                }
            }
        }

        // ── Correct answer chip (only when wrong) ─────────────────────────
        if (!isCorrect && correctAnswerLabel != null) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = CorrectGreen.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = CorrectGreen.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = CorrectGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = correctAnswerLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CorrectGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Explanation ────────────────────────────────────────────────────
        if (!explanation.isNullOrBlank()) {
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Start,
                maxLines = 3
            )
        }

        // ── Continue button ────────────────────────────────────────────────
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = if (isCorrect) Color(0xFF052E16) else Color(0xFF450A0A)
            ),
            shape = MaterialTheme.shapes.large,
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(
                text = "متابعة",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}