package com.zeros.basheer.feature.lesson.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zeros.basheer.feature.lesson.data.entity.BlockEntity
import com.zeros.basheer.feature.lesson.data.entity.BlockType
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockDao {
    @Query("SELECT * FROM blocks WHERE sectionId = :sectionId ORDER BY `order`")
    fun getBlocksBySection(sectionId: String): Flow<List<BlockEntity>>

    @Query("SELECT * FROM blocks WHERE id = :blockId")
    suspend fun getBlockById(blockId: String): BlockEntity?

    @Query("SELECT * FROM blocks WHERE conceptRef = :conceptId")
    fun getBlocksByConcept(conceptId: String): Flow<List<BlockEntity>>

    @Query("SELECT * FROM blocks WHERE sectionId = :sectionId AND type = :type ORDER BY `order`")
    fun getBlocksBySectionAndType(sectionId: String, type: BlockType): Flow<List<BlockEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertBlock(block: BlockEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertBlocks(blocks: List<BlockEntity>)

    @Query("SELECT * FROM blocks WHERE sectionId IN (:sectionIds) ORDER BY `order`")
    suspend fun getBlocksBySectionIds(sectionIds: List<String>): List<BlockEntity>

    @Delete
    suspend fun deleteBlock(block: BlockEntity)

    @Query("DELETE FROM blocks WHERE sectionId = :sectionId")
    suspend fun deleteBlocksBySection(sectionId: String)
}