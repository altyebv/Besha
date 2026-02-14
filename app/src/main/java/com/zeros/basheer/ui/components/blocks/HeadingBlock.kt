package com.zeros.basheer.ui.components.blocks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.domain.model.BlockMetadata
import com.zeros.basheer.domain.model.BlockUiModel
import com.zeros.basheer.ui.components.blocks.foundation.BlockSpacing
import com.zeros.basheer.ui.components.blocks.foundation.blockContainer

/**
 * Heading block with proper semantic markup and visual hierarchy.
 * Supports levels 1-4 with appropriate styling and spacing.
 * Level 1 includes a decorative accent line.
 */
@Composable
fun HeadingBlock(
    block: BlockUiModel,
    modifier: Modifier = Modifier
) {
    val level = (block.metadata as? BlockMetadata.Heading)?.level ?: 2

    val (style, topPadding) = when (level) {
        1 -> MaterialTheme.typography.headlineLarge to BlockSpacing.extraLarge
        2 -> MaterialTheme.typography.headlineMedium to 20.dp
        3 -> MaterialTheme.typography.headlineSmall to BlockSpacing.large
        else -> MaterialTheme.typography.titleLarge to 12.dp
    }

    Column(
        modifier = modifier
            .blockContainer()
            .padding(top = topPadding - BlockSpacing.vertical) // Adjust for blockContainer padding
            .semantics { heading() },
        verticalArrangement = Arrangement.spacedBy(BlockSpacing.medium)
    ) {
        Text(
            text = block.content,
            style = style.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Visual accent for H1
        if (level == 1) {
            HorizontalDivider(
                modifier = Modifier.width(40.dp),
                thickness = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}