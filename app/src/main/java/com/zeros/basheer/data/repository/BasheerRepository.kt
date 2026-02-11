package com.zeros.basheer.data.repository
//
//
import com.zeros.basheer.domain.repository.ContentRepository
import com.zeros.basheer.feature.concept.data.dao.ConceptReviewDao

import com.zeros.basheer.feature.concept.domain.model.Concept
import com.zeros.basheer.feature.concept.domain.model.Rating
import com.zeros.basheer.feature.concept.domain.model.Tag
import com.zeros.basheer.feature.concept.domain.repository.ConceptRepository
import com.zeros.basheer.feature.lesson.data.dao.BlockDao
import com.zeros.basheer.feature.lesson.data.dao.LessonDao
import com.zeros.basheer.feature.lesson.data.dao.SectionDao
import com.zeros.basheer.feature.lesson.data.entity.BlockEntity
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity
import com.zeros.basheer.feature.lesson.data.entity.SectionEntity
import com.zeros.basheer.feature.lesson.data.relations.LessonFull
import com.zeros.basheer.feature.progress.data.dao.ProgressDao
import com.zeros.basheer.feature.progress.data.entity.UserProgressEntity
import com.zeros.basheer.feature.progress.domain.model.UserProgress
import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import com.zeros.basheer.feature.progress.domain.usecase.UpdateProgressUseCase
import com.zeros.basheer.feature.quizbank.domain.model.Exam
import com.zeros.basheer.feature.quizbank.domain.model.ExamSource
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository

import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.domain.model.Units
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BasheerRepository @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val contentRepository: ContentRepository,
    private val updateProgressUseCase: UpdateProgressUseCase

) {

    fun getUnitsBySubject(subjectId: String): Flow<List<Units>> = subjectRepository.getUnitsBySubject(subjectId)


    // ==========================================
    // LESSONS
    // ==========================================


    // ==========================================
    // PROGRESS
    // ==========================================
    fun getProgressByLesson(lessonId: String): Flow<UserProgress?> = contentRepository.getProgressByLesson(lessonId)
    fun getCompletedLessons(): Flow<List<UserProgress>> = contentRepository.getCompletedLessons()

    suspend fun updateProgress(progress: UserProgress) = updateProgressUseCase(progress)



}