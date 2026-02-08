package com.zeros.basheer.feature.lesson.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.data.models.Concept

/**
 * A content block within a section.
 *
 * Blocks are the atomic units of content. Each block has a type
 * that determines how it's rendered in the UI.
 *
 * Media paths: For IMAGE/GIF types, `content` contains a relative path
 * from the app's assets folder, e.g., "images/geo/latitude_lines.png"
 */
@Entity(
    tableName = "blocks",
    foreignKeys = [
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Concept::class,
            parentColumns = ["id"],
            childColumns = ["conceptRef"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("sectionId"),
        Index("conceptRef")
    ]
)
data class BlockEntity(
    @PrimaryKey
    val id: String,                         // e.g., "geo_u1_l1_s1_b1"
    val sectionId: String,
    val type: BlockType,
    val content: String,                    // Main content (text, image path, formula, etc.)
    val order: Int,
    val conceptRef: String? = null,         // Link to concept this block defines/explains
    val caption: String? = null,            // For IMAGE/GIF types
    val metadata: String? = null            // JSON for type-specific data (list style, formula format, etc.)
)

enum class BlockType {
    TEXT,               // Regular paragraph
    HEADING,            // Section header (H2, H3)
    IMAGE,              // Image with optional caption
    GIF,                // Animated image
    FORMULA,            // Mathematical formula (LaTeX or plain)
    HIGHLIGHT_BOX,      // Important callout box (تنبيه/ملاحظة)
    EXAMPLE,            // Worked example
    TIP,                // Study tip
    LIST,               // Bullet/numbered list (items in content, style in metadata)
    TABLE,              // Table (JSON structure in content)
    QUOTE,              // Block quote
    DIVIDER             // Visual separator
}
