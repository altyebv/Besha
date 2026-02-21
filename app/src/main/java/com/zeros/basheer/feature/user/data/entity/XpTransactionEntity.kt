package com.zeros.basheer.feature.user.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.user.domain.model.XpSource

@Entity(
    tableName = "xp_transactions",
    indices = [
        // Fast lookup for deduplication by source + referenceId
        Index(value = ["source", "referenceId"])
    ]
)
data class XpTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Int,
    val baseAmount: Int,
    val source: XpSource,
    val multiplier: Float,
    val referenceId: String?,      // lessonId, sessionId, etc.
    val timestamp: Long = System.currentTimeMillis()
)