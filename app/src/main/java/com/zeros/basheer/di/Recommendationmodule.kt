package com.zeros.basheer.di

import com.zeros.basheer.domain.recommendation.RecommendationEngine
import com.zeros.basheer.domain.repository.ContentRepository
import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import com.zeros.basheer.feature.user.domain.repository.UserProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecommendationModule {

    @Provides
    @Singleton
    fun provideRecommendationEngine(
        contentRepository: ContentRepository,
        quizBankRepository: QuizBankRepository,
        practiceRepository: PracticeRepository,
        userProfileRepository: UserProfileRepository
    ): RecommendationEngine {
        return RecommendationEngine(
            contentRepository = contentRepository,
            quizBankRepository = quizBankRepository,
            practiceRepository = practiceRepository,
            userProfileRepository = userProfileRepository
        )
    }
}