package com.zeros.basheer.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zeros.basheer.feature.lesson.data.dao.BlockDao
import com.zeros.basheer.data.local.dao.ConceptDao
import com.zeros.basheer.data.local.dao.ConceptReviewDao
import com.zeros.basheer.data.local.dao.ConceptTagDao
import com.zeros.basheer.data.local.dao.ContentVariantDao
import com.zeros.basheer.feature.streak.data.dao.DailyActivityDao
import com.zeros.basheer.data.local.dao.ExamDao
import com.zeros.basheer.data.local.dao.ExamQuestionDao
import com.zeros.basheer.data.local.dao.FeedItemDao
import com.zeros.basheer.feature.lesson.data.dao.LessonDao
import com.zeros.basheer.data.local.dao.PracticeSessionDao
import com.zeros.basheer.data.local.dao.QuestionConceptDao
import com.zeros.basheer.data.local.dao.QuestionDao
import com.zeros.basheer.data.local.dao.QuestionResponseDao
import com.zeros.basheer.data.local.dao.QuestionStatsDao
import com.zeros.basheer.data.local.dao.QuizAttemptDao
import com.zeros.basheer.data.local.dao.SectionConceptDao
import com.zeros.basheer.feature.lesson.data.dao.SectionDao
import com.zeros.basheer.feature.lesson.data.dao.SectionProgressDao
import com.zeros.basheer.data.local.dao.SubjectDao
import com.zeros.basheer.data.local.dao.TagDao
import com.zeros.basheer.data.local.dao.UnitDao
import com.zeros.basheer.data.models.Concept
import com.zeros.basheer.data.models.ConceptReview
import com.zeros.basheer.data.models.ConceptTag
import com.zeros.basheer.data.models.ContentVariant
import com.zeros.basheer.data.models.Exam
import com.zeros.basheer.data.models.ExamQuestion
import com.zeros.basheer.data.models.FeedItem
import com.zeros.basheer.feature.lesson.data.entity.LessonEntity
import com.zeros.basheer.data.models.PracticeQuestion
import com.zeros.basheer.data.models.PracticeSession
import com.zeros.basheer.data.models.Question
import com.zeros.basheer.data.models.QuestionConcept
import com.zeros.basheer.data.models.QuestionResponse
import com.zeros.basheer.data.models.QuestionStats
import com.zeros.basheer.data.models.QuizAttempt
import com.zeros.basheer.feature.lesson.data.entity.SectionEntity
import com.zeros.basheer.data.models.SectionConcept
import com.zeros.basheer.feature.lesson.data.entity.SectionProgressEntity
import com.zeros.basheer.data.models.Subject
import com.zeros.basheer.data.models.Tag
import com.zeros.basheer.data.models.Units
import com.zeros.basheer.feature.lesson.data.entity.BlockEntity
import com.zeros.basheer.feature.progress.data.dao.ProgressDao
import com.zeros.basheer.feature.progress.data.entity.UserProgressEntity
import com.zeros.basheer.feature.streak.data.entity.DailyActivityEntity

@Database(
    entities = [
        // Core content
        Subject::class,
        Units::class,
        LessonEntity::class,
        SectionEntity::class,
        BlockEntity::class,
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
        UserProgressEntity::class,
        ConceptReview::class,
        QuizAttempt::class,
        QuestionResponse::class,
        SectionProgressEntity::class,
        DailyActivityEntity::class
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