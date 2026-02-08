package com.zeros.basheer.data.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.zeros.basheer.data.models.*
import com.zeros.basheer.feature.lesson.data.entity.BlockEntity
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity
import com.zeros.basheer.feature.lesson.data.entity.SectionEntity

// ============================================
// CONTENT RELATIONS
// ============================================

data class UnitWithLessons(
    @Embedded val unit: Units,
    @Relation(
        parentColumn = "id",
        entityColumn = "unitId"
    )
    val lessonEntities: List<LessonEntity>
)

data class LessonWithSections(
    @Embedded val lessonEntity: LessonEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "lessonId"
    )
    val sectionEntities: List<SectionEntity>
)

data class SectionWithBlocks(
    @Embedded val sectionEntity: SectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sectionId"
    )
    val blocks: List<BlockEntity>
)

data class SectionWithConcepts(
    @Embedded val sectionEntity: SectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SectionConcept::class,
            parentColumn = "sectionId",
            entityColumn = "conceptId"
        )
    )
    val concepts: List<Concept>
)

data class SectionWithBlocksAndConcepts(
    @Embedded val sectionEntity: SectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sectionId"
    )
    val blocks: List<BlockEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SectionConcept::class,
            parentColumn = "sectionId",
            entityColumn = "conceptId"
        )
    )
    val concepts: List<Concept>
)

/**
 * Full lesson content for display
 */
data class LessonFull(
    @Embedded val lessonEntity: LessonEntity,
    @Relation(
        entity = SectionEntity::class,
        parentColumn = "id",
        entityColumn = "lessonId"
    )
    val sections: List<SectionWithBlocksAndConcepts>
)

data class ConceptWithSections(
    @Embedded val concept: Concept,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SectionConcept::class,
            parentColumn = "conceptId",
            entityColumn = "sectionId"
        )
    )
    val sectionEntities: List<SectionEntity>
)

data class ConceptWithTags(
    @Embedded val concept: Concept,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ConceptTag::class,
            parentColumn = "conceptId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)

data class SubjectWithUnits(
    @Embedded val subject: Subject,
    @Relation(
        entity = Units::class,
        parentColumn = "id",
        entityColumn = "subjectId"
    )
    val units: List<UnitWithLessons>
)

// ============================================
// QUIZ RELATIONS
// ============================================

data class QuestionWithConcepts(
    @Embedded val question: Question,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = QuestionConcept::class,
            parentColumn = "questionId",
            entityColumn = "conceptId"
        )
    )
    val concepts: List<Concept>
)

data class ExamWithQuestions(
    @Embedded val exam: Exam,
    @Relation(
        entity = Question::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ExamQuestion::class,
            parentColumn = "examId",
            entityColumn = "questionId"
        )
    )
    val questions: List<Question>
)

data class QuizAttemptWithResponses(
    @Embedded val attempt: QuizAttempt,
    @Relation(
        parentColumn = "id",
        entityColumn = "attemptId"
    )
    val responses: List<QuestionResponse>
)

// ============================================
// PROGRESS RELATIONS
// ============================================

data class LessonWithProgress(
    @Embedded val lessonEntity: LessonEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "lessonId"
    )
    val progress: UserProgress?
)

data class ConceptWithReview(
    @Embedded val concept: Concept,
    @Relation(
        parentColumn = "id",
        entityColumn = "conceptId"
    )
    val review: ConceptReview?
)
