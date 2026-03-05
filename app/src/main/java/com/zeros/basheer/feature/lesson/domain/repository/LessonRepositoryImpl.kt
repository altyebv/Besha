package com.zeros.basheer.feature.lesson.domain.repository

import com.zeros.basheer.feature.lesson.data.mapper.LessonMapper

import com.zeros.basheer.core.domain.model.Result
import com.zeros.basheer.feature.lesson.data.dao.*
import com.zeros.basheer.feature.lesson.data.entity.*
import com.zeros.basheer.feature.lesson.domain.model.*
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepository
import com.zeros.basheer.feature.progress.data.dao.ProgressDao
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LessonRepositoryImpl @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val lessonDao: LessonDao,
    private val sectionDao: SectionDao,
    private val blockDao: BlockDao,
    private val sectionProgressDao: SectionProgressDao,
    private val progressDao: ProgressDao
) : LessonRepository {

    override suspend fun getLessonById(id: String): Result<LessonDomain> {
        return try {
            val entity = lessonDao.getLessonById(id)
            if (entity != null) {
                Result.Success(entity.toDomain())
            } else {
                Result.Error("Lesson not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }

    override suspend fun getNextLesson(lessonId: String): LessonDomain? =
        lessonDao.getNextLesson(lessonId)?.toDomain()

//    override suspend fun getLessonsByUnit(UnitId: String): Flow<List<LessonDomain>>{
//        return lessonDao.getLessonsByUnit(UnitId)
//            .map { entities -> entities.map { it.toDomain() } }
//    }

    override fun getLessonsBySubject(subjectId: String): Flow<List<LessonDomain>> {
        return lessonDao.getLessonsBySubject(subjectId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getLessonContent(lessonId: String): Result<com.zeros.basheer.domain.model.LessonContent> {
        return try {
            val full = lessonDao.getLessonFull(lessonId)
                ?: return Result.Error("Lesson not found")
            // LessonMapper correctly maps partIndex, learningType, metadata (hook/orientation/forwardPull)
            // so every section carries its real partIndex — this is the fix for both the
            // "part 0 = whole lesson complete" bug AND the missing intro card.
            Result.Success(LessonMapper.toLessonContent(full))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }

    // Delegate to LessonMapper so metadata (tables, headings, lists, etc.) is fully parsed
    private fun BlockEntity.toBlockUiModel() = LessonMapper.toBlockUiModelPublic(this)

    override fun getLessonsByUnit(unitId: String): Flow<List<LessonDomain>> {
        return lessonDao.getLessonsByUnit(unitId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun markLessonComplete(lessonId: String): Result<Unit> {
        return try {
            progressDao.markLessonCompleted(lessonId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }

    override suspend fun updateProgress(lessonId: String, progress: Float): Result<Unit> {
        return try {
            val existing = progressDao.getProgressByLessonOnce(lessonId)
            if (existing != null) {
                progressDao.updateProgress(existing.copy(progress = progress))
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }

    override fun observeLessonProgress(lessonId: String): Flow<Float> =
        progressDao.getProgressByLesson(lessonId)
            .map { it?.progress ?: 0f }

    override suspend fun getPartCountsForLessons(lessonIds: List<String>): Map<String, Int> {
        if (lessonIds.isEmpty()) return emptyMap()
        return sectionDao.getPartCountsByLessonIds(lessonIds)
            .associate { it.lessonId to it.partCount }
    }
}

// Extension functions for mapping
private fun LessonEntity.toDomain(): LessonDomain = LessonDomain(
    id = id,
    unitId = unitId,
    title = title,
    order = order,
    estimatedMinutes = estimatedMinutes,
    summary = summary,
    metadata = null   // LessonDomain used for list screens — metadata only needed in reader via LessonMapper
)

private fun BlockEntity.toDomain(): Block = Block(
    id = id,
    sectionId = sectionId,
    type = type,  // Direct assignment, same enum
    content = content,
    order = order,
    metadata = null  // Parse metadata JSON if needed
)