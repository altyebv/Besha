package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY nameAr")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: String): Tag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<Tag>)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()
}
