package com.zeros.basheer.feature.lesson.domain.repository


import com.zeros.basheer.core.domain.model.Result
import com.zeros.basheer.domain.model.LessonContent
import com.zeros.basheer.feature.lesson.data.dao.*
import com.zeros.basheer.feature.lesson.data.entity.*
import com.zeros.basheer.feature.lesson.domain.model.*
import com.zeros.basheer.feature.lesson.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao,
    private val sectionDao: SectionDao,
    private val blockDao: BlockDao,
    private val sectionProgressDao: SectionProgressDao
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

    override suspend fun getLessonContent(lessonId: String): Result<com.zeros.basheer.domain.model.LessonContent> {
        return try {
            val lesson = lessonDao.getLessonById(lessonId)
                ?: return Result.Error("Lesson not found")

            val sections = sectionDao.getSectionsByLessonId(lessonId)
            val sectionIds = sections.map { it.id }
            val blocks = if (sectionIds.isNotEmpty()) {
                blockDao.getBlocksBySectionIds(sectionIds)
            } else {
                emptyList()
            }

            // Map to OLD UI model (SectionUiModel, BlockUiModel)
            val sectionsUi = sections.map { section ->
                com.zeros.basheer.domain.model.SectionUiModel(
                    id = section.id,
                    title = section.title,
                    order = section.order,
                    blocks = blocks.filter { it.sectionId == section.id }
                        .sortedBy { it.order }
                        .map { it.toBlockUiModel() }
                )
            }.sortedBy { it.order }

            Result.Success(
                com.zeros.basheer.domain.model.LessonContent(
                    id = lesson.id,
                    title = lesson.title,
                    estimatedMinutes = lesson.estimatedMinutes,
                    summary = lesson.summary,
                    sections = sectionsUi
                )
            )
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }

    // Mapper extension
    private fun BlockEntity.toBlockUiModel() = com.zeros.basheer.domain.model.BlockUiModel(
        id = id,
        type = type,
        content = content,
        order = order,
        conceptRef = conceptRef,
        caption = caption,
        metadata = null  // Parse metadata if needed
    )

    override fun getLessonsByUnit(unitId: String): Flow<List<LessonDomain>> {
        return lessonDao.getLessonsByUnit(unitId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun markLessonComplete(lessonId: String): Result<Unit> {
        return try {
            // Update logic here based on your progress tracking
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }

    override suspend fun updateProgress(lessonId: String, progress: Float): Result<Unit> {
        return try {
            // Update logic here
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }

    override fun observeLessonProgress(lessonId: String): Flow<Float> {
        // Implement based on your progress tracking
        return kotlinx.coroutines.flow.flowOf(0f)
    }
}

// Extension functions for mapping
private fun LessonEntity.toDomain(): LessonDomain = LessonDomain(
    id = id,
    unitId = unitId,
    title = title,
    order = order,
    estimatedMinutes = estimatedMinutes,
    summary = summary
)

private fun BlockEntity.toDomain(): Block = Block(
    id = id,
    sectionId = sectionId,
    type = type,  // Direct assignment, same enum
    content = content,
    order = order,
    metadata = null  // Parse metadata JSON if needed
)