package com.zeros.basheer.ui.components.blocks.foundation


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Colors for InfoBox variants
 */
data class InfoBoxColors(
    val containerColor: Color,
    val borderColor: Color,
    val iconColor: Color,
    val contentColor: Color
)

/**
 * Reusable info box component used for highlights, tips, examples, etc.
 *
 * This component consolidates the pattern used in:
 * - HighlightBox (definitions, warnings, notes, tips)
 * - TipBlock
 * - ExampleBlock
 *
 * @param title Header text (e.g., "تعريف", "نصيحة", "مثال")
 * @param content Main text content
 * @param icon Leading icon
 * @param colors Color scheme for this variant
 * @param conceptRef Optional concept ID for clickable boxes
 * @param onConceptClick Callback when concept is clicked
 * @param modifier Standard modifier
 */
@Composable
fun InfoBox(
    title: String,
    content: String,
    icon: ImageVector,
    colors: InfoBoxColors,
    modifier: Modifier = Modifier,
    conceptRef: String? = null,
    onConceptClick: (String) -> Unit = {}
) {
    val isClickable = conceptRef != null

    Surface(
        modifier = modifier
            .blockContainer()
            .clickableConcept(
                conceptRef = conceptRef,
                onClick = onConceptClick,
                contentDescription = "View details for: $title"
            ),
        shape = MaterialTheme.shapes.medium,
        color = colors.containerColor,
        border = BorderStroke(BlockSizes.borderNormal, colors.borderColor)
    ) {
        Column(
            modifier = Modifier.cardInterior(),
            verticalArrangement = Arrangement.spacedBy(BlockSpacing.medium)
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BlockSpacing.iconSpacing),
                modifier = Modifier.semantics { heading() }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.iconColor,
                    modifier = Modifier.size(BlockSizes.iconMedium)
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.iconColor
                )

                // Clickable indicator
                if (isClickable) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Clickable",
                        tint = colors.iconColor.copy(alpha = BlockColors.disabledAlpha()),
                        modifier = Modifier.size(BlockSizes.iconSmall)
                    )
                }
            }

            // Content
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize * BlockTypography.bodyLineHeightMultiplier
                ),
                color = colors.contentColor
            )
        }
    }
}

/**
 * Helper to create InfoBoxColors from MaterialTheme
 */
@Composable
fun infoBoxColors(
    containerColor: Color,
    borderColor: Color,
    iconColor: Color,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) = InfoBoxColors(
    containerColor = containerColor,
    borderColor = borderColor,
    iconColor = iconColor,
    contentColor = contentColor
)