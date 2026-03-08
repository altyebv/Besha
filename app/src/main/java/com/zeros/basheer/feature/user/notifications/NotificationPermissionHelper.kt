package com.zeros.basheer.feature.user.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// ─────────────────────────────────────────────────────────────────────────────
// NOTIFICATION PERMISSION HELPER
//
// Centralises the POST_NOTIFICATIONS runtime permission (API 33+) into a
// single reusable composable. Both OnboardingScreen and SettingsScreen use
// this instead of duplicating launcher / permission check code.
//
// Exact-alarm permission (SCHEDULE_EXACT_ALARM) is a UX enhancement only —
// ReminderScheduler falls back to setWindow() when it isn't granted, so we
// don't block the user over it here. Settings surfaces it separately for
// users who care about precise timing.
//
// Usage:
//   val notifPermission = rememberNotificationPermission(
//       onGranted = { viewModel.setNotificationsEnabled(true) },
//       onDenied  = { /* optional: show snackbar */ }
//   )
//   ...
//   Switch(onCheckedChange = { if (it) notifPermission.request() else disable() })
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Returned by [rememberNotificationPermission].
 *
 * @param isGranted   True if the app can currently post notifications.
 *                    Always true on API < 33 (no runtime permission required).
 * @param request     Call this when the user enables notifications.
 *                    On API 33+ it shows the system permission dialog.
 *                    On lower API it calls [onGranted] directly.
 */
@Stable
class NotificationPermission(
    val isGranted: Boolean,
    val request: () -> Unit,
)

/**
 * Creates and remembers a [NotificationPermission] that survives recompositions.
 *
 * @param onGranted Called after permission is confirmed granted (or on API < 33
 *                  where no dialog is needed).
 * @param onDenied  Called if the user explicitly denies the permission dialog.
 *                  Default is a no-op — callers can show a snackbar / revert toggle.
 */
@Composable
fun rememberNotificationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit = {},
): NotificationPermission {

    val context = LocalContext.current

    // Compute initial granted state synchronously — no recomposition overhead.
    val isGranted = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true  // permission not required below API 33
        }
    }

    // Keep a stable mutable reference so the launcher callback can reach it
    // without recreating the launcher on recomposition.
    val onGrantedRef = rememberUpdatedState(onGranted)
    val onDeniedRef  = rememberUpdatedState(onDenied)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onGrantedRef.value() else onDeniedRef.value()
    }

    val request: () -> Unit = remember {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Only launch the system dialog — result delivered via launcher callback.
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // No runtime permission needed; confirm directly.
                onGrantedRef.value()
            }
        }
    }

    return remember(isGranted) { NotificationPermission(isGranted, request) }
}