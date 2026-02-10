package com.zeros.basheer.feature.practice.di


import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.feature.practice.data.dao.PracticeSessionDao
import com.zeros.basheer.feature.practice.domain.repository.PracticeRepositoryImpl
import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PracticeModule {

    @Provides
    @Singleton
    fun providePracticeSessionDao(database: AppDatabase): PracticeSessionDao =
        database.practiceSessionDao()

    @Provides
    @Singleton
    fun providePracticeRepository(
        practiceSessionDao: PracticeSessionDao,
        quizBankRepository: QuizBankRepository
    ): PracticeRepository = PracticeRepositoryImpl(
        practiceSessionDao = practiceSessionDao,
        quizBankRepository = quizBankRepository
    )
}