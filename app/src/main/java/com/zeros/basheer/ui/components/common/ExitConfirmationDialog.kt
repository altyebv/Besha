package com.zeros.basheer.ui.components.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Represents the context in which the user is trying to leave.
 * Controls the tone — a lesson is low-stakes; an exam is high-stakes.
 */
enum class ExitContext {
    LESSON,     // Progress is saved, low stakes
    PRACTICE,   // Session can be resumed or abandoned
    EXAM,       // High stakes — time keeps running / can't return
}

/**
 * A reusable, context-aware exit confirmation dialog.
 *
 * Usage:
 * ```
 * ExitConfirmationDialog(
 *     context = ExitContext.LESSON,
 *     onConfirm = { viewModel.pauseTimeTracking(); onBackClick() },
 *     onDismiss = { /* no-op */ }
 * )
 * ```
 */
@Composable
fun ExitConfirmationDialog(
    context: ExitContext,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    // Override defaults when needed (e.g., mid-exam with time remaining)
    titleOverride: String? = null,
    bodyOverride: String? = null,
    confirmLabelOverride: String? = null,
) {
    val strings = exitStrings(context)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        ExitDialogContent(
            icon = strings.icon,
            iconTint = strings.iconTint,
            iconBackgroundColor = strings.iconBackgroundColor,
            title = titleOverride ?: strings.title,
            body = bodyOverride ?: strings.body,
            confirmLabel = confirmLabelOverride ?: strings.confirmLabel,
            dismissLabel = strings.dismissLabel,
            confirmIsDestructive = strings.confirmIsDestructive,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Internal composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExitDialogContent(
    icon: ImageVector,
    iconTint: Color,
    iconBackgroundColor: Color,
    title: String,
    body: String,
    confirmLabel: String,
    dismissLabel: String,
    confirmIsDestructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Icon bubble ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Title ─────────────────────────────────────────────────────
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Body ──────────────────────────────────────────────────────
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Buttons ───────────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary action — confirm exit (potentially destructive)
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (confirmIsDestructive)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = confirmLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Secondary action — stay / keep going
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = dismissLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// String & style configuration per context
// ─────────────────────────────────────────────────────────────────────────────

private data class ExitDialogStrings(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackgroundColor: Color,
    val title: String,
    val body: String,
    val confirmLabel: String,
    val dismissLabel: String,
    val confirmIsDestructive: Boolean,
)

@Composable
private fun exitStrings(context: ExitContext): ExitDialogStrings {
    return when (context) {

        ExitContext.LESSON -> ExitDialogStrings(
            icon = Icons.Default.ExitToApp,
            iconTint = MaterialTheme.colorScheme.primary,
            iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            title = "مغادرة الدرس؟",
            body = "سيتم حفظ تقدمك تلقائياً.\nيمكنك الاستمرار من حيث توقفت في أي وقت.",
            confirmLabel = "مغادرة",
            dismissLabel = "متابعة القراءة",
            confirmIsDestructive = false
        )

        ExitContext.PRACTICE -> ExitDialogStrings(
            icon = Icons.Default.ExitToApp,
            iconTint = MaterialTheme.colorScheme.secondary,
            iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            title = "إنهاء جلسة التدريب؟",
            body = "ستُحفظ إجاباتك حتى الآن.\nلن تتمكن من العودة إلى هذه الجلسة.",
            confirmLabel = "إنهاء الجلسة",
            dismissLabel = "مواصلة التدريب",
            confirmIsDestructive = false
        )

        ExitContext.EXAM -> ExitDialogStrings(
            icon = Icons.Default.ExitToApp,
            iconTint = MaterialTheme.colorScheme.error,
            iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
            title = "مغادرة الامتحان؟",
            body = "تحذير: سيستمر العداد في العمل.\nمغادرة الامتحان قد تُسجَّل كمخالفة.",
            confirmLabel = "مغادرة على أي حال",
            dismissLabel = "العودة للامتحان",
            confirmIsDestructive = true
        )
    }
}