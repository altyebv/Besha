package com.zeros.basheer.di

import android.content.Context
import androidx.room.Room
import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.data.repository.BasheerRepository
import com.zeros.basheer.data.repository.DatabaseSeeder
import com.zeros.basheer.feature.concept.data.dao.ConceptReviewDao
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import com.zeros.basheer.feature.lesson.data.dao.BlockDao
import com.zeros.basheer.feature.lesson.data.dao.LessonDao
import com.zeros.basheer.feature.lesson.data.dao.SectionDao
import com.zeros.basheer.feature.lesson.data.dao.SectionProgressDao
import com.zeros.basheer.feature.practice.data.dao.PracticeSessionDao
import com.zeros.basheer.feature.practice.data.dao.SectionConceptDao
import com.zeros.basheer.feature.progress.data.dao.ProgressDao
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
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
    // SubjectDao and UnitDao now provided by SubjectModule

//    @Provides
//    fun provideContentVariantDao(database: AppDatabase): ContentVariantDao = database.contentVariantDao()

    @Provides
    fun provideLessonDao(database: AppDatabase): LessonDao = database.lessonDao()

    @Provides
    fun provideSectionDao(database: AppDatabase): SectionDao = database.sectionDao()

    @Provides
    fun provideBlockDao(database: AppDatabase): BlockDao = database.blockDao()

    // Concepts & categorization DAOs
//    @Provides
//    fun provideConceptDao(database: AppDatabase): ConceptDao = database.conceptDao()

//    @Provides
//    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

//    @Provides
//    fun provideConceptTagDao(database: AppDatabase): ConceptTagDao = database.conceptTagDao()

    @Provides
    fun provideSectionConceptDao(database: AppDatabase): SectionConceptDao = database.sectionConceptDao()

    // Quiz system DAOs




    // Feed DAO
//    @Provides
//    fun provideFeedItemDao(database: AppDatabase): FeedItemDao = database.feedItemDao()

    // ProgressDao now provided by ProgressModule - removed duplicate

    // Progress tracking DAOs (others)

//    @Provides
//    fun provideConceptReviewDao(database: AppDatabase): ConceptReviewDao = database.conceptReviewDao()





    @Provides
    fun provideSectionProgressDao(database: AppDatabase): SectionProgressDao = database.sectionProgressDao()

    // DailyActivityDao now provided by StreakModule - removed duplicate




}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideBasheerRepository(
        subjectRepository: com.zeros.basheer.feature.subject.domain.repository.SubjectRepository,
        lessonDao: LessonDao,
        sectionDao: SectionDao,
        blockDao: BlockDao,
        quizBankRepository: QuizBankRepository,
        progressDao: ProgressDao,
        conceptReviewDao: ConceptReviewDao,
        conceptRepository: ConceptRepository
    ): BasheerRepository {
        return BasheerRepository(
            subjectRepository = subjectRepository,
            quizBankRepository = quizBankRepository,
            lessonDao = lessonDao,
            sectionDao = sectionDao,
            blockDao = blockDao,
            conceptRepository = conceptRepository,
            progressDao = progressDao,
            conceptReviewDao = conceptReviewDao,

        )
    }

    @Provides
    @Singleton
    fun provideDatabaseSeeder(
        subjectRepository: com.zeros.basheer.feature.subject.domain.repository.SubjectRepository,
        quizBankRepository: QuizBankRepository,
        lessonDao: LessonDao,
        sectionDao: SectionDao,
        blockDao: BlockDao,
        sectionConceptDao: SectionConceptDao,
        practiceSessionDao: PracticeSessionDao,
        conceptRepository: ConceptRepository,
        feedRepository: com.zeros.basheer.feature.feed.domain.repository.FeedRepository
    ): DatabaseSeeder {
        return DatabaseSeeder(
            subjectRepository = subjectRepository,
            conceptRepository = conceptRepository,
            quizBankRepository = quizBankRepository,
            lessonDao = lessonDao,
            sectionDao = sectionDao,
            blockDao = blockDao,
            sectionConceptDao = sectionConceptDao,
            practiceSessionDao = practiceSessionDao,
            feedRepository = feedRepository
        )
    }
}