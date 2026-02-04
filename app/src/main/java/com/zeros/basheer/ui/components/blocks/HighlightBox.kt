package com.zeros.basheer.ui.components.blocks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.domain.model.BlockMetadata
import com.zeros.basheer.domain.model.BlockUiModel
import com.zeros.basheer.domain.model.HighlightStyle

@Composable
fun HighlightBox(
    block: BlockUiModel,
    onConceptClick: (conceptId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val style = (block.metadata as? BlockMetadata.HighlightBox)?.style ?: HighlightStyle.NOTE
    val colors = getHighlightColors(style)
    val icon = getHighlightIcon(style)
    val label = getHighlightLabel(style)
    
    val isClickable = block.conceptRef != null
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .then(
                if (isClickable) {
                    Modifier.clickable { block.conceptRef?.let(onConceptClick) }
                } else {
                    Modifier
                }
            ),
        shape = MaterialTheme.shapes.medium,
        color = colors.containerColor,
        border = BorderStroke(1.dp, colors.borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with icon and label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.iconColor
                )
                
                if (isClickable) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "اضغط للتفاصيل",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.iconColor.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Content
            Text(
                text = block.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                ),
                color = colors.contentColor
            )
        }
    }
}

private data class HighlightColors(
    val containerColor: Color,
    val borderColor: Color,
    val iconColor: Color,
    val contentColor: Color
)

@Composable
private fun getHighlightColors(style: HighlightStyle): HighlightColors {
    return when (style) {
        HighlightStyle.DEFINITION -> HighlightColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            iconColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        HighlightStyle.WARNING -> HighlightColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
            iconColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        HighlightStyle.TIP -> HighlightColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
            borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
            iconColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        HighlightStyle.NOTE -> HighlightColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getHighlightIcon(style: HighlightStyle): ImageVector {
    return when (style) {
        HighlightStyle.DEFINITION -> Icons.Outlined.MenuBook
        HighlightStyle.WARNING -> Icons.Outlined.Warning
        HighlightStyle.TIP -> Icons.Outlined.Lightbulb
        HighlightStyle.NOTE -> Icons.Outlined.Info
    }
}

private fun getHighlightLabel(style: HighlightStyle): String {
    return when (style) {
        HighlightStyle.DEFINITION -> "تعريف"
        HighlightStyle.WARNING -> "تنبيه"
        HighlightStyle.TIP -> "نصيحة"
        HighlightStyle.NOTE -> "ملاحظة"
    }
}
