package com.zeros.basheer.feature.lesson.domain.model

import com.zeros.basheer.feature.lesson.data.entity.BlockType

data class Block(
    val id: String,
    val sectionId: String,
    val type: BlockType,
    val content: String,
    val order: Int,
    val metadata: Map<String, Any>? = null
)

