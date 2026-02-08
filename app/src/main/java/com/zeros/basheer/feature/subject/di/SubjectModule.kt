package com.zeros.basheer.feature.subject.di

import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.feature.subject.data.dao.SubjectDao
import com.zeros.basheer.feature.subject.data.dao.UnitDao
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepositoryImpl
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SubjectModule {

    @Provides
    @Singleton
    fun provideSubjectDao(database: AppDatabase): SubjectDao {
        return database.subjectDao()
    }

    @Provides
    @Singleton
    fun provideUnitDao(database: AppDatabase): UnitDao {
        return database.unitDao()
    }

    @Provides
    @Singleton
    fun provideSubjectRepository(
        subjectDao: SubjectDao,
        unitDao: UnitDao
    ): SubjectRepository {
        return SubjectRepositoryImpl(subjectDao, unitDao)
    }
}