package com.zeros.basheer.ui.components.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zeros.basheer.domain.model.BlockMetadata
import com.zeros.basheer.domain.model.BlockUiModel

@Composable
fun TableBlock(
    block: BlockUiModel,
    modifier: Modifier = Modifier
) {
    val tableData = block.metadata as? BlockMetadata.Table
    val headers = tableData?.headers ?: emptyList()
    val rows = tableData?.rows ?: emptyList()
    
    if (headers.isEmpty() && rows.isEmpty()) {
        // Fallback: show raw content
        Text(
            text = block.content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.padding(16.dp)
        )
        return
    }
    
    val scrollState = rememberScrollState()
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, borderColor, MaterialTheme.shapes.small)
        ) {
            // Header row
            if (headers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    headers.forEach { header ->
                        TableCell(
                            text = header,
                            isHeader = true,
                            borderColor = borderColor
                        )
                    }
                }
            }
            
            // Data rows
            rows.forEach { row ->
                Row {
                    row.forEach { cell ->
                        TableCell(
                            text = cell,
                            isHeader = false,
                            borderColor = borderColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    isHeader: Boolean,
    borderColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .widthIn(min = 80.dp, max = 200.dp)
            .border(0.5.dp, borderColor)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = if (isHeader) {
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
