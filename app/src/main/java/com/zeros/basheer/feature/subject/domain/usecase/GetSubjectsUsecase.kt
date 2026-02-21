package com.zeros.basheer.feature.subject.domain.usecase

import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import com.zeros.basheer.feature.user.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

/**
 * Use case for retrieving subjects.
 *
 * The default [invoke] filters by the current user's path (+ COMMON), so
 * every screen that calls it automatically shows the right subjects without
 * any extra wiring.
 *
 * Use [all] only for admin / seeding / analytics screens that need every subject.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetSubjectsUseCase @Inject constructor(
    private val repository: SubjectRepository,
    private val userProfileRepository: UserProfileRepository
) {
    /**
     * Get subjects for the current user's path (includes COMMON subjects).
     * Reactively updates if the user changes their path.
     */
    operator fun invoke(): Flow<List<Subject>> =
        userProfileRepository.getProfile().flatMapLatest { profile ->
            if (profile == null) {
                // No profile yet (shouldn't happen post-onboarding, but be safe)
                repository.getAllSubjects()
            } else {
                // Filter by the user's path filter [path, COMMON]
                repository.getSubjectsByPathFilter(profile.subjectsFilter)
            }
        }

    /** Get all subjects regardless of path — for admin/debug use. */
    fun all(): Flow<List<Subject>> = repository.getAllSubjects()

    /** Get subjects for a specific path explicitly. */
    fun byPath(path: StudentPath): Flow<List<Subject>> =
        repository.getSubjectsByPath(path)

    /** Get a specific subject by ID. */
    suspend fun byId(subjectId: String): Subject? =
        repository.getSubjectById(subjectId)
}