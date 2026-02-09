package com.zeros.basheer.feature.feed.di


import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.feature.feed.data.dao.ContentVariantDao
import com.zeros.basheer.feature.feed.data.dao.FeedItemDao
import com.zeros.basheer.feature.feed.domain.repository.FeedRepositoryImpl
import com.zeros.basheer.feature.feed.domain.repository.FeedRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {

    @Provides
    @Singleton
    fun provideFeedItemDao(database: AppDatabase): FeedItemDao = database.feedItemDao()

    @Provides
    @Singleton
    fun provideContentVariantDao(database: AppDatabase): ContentVariantDao = database.contentVariantDao()

    @Provides
    @Singleton
    fun provideFeedRepository(
        feedItemDao: FeedItemDao,
        contentVariantDao: ContentVariantDao
    ): FeedRepository = FeedRepositoryImpl(feedItemDao, contentVariantDao)
}