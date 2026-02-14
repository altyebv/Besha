package com.zeros.basheer.ui.components.blocks

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.zeros.basheer.domain.model.BlockUiModel
import com.zeros.basheer.ui.components.blocks.foundation.BlockTypography
import com.zeros.basheer.ui.components.blocks.foundation.blockContainer

/**
 * Standard text block for lesson content.
 * Supports proper line spacing and accessibility.
 */
@Composable
fun TextBlock(
    block: BlockUiModel,
    modifier: Modifier = Modifier
) {
    Text(
        text = block.content,
        style = MaterialTheme.typography.bodyLarge.copy(
            lineHeight = MaterialTheme.typography.bodyLarge.fontSize * BlockTypography.bodyLineHeightMultiplier
        ),
        textAlign = TextAlign.Start,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .blockContainer()
            .semantics {
                contentDescription = "Lesson text: ${block.content.take(50)}"
            }
    )
}