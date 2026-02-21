package com.zeros.basheer.feature.user.presentation.onboarding.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun PathStep(
    name: String,
    selectedPath: StudentPath?,
    isSaving: Boolean,
    onPathSelected: (StudentPath) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "أهلاً، ${name}!",
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

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PathCard(
                emoji = "🔬",
                title = "العلمي",
                subtitle = "فيزياء · كيمياء\nأحياء · رياضيات",
                path = StudentPath.SCIENCE,
                isSelected = selectedPath == StudentPath.SCIENCE,
                selectedColor = Color(0xFF1976D2),
                onClick = { onPathSelected(StudentPath.SCIENCE) },
                modifier = Modifier.weight(1f)
            )

            PathCard(
                emoji = "📖",
                title = "الأدبي",
                subtitle = "تاريخ · جغرافيا\naقتصاد · فلسفة",
                path = StudentPath.LITERARY,
                isSelected = selectedPath == StudentPath.LITERARY,
                selectedColor = Color(0xFF388E3C),
                onClick = { onPathSelected(StudentPath.LITERARY) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !isSaving
            ) {
                Text("رجوع")
            }

            Button(
                onClick = onComplete,
                modifier = Modifier
                    .weight(2f)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = selectedPath != null && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "ابدأ الدراسة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PathCard(
    emoji: String,
    title: String,
    subtitle: String,
    path: StudentPath,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(200),
        label = "border"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor.copy(alpha = 0.08f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "container"
    )

    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.75f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 48.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) selectedColor
                else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}