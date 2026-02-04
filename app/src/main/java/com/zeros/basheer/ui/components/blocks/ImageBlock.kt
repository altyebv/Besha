package com.zeros.basheer.ui.components.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zeros.basheer.domain.model.BlockMetadata
import com.zeros.basheer.domain.model.BlockUiModel

@Composable
fun ImageBlock(
    block: BlockUiModel,
    modifier: Modifier = Modifier
) {
    val aspectRatio = (block.metadata as? BlockMetadata.Image)?.aspectRatio
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (aspectRatio != null) {
                        Modifier.aspectRatio(aspectRatio)
                    } else {
                        Modifier
                    }
                )
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getImagePath(block.content))
                    .crossfade(true)
                    .build(),
                contentDescription = block.caption,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
            )
        }
        
        // Caption
        if (!block.caption.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = block.caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Converts relative path to full asset path.
 * In production, this would load from app assets.
 * For now, we support both asset paths and file:// URIs.
 */
private fun getImagePath(content: String): String {
    return when {
        content.startsWith("http") -> content
        content.startsWith("file://") -> content
        content.startsWith("/") -> "file://$content"
        else -> "file:///android_asset/$content"
    }
}
