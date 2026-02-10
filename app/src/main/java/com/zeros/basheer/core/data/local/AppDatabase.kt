package com.zeros.basheer.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zeros.basheer.feature.feed.data.dao.ContentVariantDao
import com.zeros.basheer.feature.lesson.data.dao.BlockDao
import com.zeros.basheer.feature.streak.data.dao.DailyActivityDao
import com.zeros.basheer.feature.feed.data.dao.FeedItemDao
import com.zeros.basheer.feature.lesson.data.dao.LessonDao
import com.zeros.basheer.feature.practice.data.dao.PracticeSessionDao
import com.zeros.basheer.feature.practice.data.dao.SectionConceptDao
import com.zeros.basheer.feature.lesson.data.dao.*
import com.zeros.basheer.feature.lesson.data.entity.*
import com.zeros.basheer.feature.progress.data.dao.*
import com.zeros.basheer.feature.progress.data.entity.*
import com.zeros.basheer.feature.streak.data.entity.*
import com.zeros.basheer.feature.subject.data.entity.*
import com.zeros.basheer.feature.concept.data.dao.*
import com.zeros.basheer.feature.concept.data.entity.*
import com.zeros.basheer.feature.feed.data.entity.*
import com.zeros.basheer.feature.practice.data.entity.*
import com.zeros.basheer.feature.quizbank.data.dao.*
import com.zeros.basheer.feature.quizbank.data.entity.*
import com.zeros.basheer.feature.subject.data.dao.SubjectDao
import com.zeros.basheer.feature.subject.data.dao.UnitDao

@Database(
    entities = [
        // Core content
        SubjectEntity::class,
        UnitEntity::class,
        LessonEntity::class,
        SectionEntity::class,
        BlockEntity::class,
        ContentVariantEntity::class,

        // Concepts & categorization
        ConceptEntity::class,
        TagEntity::class,
        ConceptTagEntity::class,
        SectionConcept::class,

        // Quiz system
        QuestionEntity::class,
        QuestionConceptEntity::class,
        ExamEntity::class,
        ExamQuestionEntity::class,
        QuestionStatsEntity::class,

        // Practice sessions
        PracticeSessionEntity::class,
        PracticeQuestionEntity::class,

        // Feed
        FeedItemEntity::class,

        // Progress tracking
        UserProgressEntity::class,
        ConceptReviewEntity::class,
        QuizAttemptEntity::class,
        QuestionResponseEntity::class,
        SectionProgressEntity::class,
        DailyActivityEntity::class
    ],
    version = 1,
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