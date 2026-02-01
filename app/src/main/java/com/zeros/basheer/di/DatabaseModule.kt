package com.zeros.basheer.di

import android.content.Context
import androidx.room.Room
import com.zeros.basheer.data.local.AppDatabase
import com.zeros.basheer.data.local.dao.*
import com.zeros.basheer.data.repository.DatabaseSeeder
import com.zeros.basheer.data.repository.LessonRepository
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
    fun provideSubjectDao(database: AppDatabase): SubjectDao {
        return database.subjectDao()
    }

    @Provides
    fun provideUnitDao(database: AppDatabase): UnitDao {
        return database.unitDao()
    }

    @Provides
    fun provideLessonDao(database: AppDatabase): LessonDao {
        return database.lessonDao()
    }

    @Provides
    fun provideProgressDao(database: AppDatabase): ProgressDao {
        return database.progressDao()
    }

    @Provides
    fun provideConceptDao(database: AppDatabase): ConceptDao {
        return database.conceptDao()
    }

    @Provides
    fun provideConceptReviewDao(database: AppDatabase): ConceptReviewDao {
        return database.conceptReviewDao()
    }

    // Add DatabaseSeeder provider HERE (inside DatabaseModule object)
    @Provides
    @Singleton
    fun provideDatabaseSeeder(
        subjectDao: SubjectDao,
        unitDao: UnitDao,
        lessonDao: LessonDao,
        conceptDao: ConceptDao
    ): DatabaseSeeder {
        return DatabaseSeeder(subjectDao, unitDao, lessonDao, conceptDao)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideLessonRepository(
        subjectDao: SubjectDao,
        unitDao: UnitDao,
        lessonDao: LessonDao,
        progressDao: ProgressDao,
        conceptDao: ConceptDao,
        conceptReviewDao: ConceptReviewDao
    ): LessonRepository {
        return LessonRepository(
            subjectDao,
            unitDao,
            lessonDao,
            progressDao,
            conceptDao,
            conceptReviewDao
        )
    }
}