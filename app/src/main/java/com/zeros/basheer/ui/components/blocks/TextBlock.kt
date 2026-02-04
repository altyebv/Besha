package com.zeros.basheer.ui.components.blocks

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zeros.basheer.domain.model.BlockUiModel

@Composable
fun TextBlock(
    block: BlockUiModel,
    modifier: Modifier = Modifier
) {
    Text(
        text = block.content,
        style = MaterialTheme.typography.bodyLarge.copy(
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.6
        ),
        textAlign = TextAlign.Start,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
