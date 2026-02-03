package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "units",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class Units(
    @PrimaryKey 
    val id: String,                         // e.g., "geo_u1"
    val subjectId: String,
    val title: String,                      // "الوحدة الأولى: الجغرافيا الطبيعية"
    val order: Int,
    val description: String? = null,
    val estimatedHours: Float? = null       // Estimated study time for entire unit
)
