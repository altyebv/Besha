package com.zeros.basheer.data.local

import androidx.room.TypeConverter
import com.zeros.basheer.data.models.ConceptType
import com.zeros.basheer.data.models.Rating
import com.zeros.basheer.data.models.StudentPath

class Converters {
    @TypeConverter
    fun fromStudentPath(value: StudentPath): String = value.name

    @TypeConverter
    fun toStudentPath(value: String): StudentPath = StudentPath.valueOf(value)

    @TypeConverter
    fun fromConceptType(value: ConceptType): String = value.name

    @TypeConverter
    fun toConceptType(value: String): ConceptType = ConceptType.valueOf(value)

    @TypeConverter
    fun fromRating(value: Rating): String = value.name

    @TypeConverter
    fun toRating(value: String): Rating = Rating.valueOf(value)
}