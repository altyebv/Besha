package com.zeros.basheer.ui.components.blocks

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zeros.basheer.data.models.BlockType
import com.zeros.basheer.domain.model.BlockUiModel

/**
 * Central dispatcher for rendering blocks based on their type.
 * 
 * This is the single entry point for all block rendering.
 * Add new block types here as they're implemented.
 */
@Composable
fun BlockRenderer(
    block: BlockUiModel,
    onConceptClick: (conceptId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (block.type) {
        BlockType.TEXT -> TextBlock(
            block = block,
            modifier = modifier
        )
        
        BlockType.HEADING -> HeadingBlock(
            block = block,
            modifier = modifier
        )
        
        BlockType.IMAGE -> ImageBlock(
            block = block,
            modifier = modifier
        )
        
        BlockType.GIF -> ImageBlock(
            block = block,
            modifier = modifier
        )
        
        BlockType.HIGHLIGHT_BOX -> HighlightBox(
            block = block,
            onConceptClick = onConceptClick,
            modifier = modifier
        )
        
        BlockType.LIST -> ListBlock(
            block = block,
            onConceptClick = onConceptClick,
            modifier = modifier
        )
        
        BlockType.TIP -> TipBlock(
            block = block,
            modifier = modifier
        )
        
        BlockType.EXAMPLE -> ExampleBlock(
            block = block,
            modifier = modifier
        )
        
        BlockType.FORMULA -> FormulaBlock(
            block = block,
            modifier = modifier
        )
        
        BlockType.TABLE -> TableBlock(
            block = block,
            modifier = modifier
        )
        
        BlockType.QUOTE -> QuoteBlock(
            block = block,
            modifier = modifier
        )
        
        BlockType.DIVIDER -> DividerBlock(
            block = block,
            modifier = modifier
        )
    }
}
