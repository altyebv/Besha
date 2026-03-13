package com.zeros.basheer.feature.progress.domain.usecase

import com.zeros.basheer.feature.progress.domain.repository.ProgressRepository
import javax.inject.Inject

/**
 * @deprecated Section progress is now the single source of truth in [SectionProgressEntity].
 * Lesson completion is determined by [SectionProgressDao.isLessonFullyCompleted].
 * To mark a lesson complete, call [MarkLessonCompleteUseCase] after confirming all sections done.
 *
 * This class is kept to avoid breaking call sites but performs no operations.
 */
@Deprecated("Use SectionProgressDao.isLessonFullyCompleted() + MarkLessonCompleteUseCase instead")
class UpdateProgressFromSectionsUseCase @Inject constructor(
    @Suppress("UNUSED_PARAMETER") private val repository: ProgressRepository
) {
    @Suppress("UNUSED_PARAMETER")
    suspend operator fun invoke(lessonId: String, totalSections: Int) {
        // No-op: completedSections and progress fields removed from user_progress.
        // Progress is now computed from section_progress table on demand.
    }
}