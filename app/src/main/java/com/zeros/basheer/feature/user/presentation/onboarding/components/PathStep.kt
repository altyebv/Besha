package com.zeros.basheer.feature.user.presentation.onboarding.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeros.basheer.feature.subject.data.entity.StudentPath

// ── Track definitions ─────────────────────────────────────────────────────────

private data class TrackOption(val key: String, val emoji: String, val label: String)

private val SCIENCE_TRACKS = listOf(
    TrackOption("BIOLOGY",      "🧬", "أحياء"),
    TrackOption("ARCHITECTURE", "📐", "علوم هندسية"),
    TrackOption("COMPUTER",     "💻", "حاسوب"),
)

private val LITERARY_TRACKS = listOf(
    TrackOption("MILITARY",  "🎖️", "علوم عسكرية"),
    TrackOption("ISLAMIC",   "☪️", "دراسات إسلامية"),
)

// ── Main composable ───────────────────────────────────────────────────────────

@Composable
fun PathStep(
    name: String,
    selectedPath: StudentPath?,
    selectedTrack: String?,
    isSaving: Boolean,
    onPathSelected: (StudentPath) -> Unit,
    onTrackSelected: (String) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "أهلاً، $name!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ما مسارك الدراسي؟",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── Path cards ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PathCard(
                emoji = "🔬",
                title = "العلمي",
                subjects = "فيزياء · كيمياء · رياضيات",
                path = StudentPath.SCIENCE,
                isSelected = selectedPath == StudentPath.SCIENCE,
                selectedColor = Color(0xFF1976D2),
                onClick = { onPathSelected(StudentPath.SCIENCE) },
                modifier = Modifier.weight(1f)
            )
            PathCard(
                emoji = "📖",
                title = "الأدبي",
                subjects = "تاريخ · جغرافيا · فلسفة",
                path = StudentPath.LITERARY,
                isSelected = selectedPath == StudentPath.LITERARY,
                selectedColor = Color(0xFF388E3C),
                onClick = { onPathSelected(StudentPath.LITERARY) },
                modifier = Modifier.weight(1f)
            )
        }

        // ── Common subjects note ───────────────────────────────────────────────
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "مواد مشتركة للكل: عربي · إنجليزي · إسلامية · تربية عسكرية",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        // ── Track selection (appears after path chosen) ────────────────────────
        AnimatedVisibility(
            visible = selectedPath != null,
            enter = fadeIn() + expandVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(28.dp))

                val tracks = if (selectedPath == StudentPath.SCIENCE) SCIENCE_TRACKS
                else LITERARY_TRACKS
                val accentColor = if (selectedPath == StudentPath.SCIENCE) Color(0xFF1976D2)
                else Color(0xFF388E3C)

                Text(
                    text = "اختار مادتك السابعة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "المادة الاختيارية التي تدرسها",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                tracks.forEach { track ->
                    TrackRow(
                        track = track,
                        isSelected = selectedTrack == track.key,
                        accentColor = accentColor,
                        onClick = { onTrackSelected(track.key) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Navigation ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !isSaving
            ) { Text("رجوع") }

            Button(
                onClick = onComplete,
                modifier = Modifier.weight(2f).height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = selectedPath != null && selectedTrack != null && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "التالي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── PathCard ──────────────────────────────────────────────────────────────────

@Composable
private fun PathCard(
    emoji: String,
    title: String,
    subjects: String,
    path: StudentPath,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(200), label = "border"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200), label = "container"
    )

    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.8f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 44.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subjects,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// ── TrackRow ──────────────────────────────────────────────────────────────────

@Composable
private fun TrackRow(
    track: TrackOption,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(180), label = "track_border"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(180), label = "track_bg"
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = track.emoji, fontSize = 24.sp)
            Text(
                text = track.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}