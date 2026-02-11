package com.zeros.basheer.feature.lesson.data.relations


import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.zeros.basheer.feature.concept.data.entity.ConceptEntity
import com.zeros.basheer.feature.lesson.data.entity.*
import com.zeros.basheer.feature.practice.data.entity.SectionConcept
import com.zeros.basheer.feature.progress.data.entity.UserProgressEntity
import com.zeros.basheer.feature.quizbank.data.entity.QuestionConceptEntity
import com.zeros.basheer.feature.quizbank.data.entity.QuestionEntity

data class LessonWithSections(
    @Embedded val lesson: LessonEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "lessonId"
    )
    val sections: List<SectionEntity>
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

data class SectionWithBlocks(
    @Embedded val section: SectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sectionId"
    )
    val blocks: List<BlockEntity>
)

data class LessonWithProgress(
    @Embedded val lesson: LessonEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "lessonId"
    )
    val progress: UserProgressEntity?
)

data class LessonFull(
    @Embedded val lessonEntity: LessonEntity,
    @Relation(
        entity = SectionEntity::class,
        parentColumn = "id",
        entityColumn = "lessonId"
    )
    val sections: List<SectionWithBlocksAndConcepts>
)

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



