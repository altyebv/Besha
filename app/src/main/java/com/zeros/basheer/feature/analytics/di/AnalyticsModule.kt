package com.zeros.basheer.feature.analytics.di

import com.google.firebase.firestore.FirebaseFirestore
import com.zeros.basheer.feature.analytics.data.repository.AnalyticsRepositoryImpl
import com.zeros.basheer.feature.analytics.data.repository.UserSyncRepositoryImpl
import com.zeros.basheer.feature.analytics.domain.repository.AnalyticsRepository
import com.zeros.basheer.feature.analytics.domain.repository.UserSyncRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        impl: AnalyticsRepositoryImpl,
    ): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindUserSyncRepository(
        impl: UserSyncRepositoryImpl,
    ): UserSyncRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    }
}