package com.zeros.basheer.feature.feed.data.dao

import androidx.room.*
import com.zeros.basheer.feature.feed.data.entity.FeedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedItemDao {

    @Query("SELECT * FROM feed_items WHERE id = :id")
    suspend fun getFeedItemById(id: String): FeedItemEntity?

    @Query("SELECT * FROM feed_items WHERE conceptId = :conceptId ORDER BY `order`")
    fun getFeedItemsByConcept(conceptId: String): Flow<List<FeedItemEntity>>

    @Query("SELECT * FROM feed_items WHERE subjectId = :subjectId ORDER BY priority DESC, `order`")
    fun getFeedItemsBySubject(subjectId: String): Flow<List<FeedItemEntity>>

    @Query("SELECT * FROM feed_items WHERE type = :type ORDER BY priority DESC")
    fun getFeedItemsByType(type: String): Flow<List<FeedItemEntity>>

    @Query("""
        SELECT fi.* FROM feed_items fi
        INNER JOIN concept_reviews cr ON fi.conceptId = cr.conceptId
        WHERE cr.nextReviewAt <= :currentTime
        ORDER BY cr.nextReviewAt ASC, fi.priority DESC
        LIMIT :limit
    """)
    fun getFeedItemsDueForReview(currentTime: Long, limit: Int = 20): Flow<List<FeedItemEntity>>

    @Query("""
        SELECT DISTINCT fi.* FROM feed_items fi
        INNER JOIN section_concepts sc ON fi.conceptId = sc.conceptId
        INNER JOIN section_progress sp ON sc.sectionId = sp.sectionId
        WHERE sp.completed = 1
        AND fi.subjectId = :subjectId
        ORDER BY fi.priority DESC
        LIMIT :limit
    """)
    fun getFeedItemsForLearnedConcepts(subjectId: String, limit: Int = 20): Flow<List<FeedItemEntity>>

    @Query("""
        SELECT * FROM feed_items 
        WHERE subjectId = :subjectId 
        AND type IN ('DEFINITION', 'FORMULA', 'RULE')
        ORDER BY priority DESC
        LIMIT :limit
    """)
    fun getHighPriorityFeedItems(subjectId: String, limit: Int = 10): Flow<List<FeedItemEntity>>

    @Query("""
        SELECT * FROM feed_items 
        WHERE subjectId = :subjectId 
        AND type = 'MINI_QUIZ'
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    fun getRandomMiniQuizzes(subjectId: String, limit: Int = 5): Flow<List<FeedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedItem(feedItem: FeedItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedItems(feedItems: List<FeedItemEntity>)

    @Update
    suspend fun updateFeedItem(feedItem: FeedItemEntity)

    @Delete
    suspend fun deleteFeedItem(feedItem: FeedItemEntity)

    @Query("DELETE FROM feed_items WHERE conceptId = :conceptId")
    suspend fun deleteFeedItemsByConcept(conceptId: String)

    @Query("DELETE FROM feed_items WHERE subjectId = :subjectId")
    suspend fun deleteFeedItemsBySubject(subjectId: String)

    @Query("DELETE FROM feed_items")
    suspend fun deleteAllFeedItems()

    @Query("SELECT COUNT(*) FROM feed_items WHERE subjectId = :subjectId")
    fun getFeedItemCountBySubject(subjectId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM feed_items WHERE conceptId = :conceptId")
    suspend fun getFeedItemCountByConcept(conceptId: String): Int
}