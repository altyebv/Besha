package com.zeros.basheer.feature.lesson.data.relations


import androidx.room.Embedded
import androidx.room.Relation
import com.zeros.basheer.feature.lesson.data.entity.*
import com.zeros.basheer.feature.progress.data.entity.UserProgressEntity
import com.zeros.basheer.feature.progress.domain.model.UserProgress

data class LessonWithSections(
    @Embedded val lesson: LessonEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "lessonId"
    )
    val sections: List<SectionEntity>
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

