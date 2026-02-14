package com.zeros.basheer.ui.components.blocks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.zeros.basheer.domain.model.BlockMetadata
import com.zeros.basheer.domain.model.BlockUiModel
import com.zeros.basheer.domain.model.HighlightStyle
import com.zeros.basheer.ui.components.blocks.foundation.BlockColors
import com.zeros.basheer.ui.components.blocks.foundation.InfoBox
import com.zeros.basheer.ui.components.blocks.foundation.InfoBoxColors

/**
 * Highlight box for important content with different visual styles.
 * Now uses the shared InfoBox component for consistency.
 *
 * Supports:
 * - DEFINITION (blue) - Key terms and concepts
 * - WARNING (red) - Important warnings and caveats
 * - TIP (purple) - Helpful suggestions
 * - NOTE (gray) - General information
 */
@Composable
fun HighlightBox(
    block: BlockUiModel,
    onConceptClick: (conceptId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val style = (block.metadata as? BlockMetadata.HighlightBox)?.style ?: HighlightStyle.NOTE

    InfoBox(
        title = style.getLabel(),
        content = block.content,
        icon = style.getIcon(),
        colors = style.getColors(),
        conceptRef = block.conceptRef,
        onConceptClick = onConceptClick,
        modifier = modifier
    )
}

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

@Composable
private fun HighlightStyle.getColors(): InfoBoxColors {
    return when (this) {
        HighlightStyle.DEFINITION -> InfoBoxColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = BlockColors.containerAlphaLight()
            ),
            borderColor = MaterialTheme.colorScheme.primary.copy(
                alpha = BlockColors.borderAlpha()
            ),
            iconColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        HighlightStyle.WARNING -> InfoBoxColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                alpha = BlockColors.containerAlphaLight()
            ),
            borderColor = MaterialTheme.colorScheme.error.copy(
                alpha = BlockColors.borderAlpha()
            ),
            iconColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        HighlightStyle.TIP -> InfoBoxColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(
                alpha = BlockColors.containerAlphaLight()
            ),
            borderColor = MaterialTheme.colorScheme.tertiary.copy(
                alpha = BlockColors.borderAlpha()
            ),
            iconColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        HighlightStyle.NOTE -> InfoBoxColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = BlockColors.borderAlpha()
            ),
            borderColor = MaterialTheme.colorScheme.outline.copy(
                alpha = BlockColors.containerAlphaLight()
            ),
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun HighlightStyle.getIcon(): ImageVector {
    return when (this) {
        HighlightStyle.DEFINITION -> Icons.Outlined.MenuBook
        HighlightStyle.WARNING -> Icons.Outlined.Warning
        HighlightStyle.TIP -> Icons.Outlined.Lightbulb
        HighlightStyle.NOTE -> Icons.Outlined.Info
    }
}

private fun HighlightStyle.getLabel(): String {
    return when (this) {
        HighlightStyle.DEFINITION -> "تعريف"
        HighlightStyle.WARNING -> "تنبيه"
        HighlightStyle.TIP -> "نصيحة"
        HighlightStyle.NOTE -> "ملاحظة"
    }
}