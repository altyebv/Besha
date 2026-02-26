package com.zeros.basheer.feature.user.presentation.settings.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// PERMISSION RATIONALE DIALOG
// Generic two-button dialog used for both POST_NOTIFICATIONS and
// SCHEDULE_EXACT_ALARM rationale flows.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * @param icon         Icon shown at the top of the dialog.
 * @param title        Bold title text.
 * @param body         Explanatory body text shown in the content area.
 * @param confirmLabel Label for the positive action button.
 * @param onDismiss    Called when the user taps "لاحقاً" or dismisses the dialog.
 * @param onConfirm    Called when the user taps [confirmLabel].
 */
@Composable
internal fun PermissionRationaleDialog(
    icon: ImageVector,
    title: String,
    body: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(32.dp)
            )
        },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text  = {
            Text(
                text  = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("لاحقاً") }
        }
    )
}