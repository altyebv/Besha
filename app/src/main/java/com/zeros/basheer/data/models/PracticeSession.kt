package com.zeros.basheer.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zeros.basheer.feature.subject.data.entity.SubjectEntity
import com.zeros.basheer.feature.subject.domain.model.Subject

/**
 * Tracks a practice session - a set of questions answered outside of a formal exam.
 *
 * Unlike QuizAttempt (which is tied to a specific Exam), PracticeSession is for:
 * - Practice by unit/concept
 * - Progress-based practice (questions from what student studied)
 * - Weak area focused practice
 * - Quick review sessions
 *
 * The actual questions and responses are stored in PracticeQuestion junction.
 */
@Entity(
    tableName = "practice_sessions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subjectId"),
        Index("generationType"),
        Index("startedAt")
    ]
)
data class PracticeSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: String,

    // How was this session generated?
    val generationType: PracticeGenerationType,

    // Filter criteria used to generate questions (stored as JSON for flexibility)
    val filterUnitIds: String? = null,        // JSON array of unit IDs
    val filterLessonIds: String? = null,      // JSON array of lesson IDs
    val filterConceptIds: String? = null,     // JSON array of concept IDs
    val filterQuestionTypes: String? = null,  // JSON array of QuestionType names
    val filterDifficulty: String? = null,     // JSON: {"min": 1, "max": 3}
    val filterSource: String? = null,         // QuestionSource name or null for all

    // Session configuration
    val questionCount: Int,
    val timeLimitSeconds: Int? = null,        // Optional time limit
    val shuffled: Boolean = true,             // Were questions shuffled?

    // Difficulty distribution (for balanced sessions)
    val difficultyDistribution: String? = null, // JSON: {"easy": 3, "medium": 5, "hard": 2}

    // Session state
    val status: PracticeSessionStatus = PracticeSessionStatus.IN_PROGRESS,
    val currentQuestionIndex: Int = 0,

    // Results
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val skippedCount: Int = 0,
    val score: Float? = null,                 // Percentage score when completed

    // Timing
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val totalTimeSeconds: Int? = null
)

/**
 * How was this practice session generated?
 */
enum class PracticeGenerationType {
    FULL_EXAM,           // Taking an actual exam (links to Exam via separate field if needed)
    BY_UNIT,             // Practice specific unit(s)
    BY_LESSON,           // Practice specific lesson(s)
    BY_CONCEPT,          // Practice specific concept(s)
    BY_PROGRESS,         // Based on what student has studied so far
    WEAK_AREAS,          // Focus on concepts with low success rate
    QUICK_REVIEW,        // Quick 10-question random session
    BY_TYPE,             // Practice specific question types
    BY_SOURCE,           // Practice from specific source (e.g., ministry exams only)
    CUSTOM               // Custom filter combination
}

/**
 * Status of a practice session.
 */
enum class PracticeSessionStatus {
    IN_PROGRESS,         // Student is currently taking it
    PAUSED,              // Student paused (can resume)
    COMPLETED,           // Finished normally
    ABANDONED            // Left without completing
}