package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.Block
import com.zeros.basheer.data.models.BlockType
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockDao {
    @Query("SELECT * FROM blocks WHERE sectionId = :sectionId ORDER BY `order`")
    fun getBlocksBySection(sectionId: String): Flow<List<Block>>

    @Query("SELECT * FROM blocks WHERE id = :blockId")
    suspend fun getBlockById(blockId: String): Block?

    @Query("SELECT * FROM blocks WHERE conceptRef = :conceptId")
    fun getBlocksByConcept(conceptId: String): Flow<List<Block>>

    @Query("SELECT * FROM blocks WHERE sectionId = :sectionId AND type = :type ORDER BY `order`")
    fun getBlocksBySectionAndType(sectionId: String, type: BlockType): Flow<List<Block>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: Block)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<Block>)

    @Delete
    suspend fun deleteBlock(block: Block)

    @Query("DELETE FROM blocks WHERE sectionId = :sectionId")
    suspend fun deleteBlocksBySection(sectionId: String)
}
