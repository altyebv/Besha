package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.FeedItem
import com.zeros.basheer.data.models.FeedItemType
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedItemDao {

    // ==================== Basic Queries ====================

    @Query("SELECT * FROM feed_items WHERE id = :id")
    suspend fun getFeedItemById(id: String): FeedItem?

    @Query("SELECT * FROM feed_items WHERE conceptId = :conceptId ORDER BY `order`")
    fun getFeedItemsByConcept(conceptId: String): Flow<List<FeedItem>>

    @Query("SELECT * FROM feed_items WHERE subjectId = :subjectId ORDER BY priority DESC, `order`")
    fun getFeedItemsBySubject(subjectId: String): Flow<List<FeedItem>>

    @Query("SELECT * FROM feed_items WHERE type = :type ORDER BY priority DESC")
    fun getFeedItemsByType(type: FeedItemType): Flow<List<FeedItem>>

    // ==================== Feed Algorithm Queries ====================

    /**
     * Get feed items for concepts that are due for review.
     * This joins with ConceptReview to find items the student should see.
     */
    @Query("""
        SELECT fi.* FROM feed_items fi
        INNER JOIN concept_reviews cr ON fi.conceptId = cr.conceptId
        WHERE cr.nextReviewAt <= :currentTime
        ORDER BY cr.nextReviewAt ASC, fi.priority DESC
        LIMIT :limit
    """)
    fun getFeedItemsDueForReview(currentTime: Long, limit: Int = 20): Flow<List<FeedItem>>

    /**
     * Get feed items for concepts the student has seen (via completed sections).
     * Excludes concepts not yet introduced in lessons.
     */
    @Query("""
        SELECT DISTINCT fi.* FROM feed_items fi
        INNER JOIN section_concepts sc ON fi.conceptId = sc.conceptId
        INNER JOIN section_progress sp ON sc.sectionId = sp.sectionId
        WHERE sp.completed = 1
        AND fi.subjectId = :subjectId
        ORDER BY fi.priority DESC
        LIMIT :limit
    """)
    fun getFeedItemsForLearnedConcepts(subjectId: String, limit: Int = 20): Flow<List<FeedItem>>

    /**
     * Get high-priority feed items (definitions, formulas, rules) for a subject.
     */
    @Query("""
        SELECT * FROM feed_items 
        WHERE subjectId = :subjectId 
        AND type IN ('DEFINITION', 'FORMULA', 'RULE')
        ORDER BY priority DESC
        LIMIT :limit
    """)
    fun getHighPriorityFeedItems(subjectId: String, limit: Int = 10): Flow<List<FeedItem>>

    /**
     * Get interactive feed items (mini quizzes) for gamification.
     */
    @Query("""
        SELECT * FROM feed_items 
        WHERE subjectId = :subjectId 
        AND type = 'MINI_QUIZ'
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    fun getRandomMiniQuizzes(subjectId: String, limit: Int = 5): Flow<List<FeedItem>>

    // ==================== Insert/Update/Delete ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedItem(feedItem: FeedItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedItems(feedItems: List<FeedItem>)

    @Update
    suspend fun updateFeedItem(feedItem: FeedItem)

    @Delete
    suspend fun deleteFeedItem(feedItem: FeedItem)

    @Query("DELETE FROM feed_items WHERE conceptId = :conceptId")
    suspend fun deleteFeedItemsByConcept(conceptId: String)

    @Query("DELETE FROM feed_items WHERE subjectId = :subjectId")
    suspend fun deleteFeedItemsBySubject(subjectId: String)

    @Query("DELETE FROM feed_items")
    suspend fun deleteAllFeedItems()

    // ==================== Count Queries ====================

    @Query("SELECT COUNT(*) FROM feed_items WHERE subjectId = :subjectId")
    fun getFeedItemCountBySubject(subjectId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM feed_items WHERE conceptId = :conceptId")
    suspend fun getFeedItemCountByConcept(conceptId: String): Int
}