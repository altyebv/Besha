package com.zeros.basheer.data.models


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = Units::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("unitId")]
)
data class Lesson(
    @PrimaryKey val id: String, // e.g., "geo_lesson_1_1"
    val unitId: String,
    val title: String,
    val content: String, // Markdown or HTML formatted text
    val order: Int,
    val estimatedMinutes: Int = 15, // Reading time estimate
    val tags: String = "" // Comma-separated: "climate,maps,coordinates"
)