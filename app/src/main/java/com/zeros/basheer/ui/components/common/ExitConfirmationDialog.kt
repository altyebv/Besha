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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class ExitContext {
    LESSON,
    PRACTICE,
    EXAM,
}

/**
 * Exit confirmation dialog.
 *
 * @param forwardPull Optional teaser shown above the buttons — e.g. "الدرس القادم: الجاذبية العالمية".
 *                    Only shown in LESSON context. Nudges the user to stay.
 */
@Composable
fun ExitConfirmationDialog(
    context: ExitContext,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    forwardPull: String? = null,
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
            forwardPull = if (context == ExitContext.LESSON) forwardPull else null,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

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
    forwardPull: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val Amber = Color(0xFFF59E0B)
    val AmberDeep = Color(0xFF78350F)

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
            // Icon bubble
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

            Spacer(Modifier.height(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f
            )

            // ── Forward pull nudge ────────────────────────────────────────────
            if (forwardPull != null) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Amber.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🎯",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = forwardPull,
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberDeep,
                            fontWeight = FontWeight.Medium,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3f
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Buttons — dismiss (stay) is primary action in LESSON context
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Stay = primary
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = dismissLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Exit = secondary / outlined
                OutlinedButton(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (confirmIsDestructive)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = confirmLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

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