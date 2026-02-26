package com.zeros.basheer.feature.quizbank.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.feature.quizbank.presentation.foundation.*

enum class QuizMode { EXAMS, PRACTICE }

@Composable
internal fun ModeSwitcher(
    selected: QuizMode,
    onSelect: (QuizMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgCard)
            .border(1.dp, bgBorder, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ModeTab(
                label = "الامتحانات",
                icon = androidx.compose.material.icons.Icons.Outlined.EmojiEvents,
                active = selected == QuizMode.EXAMS,
                activeColor = AccentExam,
                onClick = { onSelect(QuizMode.EXAMS) },
                modifier = Modifier.weight(1f)
            )
            ModeTab(
                label = "التدريب",
                icon = androidx.compose.material.icons.Icons.Outlined.Bolt,
                active = selected == QuizMode.PRACTICE,
                activeColor = AccentPractice,
                onClick = { onSelect(QuizMode.PRACTICE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeTab(
    label: String,
    icon: ImageVector,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (active) activeColor.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(200), label = "tab_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (active) activeColor.copy(alpha = 0.40f) else Color.Transparent,
        animationSpec = tween(200), label = "tab_border"
    )
    val contentColor by animateColorAsState(
        targetValue = if (active) activeColor else textSecondary,
        animationSpec = tween(200), label = "tab_content"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}