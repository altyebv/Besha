package com.zeros.basheer.domain.model

import com.zeros.basheer.feature.lesson.data.entity.BlockType

import com.zeros.basheer.feature.lesson.data.entity.LearningType

import com.zeros.basheer.feature.lesson.domain.model.LessonMetadata

/**
 * UI-ready lesson content model.
 * Flattened and optimized for rendering in LazyColumn.
 */
data class LessonContent(
    val id: String,
    val title: String,
    val estimatedMinutes: Int,
    val summary: String?,
    val sections: List<SectionUiModel>,
    /** The unit this lesson belongs to — mirrors [LessonEntity.unitId]. */
    val unitId: String = "",
    /** Parsed lesson metadata (hook, orientation, forwardPull). Null if not authored. */
    val metadata: LessonMetadata? = null
)

data class SectionUiModel(
    val id: String,
    val title: String,
    val order: Int,
    val learningType: LearningType = LearningType.UNDERSTANDING,
    val blocks: List<BlockUiModel>,
    /** Part this section belongs to (0-indexed). Derived from [SectionEntity.partIndex]. */
    val partIndex: Int = 0
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

    /**
     * Example block metadata.
     *
     * Static mode  → [interactive] = false, [steps] is empty, content rendered as-is.
     * Interactive mode → [interactive] = true, [steps] holds each step string.
     *   Each tap reveals the next step; all steps accumulate on screen.
     *
     */
    data class Example(
        val interactive: Boolean,
        val steps: kotlin.collections.List<String>
    ) : BlockMetadata()
}

enum class ListStyle {
    BULLET, NUMBERED
}

data class ListItem(
    val text: String,
    val conceptRef: String? = null,
    val children: List<ListItem>? = null
)

enum class HighlightStyle {
    DEFINITION, WARNING, NOTE, TIP
}