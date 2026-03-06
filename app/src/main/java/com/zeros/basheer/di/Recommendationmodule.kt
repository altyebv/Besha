package com.zeros.basheer.di

import com.zeros.basheer.domain.recommendation.RecommendationEngine
import com.zeros.basheer.domain.repository.ContentRepository
import com.zeros.basheer.feature.practice.domain.repository.PracticeRepository
import com.zeros.basheer.feature.practice.domain.usecase.GetWeakAreaQuestionsUseCase
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
    fun provideGetWeakAreaQuestionsUseCase(
        quizBankRepository: QuizBankRepository,
        practiceRepository: PracticeRepository,
    ): GetWeakAreaQuestionsUseCase = GetWeakAreaQuestionsUseCase(
        quizBankRepository = quizBankRepository,
        practiceRepository = practiceRepository,
    )

    @Provides
    @Singleton
    fun provideRecommendationEngine(
        contentRepository: ContentRepository,
        quizBankRepository: QuizBankRepository,
        practiceRepository: PracticeRepository,
        userProfileRepository: UserProfileRepository,
        getWeakAreaQuestions: GetWeakAreaQuestionsUseCase,
    ): RecommendationEngine = RecommendationEngine(
        contentRepository    = contentRepository,
        quizBankRepository   = quizBankRepository,
        practiceRepository   = practiceRepository,
        userProfileRepository = userProfileRepository,
        getWeakAreaQuestions  = getWeakAreaQuestions,
    )
}