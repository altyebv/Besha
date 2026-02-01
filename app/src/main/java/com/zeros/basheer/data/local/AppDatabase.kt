package com.zeros.basheer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zeros.basheer.data.local.dao.*
import com.zeros.basheer.data.models.*

@Database(
    entities = [
        Subject::class,        // No foreign keys - comes first
        Units::class,           // References Subject
        Lesson::class,         // References Unit
        Concept::class,        // References Subject
        UserProgress::class,   // References Lesson
        ConceptReview::class   // References Concept
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun unitDao(): UnitDao
    abstract fun lessonDao(): LessonDao
    abstract fun progressDao(): ProgressDao
    abstract fun conceptDao(): ConceptDao
    abstract fun conceptReviewDao(): ConceptReviewDao
}