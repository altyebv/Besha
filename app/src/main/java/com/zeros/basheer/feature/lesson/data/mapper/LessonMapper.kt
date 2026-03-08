package com.zeros.basheer.feature.lesson.data.mapper

import com.zeros.basheer.domain.model.BlockMetadata
import com.zeros.basheer.domain.model.BlockUiModel
import com.zeros.basheer.domain.model.HighlightStyle
import com.zeros.basheer.domain.model.LessonContent
import com.zeros.basheer.domain.model.ListItem
import com.zeros.basheer.domain.model.ListStyle
import com.zeros.basheer.domain.model.SectionUiModel
import com.zeros.basheer.feature.lesson.data.entity.BlockEntity
import com.zeros.basheer.feature.lesson.data.entity.BlockType
import com.zeros.basheer.feature.lesson.data.relations.LessonFull
import com.zeros.basheer.feature.lesson.data.relations.SectionWithBlocksAndConcepts
import com.zeros.basheer.feature.lesson.domain.model.LessonMetadata
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
            unitId = lessonFull.lessonEntity.unitId,
            metadata = parseLessonMetadata(lessonFull.lessonEntity.metadata),
            sections = lessonFull.sections
                .sortedBy { it.sectionEntity.order }
                .map { toSectionUiModel(it) }
        )
    }

    private fun parseLessonMetadata(json: String?): LessonMetadata? {
        json ?: return null
        return try {
            val obj = JSONObject(json)
            val orientationArray = obj.optJSONArray("orientation")
            val orientation = if (orientationArray != null) {
                (0 until orientationArray.length()).map { orientationArray.getString(it) }
            } else emptyList()
            LessonMetadata(
                hook = obj.optString("hook").takeIf { it.isNotEmpty() },
                orientation = orientation,
                forwardPull = obj.optString("forwardPull").takeIf { it.isNotEmpty() }
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun toSectionUiModel(section: SectionWithBlocksAndConcepts): SectionUiModel {
        return SectionUiModel(
            id = section.sectionEntity.id,
            title = section.sectionEntity.title,
            order = section.sectionEntity.order,
            learningType = section.sectionEntity.learningType,
            partIndex = section.sectionEntity.partIndex,
            blocks = section.blocks
                .sortedBy { it.order }
                .map { toBlockUiModel(it) }
        )
    }

    /** Public entry point for callers outside this mapper (e.g. LessonRepositoryImpl). */
    fun toBlockUiModelPublic(block: BlockEntity): BlockUiModel = toBlockUiModel(block)

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
        // TABLE blocks store their data in content, not metadata — handle before the null check
        if (block.type == BlockType.TABLE) return parseTableMetadata(block.content)

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

                BlockType.EXAMPLE -> {
                    val interactive = json.optBoolean("interactive", false)
                    val stepsArray = json.optJSONArray("steps")
                    val steps = if (stepsArray != null) {
                        (0 until stepsArray.length()).map { stepsArray.getString(it) }
                    } else emptyList()
                    BlockMetadata.Example(interactive = interactive, steps = steps)
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
                                        conceptRef = child.optString("conceptRef")
                                            .takeIf { it.isNotEmpty() }
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
                    // Support two row formats:
                    // 1. Plain array:       ["القوة", "F", "نيوتن"]
                    // 2. Object with cells: {"cells": ["القوة", "F", "نيوتن"]}
                    val rowElement = rowsArr.get(i)
                    val cellsArray = when (rowElement) {
                        is JSONArray -> rowElement
                        is JSONObject -> rowElement.optJSONArray("cells") ?: JSONArray()
                        else -> JSONArray()
                    }
                    (0 until cellsArray.length()).map { j -> cellsArray.getString(j) }
                }
            } ?: emptyList()

            BlockMetadata.Table(headers = headers, rows = rows)
        } catch (e: Exception) {
            BlockMetadata.Table(headers = emptyList(), rows = emptyList())
        }
    }
}