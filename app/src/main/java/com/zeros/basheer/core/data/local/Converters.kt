package com.zeros.basheer.core.data.local

import androidx.room.TypeConverter
import com.zeros.basheer.feature.lesson.data.entity.BlockType
import com.zeros.basheer.data.models.CognitiveLevel
import com.zeros.basheer.data.models.ConceptType
import com.zeros.basheer.data.models.ContentSource
import com.zeros.basheer.data.models.ExamSource
import com.zeros.basheer.data.models.FeedItemType
import com.zeros.basheer.data.models.InteractionType
import com.zeros.basheer.data.models.PracticeGenerationType
import com.zeros.basheer.data.models.PracticeSessionStatus
import com.zeros.basheer.data.models.QuestionSource
import com.zeros.basheer.data.models.QuestionType
import com.zeros.basheer.data.models.Rating
import com.zeros.basheer.feature.streak.data.entity.StreakLevel
import com.zeros.basheer.data.models.StudentPath
import com.zeros.basheer.data.models.VariantType

class Converters {


    // FeedItem enums
    @TypeConverter
    fun fromFeedItemType(value: FeedItemType): String = value.name

    @TypeConverter
    fun toFeedItemType(value: String): FeedItemType = FeedItemType.valueOf(value)

    @TypeConverter
    fun fromInteractionType(value: InteractionType?): String? = value?.name

    @TypeConverter
    fun toInteractionType(value: String?): InteractionType? = value?.let { InteractionType.valueOf(it) }

    // ContentVariant enums
    @TypeConverter
    fun fromVariantType(value: VariantType): String = value.name

    @TypeConverter
    fun toVariantType(value: String): VariantType = VariantType.valueOf(value)

    @TypeConverter
    fun fromContentSource(value: ContentSource): String = value.name

    @TypeConverter
    fun toContentSource(value: String): ContentSource = ContentSource.valueOf(value)

    // StudentPath
    @TypeConverter
    fun fromStudentPath(value: StudentPath): String = value.name

    @TypeConverter
    fun toStudentPath(value: String): StudentPath = StudentPath.valueOf(value)

    // BlockType
    @TypeConverter
    fun fromBlockType(value: BlockType): String = value.name

    @TypeConverter
    fun toBlockType(value: String): BlockType = BlockType.valueOf(value)

    // ConceptType
    @TypeConverter
    fun fromConceptType(value: ConceptType): String = value.name

    @TypeConverter
    fun toConceptType(value: String): ConceptType = ConceptType.valueOf(value)

    // QuestionType
    @TypeConverter
    fun fromQuestionType(value: QuestionType): String = value.name

    @TypeConverter
    fun toQuestionType(value: String): QuestionType = QuestionType.valueOf(value)

    // ExamSource
    @TypeConverter
    fun fromExamSource(value: ExamSource): String = value.name

    @TypeConverter
    fun toExamSource(value: String): ExamSource = ExamSource.valueOf(value)

    // Rating
    @TypeConverter
    fun fromRating(value: Rating): String = value.name

    @TypeConverter
    fun toRating(value: String): Rating = Rating.valueOf(value)

    // StreakLevel
    @TypeConverter
    fun fromStreakLevel(value: StreakLevel): String = value.name

    @TypeConverter
    fun toStreakLevel(value: String): StreakLevel = StreakLevel.valueOf(value)

    // QuestionSource
    @TypeConverter
    fun fromQuestionSource(value: QuestionSource): String = value.name

    @TypeConverter
    fun toQuestionSource(value: String): QuestionSource = QuestionSource.valueOf(value)

    // CognitiveLevel
    @TypeConverter
    fun fromCognitiveLevel(value: CognitiveLevel): String = value.name

    @TypeConverter
    fun toCognitiveLevel(value: String): CognitiveLevel = CognitiveLevel.valueOf(value)

    // PracticeGenerationType
    @TypeConverter
    fun fromPracticeGenerationType(value: PracticeGenerationType): String = value.name

    @TypeConverter
    fun toPracticeGenerationType(value: String): PracticeGenerationType = PracticeGenerationType.valueOf(value)

    // PracticeSessionStatus
    @TypeConverter
    fun fromPracticeSessionStatus(value: PracticeSessionStatus): String = value.name

    @TypeConverter
    fun toPracticeSessionStatus(value: String): PracticeSessionStatus = PracticeSessionStatus.valueOf(value)
}