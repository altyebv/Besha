package com.zeros.basheer.feature.user.di

import android.content.SharedPreferences
import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.feature.user.data.dao.UserProfileDao
import com.zeros.basheer.feature.user.data.dao.XpDao
import com.zeros.basheer.feature.user.data.repository.XpRepositoryImpl
import com.zeros.basheer.feature.user.domain.repository.XpRepository
import com.zeros.basheer.feature.streak.domain.repository.StreakRepository
import com.zeros.basheer.feature.user.data.repository.UserPreferencesRepositoryImpl
import com.zeros.basheer.feature.user.data.repository.UserProfileRepositoryImpl
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import com.zeros.basheer.feature.user.domain.repository.UserProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao =
        database.userProfileDao()

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        dao: UserProfileDao
    ): UserProfileRepository = UserProfileRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        prefs: SharedPreferences
    ): UserPreferencesRepository = UserPreferencesRepositoryImpl(prefs)

    @Provides
    fun provideXpDao(database: AppDatabase): XpDao =
        database.xpDao()

    @Provides
    @Singleton
    fun provideXpRepository(
        dao: XpDao,
        streakRepository: StreakRepository
    ): XpRepository = XpRepositoryImpl(dao, streakRepository)
}