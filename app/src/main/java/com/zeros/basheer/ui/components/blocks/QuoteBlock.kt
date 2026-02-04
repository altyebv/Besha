package com.zeros.basheer.ui.components.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.zeros.basheer.domain.model.BlockUiModel

@Composable
fun QuoteBlock(
    block: BlockUiModel,
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.primary
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .drawBehind {
                // Draw right border for RTL
                drawLine(
                    color = borderColor,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 4.dp.toPx()
                )
            }
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(start = 16.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Text(
            text = block.content,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontStyle = FontStyle.Italic,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
