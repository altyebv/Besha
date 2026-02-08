package com.zeros.basheer.feature.lesson.di


import com.zeros.basheer.feature.lesson.data.dao.*
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepositoryImpl
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LessonModule {

    @Provides
    @Singleton
    fun provideLessonRepository(
        lessonDao: LessonDao,
        sectionDao: SectionDao,
        blockDao: BlockDao,
        sectionProgressDao: SectionProgressDao
    ): LessonRepository {
        return LessonRepositoryImpl(
            lessonDao = lessonDao,
            sectionDao = sectionDao,
            blockDao = blockDao,
            sectionProgressDao = sectionProgressDao
        )
    }
}