package com.zeros.basheer.feature.progress.di

import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.feature.progress.data.dao.LessonPartProgressDao
import com.zeros.basheer.feature.progress.data.dao.ProgressDao
import com.zeros.basheer.feature.progress.data.repository.ProgressRepositoryImpl
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProgressModule {

    @Provides
    @Singleton
    fun provideProgressDao(database: AppDatabase): ProgressDao =
        database.progressDao()

    @Provides
    @Singleton
    fun provideLessonPartProgressDao(database: AppDatabase): LessonPartProgressDao =
        database.lessonPartProgressDao()

    @Provides
    @Singleton
    fun provideProgressRepository(progressDao: ProgressDao): ProgressRepository =
        ProgressRepositoryImpl(progressDao)
}