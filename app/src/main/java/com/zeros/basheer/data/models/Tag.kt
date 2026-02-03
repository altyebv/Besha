package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tags for cross-cutting categorization.
 * 
 * Tags allow filtering and grouping across the entire app:
 * - Find all concepts about "climate"
 * - Find all lessons related to "maps"
 * - Group content for contributors
 */
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey
    val id: String,                         // e.g., "climate", "maps", "coordinates"
    val nameAr: String,                     // "المناخ"
    val nameEn: String? = null,             // "Climate"
    val color: String? = null               // Optional hex color for UI: "#4CAF50"
)
