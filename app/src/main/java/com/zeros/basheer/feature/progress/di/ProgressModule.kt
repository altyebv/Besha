package com.zeros.basheer.feature.progress.di


import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.feature.progress.data.dao.ProgressDao
import com.zeros.basheer.feature.progress.data.repository.ProgressRepositoryImpl
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for Progress feature dependencies.
 * Provides DAOs, Repository, and Use Cases.
 */
@Module
@InstallIn(SingletonComponent::class)
object ProgressModule {

    /**
     * Provides ProgressDao from AppDatabase.
     */
    @Provides
    @Singleton
    fun provideProgressDao(database: AppDatabase): ProgressDao {
        return database.progressDao()
    }

    /**
     * Provides ProgressRepository implementation.
     */
    @Provides
    @Singleton
    fun provideProgressRepository(
        progressDao: ProgressDao
    ): ProgressRepository {
        return ProgressRepositoryImpl(progressDao)
    }
}