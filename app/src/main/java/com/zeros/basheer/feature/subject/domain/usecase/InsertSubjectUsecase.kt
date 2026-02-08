package com.zeros.basheer.feature.subject.domain.usecase

import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.domain.repository.SubjectRepository
import javax.inject.Inject

/**
 * Use case for inserting subjects (used by data seeding)
 */
class InsertSubjectsUseCase @Inject constructor(
    private val repository: SubjectRepository
) {
    /**
     * Insert a single subject
     */
    suspend operator fun invoke(subject: Subject) {
        repository.insertSubject(subject)
    }

    /**
     * Insert multiple subjects (batch operation)
     */
    suspend fun insertMany(subjects: List<Subject>) {
        repository.insertSubjects(subjects)
    }
}