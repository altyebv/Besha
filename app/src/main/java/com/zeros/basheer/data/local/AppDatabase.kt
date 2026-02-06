package com.zeros.basheer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zeros.basheer.data.local.dao.*
import com.zeros.basheer.data.models.*


@Database(
    entities = [
        // Core content
        Subject::class,
        Units::class,
        Lesson::class,
        Section::class,
        Block::class,
        ContentVariant::class,
        
        // Concepts & categorization
        Concept::class,
        Tag::class,
        ConceptTag::class,
        SectionConcept::class,
        
        // Quiz system
        Question::class,
        QuestionConcept::class,
        Exam::class,
        ExamQuestion::class,

        // Feed
        FeedItem::class,


        
        // Progress tracking
        UserProgress::class,
        ConceptReview::class,
        QuizAttempt::class,
        QuestionResponse::class,
        SectionProgress::class,
        DailyActivity::class
    ],
    version = 2,  // Bumped for DailyActivity entity
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // Core content
    abstract fun subjectDao(): SubjectDao
    abstract fun unitDao(): UnitDao
    abstract fun lessonDao(): LessonDao
    abstract fun sectionDao(): SectionDao
    abstract fun blockDao(): BlockDao
    abstract fun contentVariantDao(): ContentVariantDao
    
    // Concepts & categorization
    abstract fun conceptDao(): ConceptDao
    abstract fun tagDao(): TagDao
    abstract fun conceptTagDao(): ConceptTagDao
    abstract fun sectionConceptDao(): SectionConceptDao
    
    // Quiz system
    abstract fun questionDao(): QuestionDao
    abstract fun questionConceptDao(): QuestionConceptDao
    abstract fun examDao(): ExamDao
    abstract fun examQuestionDao(): ExamQuestionDao

    // Feed
    abstract fun feedItemDao(): FeedItemDao
    
    // Progress tracking
    abstract fun progressDao(): ProgressDao
    abstract fun conceptReviewDao(): ConceptReviewDao
    abstract fun quizAttemptDao(): QuizAttemptDao
    abstract fun questionResponseDao(): QuestionResponseDao
    abstract fun sectionProgressDao(): SectionProgressDao
    abstract fun dailyActivityDao(): DailyActivityDao
}
