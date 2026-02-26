package com.zeros.basheer.feature.user.presentation.settings

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeros.basheer.feature.user.presentation.settings.components.*

// ─────────────────────────────────────────────────────────────────────────────
// SETTINGS SCREEN — orchestrator only
// All visual components live in settings/components/
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state   by viewModel.state.collectAsState()

    // ── Dialog visibility ─────────────────────────────────────────────────────
    var showGoalDialog          by remember { mutableStateOf(false) }
    var showAboutDialog         by remember { mutableStateOf(false) }
    var showTimeDialog          by remember { mutableStateOf(false) }
    var showPermRationale       by remember { mutableStateOf(false) }
    var showExactAlarmRationale by remember { mutableStateOf(false) }

    // ── Permission launchers ──────────────────────────────────────────────────

    // Returns from the system exact-alarm settings page
    val exactAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Enable regardless — ReminderScheduler uses setWindow fallback if still denied
            viewModel.setNotificationsEnabled(true)
        }
    }

    // System POST_NOTIFICATIONS permission dialog (Android 13+)
    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!am.canScheduleExactAlarms()) {
                    showExactAlarmRationale = true
                    return@rememberLauncherForActivityResult
                }
            }
            viewModel.setNotificationsEnabled(true)
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showGoalDialog) {
        DailyGoalDialog(
            currentGoal = state.dailyGoalMinutes,
            onDismiss   = { showGoalDialog = false },
            onConfirm   = { minutes ->
                viewModel.setDailyGoalMinutes(minutes)
                showGoalDialog = false
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showTimeDialog) {
        ReminderTimeDialog(
            currentHour   = state.reminderHour,
            currentMinute = state.reminderMinute,
            onDismiss     = { showTimeDialog = false },
            onConfirm     = { hour, minute ->
                viewModel.setReminderTime(hour, minute)
                showTimeDialog = false
            }
        )
    }

    if (showPermRationale) {
        PermissionRationaleDialog(
            icon         = Icons.Outlined.Notifications,
            title        = "السماح بالإشعارات",
            body         = "يحتاج بشير إذن الإشعارات لتذكيرك بمراجعة دروسك يومياً. لن نرسل لك إلا تذكيراً واحداً في اليوم.",
            confirmLabel = "منح الإذن",
            onDismiss    = { showPermRationale = false },
            onConfirm    = {
                showPermRationale = false
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                        notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        if (!am.canScheduleExactAlarms()) showExactAlarmRationale = true
                        else viewModel.setNotificationsEnabled(true)
                    }
                    else -> viewModel.setNotificationsEnabled(true)
                }
            }
        )
    }

    if (showExactAlarmRationale) {
        PermissionRationaleDialog(
            icon         = Icons.Outlined.Schedule,
            title        = "التنبيهات الدقيقة",
            body         = "لكي يصلك التذكير في وقته بالضبط، يحتاج بشير إذن \"التنبيهات والتذكيرات\" من إعدادات الجهاز.\n\nبدونه سيصلك التذكير في غضون 10 دقائق من الوقت المختار.",
            confirmLabel = "فتح الإعدادات",
            onDismiss    = {
                // User skipped — enable anyway, scheduler uses inexact fallback
                showExactAlarmRationale = false
                viewModel.setNotificationsEnabled(true)
            },
            onConfirm    = {
                showExactAlarmRationale = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    exactAlarmLauncher.launch(
                        Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }
            }
        )
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Appearance ────────────────────────────────────────────────────
            SettingsGroup(title = "المظهر") {
                ToggleSettingsRow(
                    icon            = Icons.Outlined.DarkMode,
                    iconTint        = Color(0xFF6366F1),
                    title           = "الوضع الداكن",
                    subtitle        = if (state.isDarkMode) "مفعّل" else "معطّل",
                    checked         = state.isDarkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
            }

            // ── Notifications ─────────────────────────────────────────────────
            SettingsGroup(title = "الإشعارات") {
                ToggleSettingsRow(
                    icon     = Icons.Outlined.Notifications,
                    iconTint = Color(0xFFF97316),
                    title    = "تذكير يومي",
                    subtitle = if (state.isNotificationsEnabled) "مفعّل" else "معطّل",
                    checked  = state.isNotificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) showPermRationale = true
                        else viewModel.setNotificationsEnabled(false)
                    }
                )
                if (state.isNotificationsEnabled) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color    = MaterialTheme.colorScheme.outlineVariant
                    )
                    ActionSettingsRow(
                        icon     = Icons.Outlined.Schedule,
                        iconTint = Color(0xFF0EA5E9),
                        title    = "وقت التذكير",
                        subtitle = formatTime(state.reminderHour, state.reminderMinute),
                        onClick  = { showTimeDialog = true }
                    )
                }
            }

            // ── Study ─────────────────────────────────────────────────────────
            SettingsGroup(title = "الدراسة") {
                ActionSettingsRow(
                    icon     = Icons.Outlined.Timer,
                    iconTint = Color(0xFF10B981),
                    title    = "الهدف اليومي",
                    subtitle = "${state.dailyGoalMinutes} دقيقة في اليوم",
                    onClick  = { showGoalDialog = true }
                )
            }

            // ── Account ───────────────────────────────────────────────────────
            SettingsGroup(title = "الحساب") {
                ActionSettingsRow(
                    icon     = Icons.Outlined.Person,
                    iconTint = Color(0xFF3B82F6),
                    title    = "تعديل الملف الشخصي",
                    subtitle = "الاسم، المسار الدراسي",
                    onClick  = onEditProfile
                )
            }

            // ── App ───────────────────────────────────────────────────────────
            SettingsGroup(title = "التطبيق") {
                ActionSettingsRow(
                    icon     = Icons.Outlined.Info,
                    iconTint = Color(0xFF6B7280),
                    title    = "حول التطبيق",
                    subtitle = "بشير — الإصدار 0.1.0",
                    onClick  = { showAboutDialog = true }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color    = MaterialTheme.colorScheme.outlineVariant
                )
                ActionSettingsRow(
                    icon     = Icons.Outlined.StarOutline,
                    iconTint = Color(0xFFF59E0B),
                    title    = "قيّم التطبيق",
                    subtitle = "شاركنا رأيك في المتجر",
                    onClick  = { /* TODO: Play Store deep link */ }
                )
            }
        }
    }
}