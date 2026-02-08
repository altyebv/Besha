package com.zeros.basheer.feature.subject.data.relations


import androidx.room.Embedded
import androidx.room.Relation
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity
import com.zeros.basheer.feature.subject.data.entity.SubjectEntity
import com.zeros.basheer.feature.subject.data.entity.UnitEntity

/**
 * Room relation: Unit with its Lessons
 */
data class UnitWithLessons(
    @Embedded val unit: UnitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "unitId"
    )
    val lessonEntities: List<LessonEntity>
)

/**
 * Room relation: Subject with its Units (and nested lessons)
 */
data class SubjectWithUnits(
    @Embedded val subject: SubjectEntity,
    @Relation(
        entity = UnitEntity::class,
        parentColumn = "id",
        entityColumn = "subjectId"
    )
    val units: List<UnitWithLessons>
)