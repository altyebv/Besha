package com.zeros.basheer.ui.components.blocks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.zeros.basheer.domain.model.BlockMetadata
import com.zeros.basheer.domain.model.BlockUiModel
import com.zeros.basheer.domain.model.ListItem
import com.zeros.basheer.domain.model.ListStyle

@Composable
fun ListBlock(
    block: BlockUiModel,
    onConceptClick: (conceptId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val metadata = block.metadata as? BlockMetadata.List
    val style = metadata?.style ?: ListStyle.BULLET
    val items = metadata?.items ?: parseSimpleList(block.content)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, item ->
            ListItemRow(
                item = item,
                index = index,
                style = style,
                level = 0,
                onConceptClick = onConceptClick
            )
        }
    }
}

@Composable
private fun ListItemRow(
    item: ListItem,
    index: Int,
    style: ListStyle,
    level: Int,
    onConceptClick: (conceptId: String) -> Unit
) {
    val bulletChar = when (style) {
        ListStyle.BULLET -> when (level) {
            0 -> "•"
            1 -> "◦"
            else -> "▪"
        }
        ListStyle.NUMBERED -> "${index + 1}."
    }
    
    val startPadding = (level * 16).dp
    val isClickable = item.conceptRef != null
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isClickable) {
                        Modifier.clickable { item.conceptRef?.let(onConceptClick) }
                    } else {
                        Modifier
                    }
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Bullet/Number
            Text(
                text = bulletChar,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(24.dp)
            )
            
            // Item text
            if (isClickable) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(item.text)
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Render children if present
        item.children?.forEachIndexed { childIndex, child ->
            Spacer(modifier = Modifier.height(4.dp))
            ListItemRow(
                item = child,
                index = childIndex,
                style = ListStyle.BULLET, // Children always use bullets
                level = level + 1,
                onConceptClick = onConceptClick
            )
        }
    }
}

/**
 * Fallback parser for plain text content.
 */
private fun parseSimpleList(content: String): List<ListItem> {
    return content.split("\n")
        .filter { it.isNotBlank() }
        .map { line ->
            // Remove common bullet prefixes
            val cleanedLine = line.trim()
                .removePrefix("•")
                .removePrefix("-")
                .removePrefix("*")
                .trim()
            ListItem(text = cleanedLine)
        }
}
