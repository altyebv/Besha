package com.zeros.basheer.di


import com.zeros.basheer.core.data.local.AppDatabase
import com.zeros.basheer.domain.repository.ContentRepository
import com.zeros.basheer.feature.lesson.data.dao.LessonDao
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContentRepositoryModule {

    @Provides
    @Singleton
    fun provideContentRepository(
        subjectRepository: SubjectRepository,
        lessonDao: LessonDao,
        progressRepository: ProgressRepository
    ): ContentRepository {
        return ContentRepository(
            subjectRepository = subjectRepository,
            lessonDao = lessonDao,
            progressRepository = progressRepository
        )
    }
}
