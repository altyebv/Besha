package com.zeros.basheer.feature.concept.di


import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.feature.concept.data.dao.ConceptDao
import com.zeros.basheer.feature.concept.data.dao.ConceptReviewDao
import com.zeros.basheer.feature.concept.data.dao.ConceptTagDao
import com.zeros.basheer.feature.concept.data.dao.TagDao
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepositoryImpl
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConceptModule {

    @Provides
    @Singleton
    fun provideConceptDao(database: AppDatabase): ConceptDao = database.conceptDao()

    @Provides
    @Singleton
    fun provideConceptReviewDao(database: AppDatabase): ConceptReviewDao = database.conceptReviewDao()

    @Provides
    @Singleton
    fun provideConceptTagDao(database: AppDatabase): ConceptTagDao = database.conceptTagDao()

    @Provides
    @Singleton
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

    @Provides
    @Singleton
    fun provideConceptRepository(
        conceptDao: ConceptDao,
        conceptReviewDao: ConceptReviewDao,
        conceptTagDao: ConceptTagDao,
        tagDao: TagDao
    ): ConceptRepository = ConceptRepositoryImpl(
        conceptDao,
        conceptReviewDao,
        conceptTagDao,
        tagDao
    )
}