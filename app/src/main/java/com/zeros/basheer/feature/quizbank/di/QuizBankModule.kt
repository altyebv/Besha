package com.zeros.basheer.feature.quizbank.di


import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.feature.quizbank.data.dao.*
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepositoryImpl
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QuizBankModule {

    @Provides
    @Singleton
    fun provideQuestionDao(database: AppDatabase): QuestionDao =
        database.questionDao()

    @Provides
    @Singleton
    fun provideExamDao(database: AppDatabase): ExamDao =
        database.examDao()

    @Provides
    @Singleton
    fun provideExamQuestionDao(database: AppDatabase): ExamQuestionDao =
        database.examQuestionDao()

    @Provides
    @Singleton
    fun provideQuestionConceptDao(database: AppDatabase): QuestionConceptDao =
        database.questionConceptDao()

    @Provides
    @Singleton
    fun provideQuizAttemptDao(database: AppDatabase): QuizAttemptDao =
        database.quizAttemptDao()

    @Provides
    @Singleton
    fun provideQuestionResponseDao(database: AppDatabase): QuestionResponseDao =
        database.questionResponseDao()

    @Provides
    @Singleton
    fun provideQuestionStatsDao(database: AppDatabase): QuestionStatsDao =
        database.questionStatsDao()

    @Provides
    @Singleton
    fun provideQuizBankRepository(
        questionDao: QuestionDao,
        examDao: ExamDao,
        examQuestionDao: ExamQuestionDao,
        questionConceptDao: QuestionConceptDao,
        quizAttemptDao: QuizAttemptDao,
        questionResponseDao: QuestionResponseDao,
        questionStatsDao: QuestionStatsDao
    ): QuizBankRepository = QuizBankRepositoryImpl(
        questionDao = questionDao,
        examDao = examDao,
        examQuestionDao = examQuestionDao,
        questionConceptDao = questionConceptDao,
        quizAttemptDao = quizAttemptDao,
        questionResponseDao = questionResponseDao,
        questionStatsDao = questionStatsDao
    )
}