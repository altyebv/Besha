package com.zeros.basheer.feature.user.di

import android.content.Context
import com.zeros.basheer.feature.user.notification.ReminderNotificationManager
import com.zeros.basheer.feature.user.notification.ReminderScheduler
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideReminderNotificationManager(
        @ApplicationContext context: Context
    ): ReminderNotificationManager = ReminderNotificationManager(context)

    @Provides
    @Singleton
    fun provideReminderScheduler(
        @ApplicationContext context: Context,
        preferences: UserPreferencesRepository
    ): ReminderScheduler = ReminderScheduler(context, preferences)
}