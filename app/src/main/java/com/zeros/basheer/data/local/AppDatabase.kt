package com.zeros.basheer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zeros.basheer.data.local.dao.*
//import com.zeros.basheer.data.models.*

import com.zeros.basheer.data.models.Lesson
import com.zeros.basheer.data.models.QuestionStats
import com.zeros.basheer.data.models.Section
import com.zeros.basheer.data.models.Units
import com.zeros.basheer.data.models.Subject
import com.zeros.basheer.data.models.Concept
import com.zeros.basheer.data.models.Tag
import com.zeros.basheer.data.models.Block
import com.zeros.basheer.data.models.ConceptReview
import com.zeros.basheer.data.models.ConceptTag
import com.zeros.basheer.data.models.ContentVariant
import com.zeros.basheer.data.models.DailyActivity
import com.zeros.basheer.data.models.Exam
import com.zeros.basheer.data.models.ExamQuestion
import com.zeros.basheer.data.models.FeedItem
import com.zeros.basheer.data.models.PracticeQuestion
import com.zeros.basheer.data.models.PracticeSession
import com.zeros.basheer.data.models.Question
import com.zeros.basheer.data.models.QuestionConcept
import com.zeros.basheer.data.models.QuestionResponse
import com.zeros.basheer.data.models.QuizAttempt
import com.zeros.basheer.data.models.SectionConcept
import com.zeros.basheer.data.models.SectionProgress
import com.zeros.basheer.data.models.UserProgress


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
        QuestionStats::class,

        // Practice sessions
        PracticeSession::class,
        PracticeQuestion::class,

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
    version = 3,
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
    abstract fun questionStatsDao(): QuestionStatsDao

    // Practice sessions
    abstract fun practiceSessionDao(): PracticeSessionDao

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
