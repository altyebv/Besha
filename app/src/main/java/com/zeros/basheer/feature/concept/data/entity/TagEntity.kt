package com.zeros.basheer.feature.concept.data.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val id: String,
    val nameAr: String,
    val nameEn: String? = null,
    val color: String? = null
)