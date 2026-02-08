package com.zeros.basheer.di

import android.content.Context
import androidx.room.Room
import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.data.local.dao.*
import com.zeros.basheer.data.repository.BasheerRepository
import com.zeros.basheer.data.repository.DatabaseSeeder
import com.zeros.basheer.feature.lesson.data.dao.BlockDao
import com.zeros.basheer.feature.lesson.data.dao.LessonDao
import com.zeros.basheer.feature.lesson.data.dao.SectionDao
import com.zeros.basheer.feature.lesson.data.dao.SectionProgressDao
import com.zeros.basheer.feature.progress.data.dao.ProgressDao
import com.zeros.basheer.feature.subject.data.dao.SubjectDao
import com.zeros.basheer.feature.subject.data.dao.UnitDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "basheer_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // Core content DAOs
    @Provides
    fun provideSubjectDao(database: AppDatabase): SubjectDao = database.subjectDao()


    @Provides
    fun provideContentVariantDao(database: AppDatabase): ContentVariantDao = database.contentVariantDao()


    @Provides
    fun provideUnitDao(database: AppDatabase): UnitDao = database.unitDao()

    @Provides
    fun provideLessonDao(database: AppDatabase): LessonDao = database.lessonDao()

    @Provides
    fun provideSectionDao(database: AppDatabase): SectionDao = database.sectionDao()

    @Provides
    fun provideBlockDao(database: AppDatabase): BlockDao = database.blockDao()

    // Concepts & categorization DAOs
    @Provides
    fun provideConceptDao(database: AppDatabase): ConceptDao = database.conceptDao()

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

    @Provides
    fun provideConceptTagDao(database: AppDatabase): ConceptTagDao = database.conceptTagDao()

    @Provides
    fun provideSectionConceptDao(database: AppDatabase): SectionConceptDao = database.sectionConceptDao()

    // Quiz system DAOs
    @Provides
    fun provideQuestionDao(database: AppDatabase): QuestionDao = database.questionDao()

    @Provides
    fun provideQuestionConceptDao(database: AppDatabase): QuestionConceptDao = database.questionConceptDao()

    @Provides
    fun provideExamDao(database: AppDatabase): ExamDao = database.examDao()

    @Provides
    fun provideExamQuestionDao(database: AppDatabase): ExamQuestionDao = database.examQuestionDao()

    // Feed DAO
    @Provides
    fun provideFeedItemDao(database: AppDatabase): FeedItemDao = database.feedItemDao()

    // ProgressDao now provided by ProgressModule - removed duplicate

    // Progress tracking DAOs (others)

    @Provides
    fun provideConceptReviewDao(database: AppDatabase): ConceptReviewDao = database.conceptReviewDao()

    @Provides
    fun provideQuizAttemptDao(database: AppDatabase): QuizAttemptDao = database.quizAttemptDao()

    @Provides
    fun provideQuestionResponseDao(database: AppDatabase): QuestionResponseDao = database.questionResponseDao()

    @Provides
    fun provideSectionProgressDao(database: AppDatabase): SectionProgressDao = database.sectionProgressDao()

    // DailyActivityDao now provided by StreakModule - removed duplicate

    @Provides
    fun provideQuestionStatsDao(database: AppDatabase): QuestionStatsDao = database.questionStatsDao()

    @Provides
    fun providePracticeSessionDao(database: AppDatabase): PracticeSessionDao = database.practiceSessionDao()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideBasheerRepository(
        subjectDao: SubjectDao,
        unitDao: UnitDao,
        lessonDao: LessonDao,
        sectionDao: SectionDao,
        blockDao: BlockDao,
        conceptDao: ConceptDao,
        tagDao: TagDao,
        conceptTagDao: ConceptTagDao,
        sectionConceptDao: SectionConceptDao,
        questionDao: QuestionDao,
        questionConceptDao: QuestionConceptDao,
        examDao: ExamDao,
        examQuestionDao: ExamQuestionDao,
        progressDao: ProgressDao,
        conceptReviewDao: ConceptReviewDao,
        quizAttemptDao: QuizAttemptDao,
        questionResponseDao: QuestionResponseDao
    ): BasheerRepository {
        return BasheerRepository(
            subjectDao = subjectDao,
            unitDao = unitDao,
            lessonDao = lessonDao,
            sectionDao = sectionDao,
            blockDao = blockDao,
            conceptDao = conceptDao,
            tagDao = tagDao,
            conceptTagDao = conceptTagDao,
            sectionConceptDao = sectionConceptDao,
            questionDao = questionDao,
            questionConceptDao = questionConceptDao,
            examDao = examDao,
            examQuestionDao = examQuestionDao,
            progressDao = progressDao,
            conceptReviewDao = conceptReviewDao,
            quizAttemptDao = quizAttemptDao,
            questionResponseDao = questionResponseDao
        )
    }

    @Provides
    @Singleton
    fun provideDatabaseSeeder(
        subjectDao: SubjectDao,
        unitDao: UnitDao,
        lessonDao: LessonDao,
        sectionDao: SectionDao,
        blockDao: BlockDao,
        conceptDao: ConceptDao,
        tagDao: TagDao,
        conceptTagDao: ConceptTagDao,
        sectionConceptDao: SectionConceptDao,
        questionDao: QuestionDao,
        questionConceptDao: QuestionConceptDao,
        examDao: ExamDao,
        examQuestionDao: ExamQuestionDao,
        feedItemDao: FeedItemDao,
        practiceSessionDao: PracticeSessionDao,
        questionStatsDao: QuestionStatsDao
    ): DatabaseSeeder {
        return DatabaseSeeder(
            subjectDao = subjectDao,
            unitDao = unitDao,
            lessonDao = lessonDao,
            sectionDao = sectionDao,
            blockDao = blockDao,
            conceptDao = conceptDao,
            tagDao = tagDao,
            conceptTagDao = conceptTagDao,
            sectionConceptDao = sectionConceptDao,
            questionDao = questionDao,
            questionConceptDao = questionConceptDao,
            examDao = examDao,
            examQuestionDao = examQuestionDao,
            feedItemDao = feedItemDao,
            practiceSessionDao = practiceSessionDao,
            questionStatsDao = questionStatsDao
        )
    }
}