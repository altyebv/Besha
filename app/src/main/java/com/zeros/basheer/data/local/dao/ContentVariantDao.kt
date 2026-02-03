package com.zeros.basheer.data.local.dao

import androidx.room.*
import com.zeros.basheer.data.models.ContentSource
import com.zeros.basheer.data.models.ContentVariant
import com.zeros.basheer.data.models.VariantType
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentVariantDao {

    // ==================== Basic Queries ====================

    @Query("SELECT * FROM content_variants WHERE id = :id")
    suspend fun getVariantById(id: String): ContentVariant?

    @Query("SELECT * FROM content_variants WHERE conceptId = :conceptId ORDER BY `order`")
    fun getVariantsByConcept(conceptId: String): Flow<List<ContentVariant>>

    @Query("SELECT * FROM content_variants WHERE conceptId = :conceptId AND type = :type ORDER BY `order`")
    fun getVariantsByConceptAndType(conceptId: String, type: VariantType): Flow<List<ContentVariant>>

    @Query("SELECT * FROM content_variants WHERE source = :source ORDER BY createdAt DESC")
    fun getVariantsBySource(source: ContentSource): Flow<List<ContentVariant>>

    // ==================== Feature-Specific Queries ====================

    /**
     * Get all explanations for a concept (for "alternative explanations" feature).
     */
    @Query("""
        SELECT * FROM content_variants 
        WHERE conceptId = :conceptId 
        AND type = 'EXPLANATION'
        ORDER BY isVerified DESC, upvotes DESC, `order`
    """)
    fun getExplanationsForConcept(conceptId: String): Flow<List<ContentVariant>>

    /**
     * Get all examples for a concept.
     */
    @Query("""
        SELECT * FROM content_variants 
        WHERE conceptId = :conceptId 
        AND type = 'EXAMPLE'
        ORDER BY isVerified DESC, upvotes DESC, `order`
    """)
    fun getExamplesForConcept(conceptId: String): Flow<List<ContentVariant>>

    /**
     * Get mnemonics and memory aids for a concept.
     */
    @Query("""
        SELECT * FROM content_variants 
        WHERE conceptId = :conceptId 
        AND type IN ('MNEMONIC', 'ANALOGY')
        ORDER BY upvotes DESC
    """)
    fun getMemoryAidsForConcept(conceptId: String): Flow<List<ContentVariant>>

    /**
     * Get verified teacher contributions.
     */
    @Query("""
        SELECT * FROM content_variants 
        WHERE source = 'TEACHER' 
        AND isVerified = 1
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    fun getVerifiedTeacherContent(limit: Int = 50): Flow<List<ContentVariant>>

    /**
     * Get content from official curriculum for a concept.
     */
    @Query("""
        SELECT * FROM content_variants 
        WHERE conceptId = :conceptId 
        AND source = 'OFFICIAL'
        ORDER BY `order`
    """)
    fun getOfficialContentForConcept(conceptId: String): Flow<List<ContentVariant>>

    // ==================== Insert/Update/Delete ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariant(variant: ContentVariant)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<ContentVariant>)

    @Update
    suspend fun updateVariant(variant: ContentVariant)

    @Delete
    suspend fun deleteVariant(variant: ContentVariant)

    @Query("DELETE FROM content_variants WHERE conceptId = :conceptId")
    suspend fun deleteVariantsByConcept(conceptId: String)

    @Query("DELETE FROM content_variants")
    suspend fun deleteAllVariants()

    // ==================== Upvote System (Future) ====================

    @Query("UPDATE content_variants SET upvotes = upvotes + 1 WHERE id = :variantId")
    suspend fun upvoteVariant(variantId: String)

    @Query("UPDATE content_variants SET upvotes = upvotes - 1 WHERE id = :variantId AND upvotes > 0")
    suspend fun downvoteVariant(variantId: String)

    // ==================== Moderation (Future) ====================

    @Query("UPDATE content_variants SET isVerified = 1 WHERE id = :variantId")
    suspend fun verifyVariant(variantId: String)

    @Query("SELECT * FROM content_variants WHERE isVerified = 0 ORDER BY createdAt ASC")
    fun getUnverifiedVariants(): Flow<List<ContentVariant>>

    // ==================== Count Queries ====================

    @Query("SELECT COUNT(*) FROM content_variants WHERE conceptId = :conceptId")
    suspend fun getVariantCountByConcept(conceptId: String): Int

    @Query("SELECT COUNT(*) FROM content_variants WHERE conceptId = :conceptId AND type = :type")
    suspend fun getVariantCountByConceptAndType(conceptId: String, type: VariantType): Int
}