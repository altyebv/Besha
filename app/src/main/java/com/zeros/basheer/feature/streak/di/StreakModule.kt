package com.zeros.basheer.feature.streak.di


import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.feature.streak.data.dao.DailyActivityDao
import com.zeros.basheer.feature.streak.data.repository.StreakRepositoryImpl
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for Streak feature dependencies.
 * Provides DAOs, Repository, and Use Cases.
 */
@Module
@InstallIn(SingletonComponent::class)
object StreakModule {

    /**
     * Provides DailyActivityDao from AppDatabase.
     */
    @Provides
    @Singleton
    fun provideDailyActivityDao(database: AppDatabase): DailyActivityDao {
        return database.dailyActivityDao()
    }

    /**
     * Provides StreakRepository implementation.
     */
    @Provides
    @Singleton
    fun provideStreakRepository(
        dailyActivityDao: DailyActivityDao
    ): StreakRepository {
        return StreakRepositoryImpl(dailyActivityDao)
    }
}