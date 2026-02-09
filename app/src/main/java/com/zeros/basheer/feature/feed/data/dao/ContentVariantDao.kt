package com.zeros.basheer.feature.feed.data.dao

import androidx.room.*
import com.zeros.basheer.feature.feed.data.entity.ContentVariantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentVariantDao {

    @Query("SELECT * FROM content_variants WHERE id = :id")
    suspend fun getVariantById(id: String): ContentVariantEntity?

    @Query("SELECT * FROM content_variants WHERE conceptId = :conceptId ORDER BY `order`")
    fun getVariantsByConcept(conceptId: String): Flow<List<ContentVariantEntity>>

    @Query("SELECT * FROM content_variants WHERE conceptId = :conceptId AND type = :type ORDER BY `order`")
    fun getVariantsByConceptAndType(conceptId: String, type: String): Flow<List<ContentVariantEntity>>

    @Query("SELECT * FROM content_variants WHERE source = :source ORDER BY createdAt DESC")
    fun getVariantsBySource(source: String): Flow<List<ContentVariantEntity>>

    @Query("""
        SELECT * FROM content_variants 
        WHERE conceptId = :conceptId 
        AND type = 'EXPLANATION'
        ORDER BY isVerified DESC, upvotes DESC, `order`
    """)
    fun getExplanationsForConcept(conceptId: String): Flow<List<ContentVariantEntity>>

    @Query("""
        SELECT * FROM content_variants 
        WHERE conceptId = :conceptId 
        AND type = 'EXAMPLE'
        ORDER BY isVerified DESC, upvotes DESC, `order`
    """)
    fun getExamplesForConcept(conceptId: String): Flow<List<ContentVariantEntity>>

    @Query("""
        SELECT * FROM content_variants 
        WHERE conceptId = :conceptId 
        AND type IN ('MNEMONIC', 'ANALOGY')
        ORDER BY upvotes DESC
    """)
    fun getMemoryAidsForConcept(conceptId: String): Flow<List<ContentVariantEntity>>

    @Query("""
        SELECT * FROM content_variants 
        WHERE source = 'TEACHER' 
        AND isVerified = 1
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    fun getVerifiedTeacherContent(limit: Int = 50): Flow<List<ContentVariantEntity>>

    @Query("""
        SELECT * FROM content_variants 
        WHERE conceptId = :conceptId 
        AND source = 'OFFICIAL'
        ORDER BY `order`
    """)
    fun getOfficialContentForConcept(conceptId: String): Flow<List<ContentVariantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariant(variant: ContentVariantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariants(variants: List<ContentVariantEntity>)

    @Update
    suspend fun updateVariant(variant: ContentVariantEntity)

    @Delete
    suspend fun deleteVariant(variant: ContentVariantEntity)

    @Query("DELETE FROM content_variants WHERE conceptId = :conceptId")
    suspend fun deleteVariantsByConcept(conceptId: String)

    @Query("DELETE FROM content_variants")
    suspend fun deleteAllVariants()

    @Query("UPDATE content_variants SET upvotes = upvotes + 1 WHERE id = :variantId")
    suspend fun upvoteVariant(variantId: String)

    @Query("UPDATE content_variants SET upvotes = upvotes - 1 WHERE id = :variantId AND upvotes > 0")
    suspend fun downvoteVariant(variantId: String)

    @Query("UPDATE content_variants SET isVerified = 1 WHERE id = :variantId")
    suspend fun verifyVariant(variantId: String)

    @Query("SELECT * FROM content_variants WHERE isVerified = 0 ORDER BY createdAt ASC")
    fun getUnverifiedVariants(): Flow<List<ContentVariantEntity>>

    @Query("SELECT COUNT(*) FROM content_variants WHERE conceptId = :conceptId")
    suspend fun getVariantCountByConcept(conceptId: String): Int

    @Query("SELECT COUNT(*) FROM content_variants WHERE conceptId = :conceptId AND type = :type")
    suspend fun getVariantCountByConceptAndType(conceptId: String, type: String): Int
}