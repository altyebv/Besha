package com.zeros.basheer.domain.mapper

import com.zeros.basheer.feature.lesson.data.entity.BlockEntity
import com.zeros.basheer.feature.lesson.data.entity.BlockType
import com.zeros.basheer.domain.model.*
import com.zeros.basheer.feature.lesson.data.relations.LessonFull
import com.zeros.basheer.feature.lesson.data.relations.SectionWithBlocksAndConcepts
import org.json.JSONArray
import org.json.JSONObject

/**
 * Maps Room entities to UI-ready models.
 */
object LessonMapper {
    
    fun toLessonContent(lessonFull: LessonFull): LessonContent {
        return LessonContent(
            id = lessonFull.lessonEntity.id,
            title = lessonFull.lessonEntity.title,
            estimatedMinutes = lessonFull.lessonEntity.estimatedMinutes,
            summary = lessonFull.lessonEntity.summary,
            sections = lessonFull.sections
                .sortedBy { it.sectionEntity.order }
                .map { toSectionUiModel(it) }
        )
    }
    
    private fun toSectionUiModel(section: SectionWithBlocksAndConcepts): SectionUiModel {
        return SectionUiModel(
            id = section.sectionEntity.id,
            title = section.sectionEntity.title,
            order = section.sectionEntity.order,
            blocks = section.blocks
                .sortedBy { it.order }
                .map { toBlockUiModel(it) }
        )
    }
    
    private fun toBlockUiModel(block: BlockEntity): BlockUiModel {
        return BlockUiModel(
            id = block.id,
            type = block.type,
            content = block.content,
            order = block.order,
            conceptRef = block.conceptRef,
            caption = block.caption,
            metadata = parseMetadata(block)
        )
    }
    
    private fun parseMetadata(block: BlockEntity): BlockMetadata? {
        val metadataJson = block.metadata ?: return getDefaultMetadata(block.type)
        
        return try {
            val json = JSONObject(metadataJson)
            
            when (block.type) {
                BlockType.HEADING -> {
                    BlockMetadata.Heading(
                        level = json.optInt("level", 2)
                    )
                }
                
                BlockType.LIST -> {
                    val style = when (json.optString("style", "bullet")) {
                        "numbered" -> ListStyle.NUMBERED
                        else -> ListStyle.BULLET
                    }
                    // Parse items from content if hasConceptLinks is true
                    val items = parseListItems(block.content, json.optBoolean("hasConceptLinks", false))
                    BlockMetadata.List(style = style, items = items)
                }
                
                BlockType.HIGHLIGHT_BOX -> {
                    val style = when (json.optString("style", "note")) {
                        "definition" -> HighlightStyle.DEFINITION
                        "warning" -> HighlightStyle.WARNING
                        "tip" -> HighlightStyle.TIP
                        else -> HighlightStyle.NOTE
                    }
                    BlockMetadata.HighlightBox(style = style)
                }
                
                BlockType.TABLE -> {
                    parseTableMetadata(block.content)
                }
                
                BlockType.IMAGE, BlockType.GIF -> {
                    BlockMetadata.Image(
                        aspectRatio = json.optDouble("aspectRatio", 0.0).takeIf { it > 0 }?.toFloat()
                    )
                }
                
                else -> null
            }
        } catch (e: Exception) {
            getDefaultMetadata(block.type)
        }
    }
    
    private fun getDefaultMetadata(type: BlockType): BlockMetadata? {
        return when (type) {
            BlockType.HEADING -> BlockMetadata.Heading(level = 2)
            BlockType.HIGHLIGHT_BOX -> BlockMetadata.HighlightBox(style = HighlightStyle.NOTE)
            else -> null
        }
    }
    
    private fun parseListItems(content: String, hasConceptLinks: Boolean): List<ListItem> {
        return try {
            if (hasConceptLinks || content.startsWith("[")) {
                // Content is JSON array
                val jsonArray = JSONArray(content)
                (0 until jsonArray.length()).map { i ->
                    val item = jsonArray.getJSONObject(i)
                    ListItem(
                        text = item.getString("text"),
                        conceptRef = item.optString("conceptRef").takeIf { it.isNotEmpty() },
                        children = item.optJSONArray("children")?.let { childArray ->
                            (0 until childArray.length()).map { j ->
                                val child = childArray.get(j)
                                when (child) {
                                    is String -> ListItem(text = child)
                                    is JSONObject -> ListItem(
                                        text = child.getString("text"),
                                        conceptRef = child.optString("conceptRef").takeIf { it.isNotEmpty() }
                                    )
                                    else -> ListItem(text = child.toString())
                                }
                            }
                        }
                    )
                }
            } else {
                // Content is plain text with newlines
                content.split("\n")
                    .filter { it.isNotBlank() }
                    .map { ListItem(text = it.trim()) }
            }
        } catch (e: Exception) {
            // Fallback: treat as plain text
            content.split("\n")
                .filter { it.isNotBlank() }
                .map { ListItem(text = it.trim()) }
        }
    }
    
    private fun parseTableMetadata(content: String): BlockMetadata.Table {
        return try {
            val json = JSONObject(content)
            val headers = json.optJSONArray("headers")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList()
            
            val rows = json.optJSONArray("rows")?.let { rowsArr ->
                (0 until rowsArr.length()).map { i ->
                    val row = rowsArr.getJSONArray(i)
                    (0 until row.length()).map { j -> row.getString(j) }
                }
            } ?: emptyList()
            
            BlockMetadata.Table(headers = headers, rows = rows)
        } catch (e: Exception) {
            BlockMetadata.Table(headers = emptyList(), rows = emptyList())
        }
    }
}
