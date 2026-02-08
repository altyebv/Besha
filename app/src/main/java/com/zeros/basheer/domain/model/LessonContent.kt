package com.zeros.basheer.domain.model

import com.zeros.basheer.feature.lesson.data.entity.BlockType

/**
 * UI-ready lesson content model.
 * Flattened and optimized for rendering in LazyColumn.
 */
data class LessonContent(
    val id: String,
    val title: String,
    val estimatedMinutes: Int,
    val summary: String?,
    val sections: List<SectionUiModel>
)

data class SectionUiModel(
    val id: String,
    val title: String,
    val order: Int,
    val blocks: List<BlockUiModel>
)

data class BlockUiModel(
    val id: String,
    val type: BlockType,
    val content: String,
    val order: Int,
    val conceptRef: String?,
    val caption: String?,
    val metadata: BlockMetadata?
)

/**
 * Parsed metadata for different block types.
 * This avoids parsing JSON in the UI layer.
 */
sealed class BlockMetadata {
    data class Heading(val level: Int) : BlockMetadata()
    
    data class List(
        val style: ListStyle,
        val items: kotlin.collections.List<ListItem>
    ) : BlockMetadata()
    
    data class HighlightBox(val style: HighlightStyle) : BlockMetadata()
    
    data class Table(
        val headers: kotlin.collections.List<String>,
        val rows: kotlin.collections.List<kotlin.collections.List<String>>
    ) : BlockMetadata()
    
    data class Image(val aspectRatio: Float?) : BlockMetadata()
}

enum class ListStyle {
    BULLET, NUMBERED
}

data class ListItem(
    val text: String,
    val conceptRef: String? = null,
    val children: kotlin.collections.List<ListItem>? = null
)

enum class HighlightStyle {
    DEFINITION, WARNING, NOTE, TIP
}
