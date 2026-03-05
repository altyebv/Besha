package com.zeros.basheer.feature.lesson.domain.repository


import com.zeros.basheer.core.domain.model.Result
import com.zeros.basheer.domain.model.LessonContent
import com.zeros.basheer.feature.lesson.domain.model.*
import kotlinx.coroutines.flow.Flow

interface LessonRepository {
    suspend fun getLessonById(id: String): Result<LessonDomain>
    /** Returns the next lesson in the same unit, or null if this is the last one. */
    suspend fun getNextLesson(lessonId: String): LessonDomain?
    fun getLessonsBySubject(subjectId: String): Flow<List<LessonDomain>>
    suspend fun getLessonContent(lessonId: String): Result<LessonContent>
    fun getLessonsByUnit(unitId: String): Flow<List<LessonDomain>>
    suspend fun markLessonComplete(lessonId: String): Result<Unit>
    suspend fun updateProgress(lessonId: String, progress: Float): Result<Unit>
    fun observeLessonProgress(lessonId: String): Flow<Float>
    /** Returns a map of lessonId → distinct part count for all supplied lesson IDs. */
    suspend fun getPartCountsForLessons(lessonIds: List<String>): Map<String, Int>
}