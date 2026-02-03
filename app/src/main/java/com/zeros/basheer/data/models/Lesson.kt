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
    @PrimaryKey 
    val id: String,                         // e.g., "geo_u1_l1"
    val unitId: String,
    val title: String,                      // "الإحداثيات الجغرافية"
    val order: Int,
    val estimatedMinutes: Int = 15,
    val summary: String? = null,
    val content: String = "", // temp fix
)
