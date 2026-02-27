package com.zeros.basheer.feature.user.presentation.onboarding.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class StudyOption(val minutes: Int, val label: String)

private val STUDY_OPTIONS = listOf(
    StudyOption(30,  "٣٠ د"),
    StudyOption(60,  "ساعة"),
    StudyOption(120, "ساعتان"),
    StudyOption(180, "٣+ ساعات")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesStep(
    dailyStudyMinutes: Int,
    reminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    onDailyStudyMinutesChanged: (Int) -> Unit,
    onReminderEnabledChanged: (Boolean) -> Unit,
    onReminderTimeChanged: (Int, Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = reminderHour,
        initialMinute = reminderMinute,
        is24Hour = false
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "كيف نساعدك؟",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "اضبط هدفك اليومي وتذكيراتك",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── Daily goal label ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "هدف الدراسة اليومي",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Study time pills ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            STUDY_OPTIONS.forEach { option ->
                val selected = dailyStudyMinutes == option.minutes
                FilterChip(
                    selected = selected,
                    onClick = { onDailyStudyMinutesChanged(option.minutes) },
                    label = {
                        Text(
                            text = option.label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(32.dp))

        // ── Reminders toggle ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ذكّرني بالمذاكرة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "إشعار يومي لتحفيزك",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = reminderEnabled,
                onCheckedChange = onReminderEnabledChanged
            )
        }

        // ── Time picker trigger (only when reminders on) ──────────────────────
        AnimatedVisibility(visible = reminderEnabled) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "وقت التذكير",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        val amPm = if (reminderHour < 12) "ص" else "م"
                        val displayHour = when {
                            reminderHour == 0    -> 12
                            reminderHour <= 12   -> reminderHour
                            else                 -> reminderHour - 12
                        }
                        Text(
                            text = "%d:%02d %s".format(displayHour, reminderMinute, amPm),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) { Text("رجوع") }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(2f).height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "التالي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // ── Time picker dialog ────────────────────────────────────────────────────
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onReminderTimeChanged(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("تأكيد") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("إلغاء") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}