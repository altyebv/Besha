package com.zeros.basheer.di

import android.content.Context
import androidx.room.Room
import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.core.data.DatabaseSeeder
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import com.zeros.basheer.feature.lesson.data.dao.BlockDao
import com.zeros.basheer.feature.lesson.data.dao.LessonDao
import com.zeros.basheer.feature.lesson.data.dao.SectionDao
import com.zeros.basheer.feature.lesson.data.dao.SectionProgressDao
import com.zeros.basheer.feature.practice.data.dao.PracticeSessionDao
import com.zeros.basheer.feature.practice.data.dao.SectionConceptDao
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

    @Provides
    fun provideLessonDao(database: AppDatabase): LessonDao = database.lessonDao()

    @Provides
    fun provideSectionDao(database: AppDatabase): SectionDao = database.sectionDao()

    @Provides
    fun provideBlockDao(database: AppDatabase): BlockDao = database.blockDao()


    @Provides
    fun provideSectionConceptDao(database: AppDatabase): SectionConceptDao = database.sectionConceptDao()

    @Provides
    fun provideSectionProgressDao(database: AppDatabase): SectionProgressDao = database.sectionProgressDao()

    // DailyActivityDao now provided by StreakModule - removed duplicate

}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {



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