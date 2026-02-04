package com.zeros.basheer.ui.components.blocks

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeros.basheer.domain.model.BlockMetadata
import com.zeros.basheer.domain.model.BlockUiModel

@Composable
fun HeadingBlock(
    block: BlockUiModel,
    modifier: Modifier = Modifier
) {
    val level = (block.metadata as? BlockMetadata.Heading)?.level ?: 2
    
    val style = when (level) {
        1 -> MaterialTheme.typography.headlineLarge
        2 -> MaterialTheme.typography.headlineMedium
        3 -> MaterialTheme.typography.headlineSmall
        else -> MaterialTheme.typography.titleLarge
    }
    
    val topPadding = when (level) {
        1 -> 24.dp
        2 -> 20.dp
        3 -> 16.dp
        else -> 12.dp
    }
    
    Text(
        text = block.content,
        style = style.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = topPadding,
                bottom = 8.dp
            )
    )
}
