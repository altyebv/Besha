package com.zeros.basheer.di

import com.zeros.basheer.data.repository.LessonRepository
import com.zeros.basheer.data.repository.QuizBankRepository
import com.zeros.basheer.domain.recommendation.RecommendationEngine
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
        lessonRepository: LessonRepository,
        quizBankRepository: QuizBankRepository
    ): RecommendationEngine {
        return RecommendationEngine(
            lessonRepository = lessonRepository,
            quizBankRepository = quizBankRepository
        )
    }
}