package com.zeros.basheer.data.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.zeros.basheer.data.models.*
import com.zeros.basheer.feature.concept.data.entity.ConceptEntity
import com.zeros.basheer.feature.lesson.data.entity.BlockEntity
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity
import com.zeros.basheer.feature.lesson.data.entity.SectionEntity
import com.zeros.basheer.feature.practice.data.entity.SectionConcept
import com.zeros.basheer.feature.progress.domain.model.UserProgress
import com.zeros.basheer.feature.quizbank.data.entity.ExamEntity
import com.zeros.basheer.feature.quizbank.data.entity.QuestionConceptEntity
import com.zeros.basheer.feature.quizbank.data.entity.QuestionEntity
import com.zeros.basheer.feature.quizbank.domain.model.Question
import com.zeros.basheer.feature.subject.data.entity.UnitEntity
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.subject.domain.model.Units

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
    val concepts: List<ConceptEntity>
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
    val concepts: List<ConceptEntity>
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


data class SubjectWithUnits(
    @Embedded val subject: Subject,
    @Relation(
        entity = UnitEntity::class,
        parentColumn = "id",
        entityColumn = "subjectId"
    )
    val units: List<UnitWithLessons>
)

// ============================================
// QUIZ RELATIONS
// ============================================

data class QuestionWithConcepts(
    @Embedded val question: QuestionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = QuestionConceptEntity::class,
            parentColumn = "questionId",
            entityColumn = "conceptId"
        )
    )
    val concepts: List<ConceptEntity>
)





// ============================================
// PROGRESS RELATIONS
// ============================================




