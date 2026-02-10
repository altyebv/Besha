package com.zeros.basheer.feature.concept.data.relations


import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.zeros.basheer.feature.practice.data.entity.SectionConcept
import com.zeros.basheer.feature.concept.data.entity.ConceptEntity
import com.zeros.basheer.feature.concept.data.entity.ConceptReviewEntity
import com.zeros.basheer.feature.concept.data.entity.ConceptTagEntity
import com.zeros.basheer.feature.concept.data.entity.TagEntity
import com.zeros.basheer.feature.lesson.data.entity.SectionEntity

data class ConceptWithSections(
    @Embedded val concept: ConceptEntity,
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
    @Embedded val concept: ConceptEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ConceptTagEntity::class,
            parentColumn = "conceptId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)

data class ConceptWithReview(
    @Embedded val concept: ConceptEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "conceptId"
    )
    val review: ConceptReviewEntity?
)