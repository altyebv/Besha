package com.zeros.basheer.domain.recommendation

import com.zeros.basheer.data.models.*
import com.zeros.basheer.data.repository.LessonRepository
import com.zeros.basheer.data.repository.QuizBankRepository
import com.zeros.basheer.domain.model.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * Configurable weights and thresholds for recommendation scoring
 */
data class RecommendationConfig(
    // Scoring weights (must sum to 1.0)
    val urgencyWeight: Float = 0.4f,
    val relevanceWeight: Float = 0.3f,
    val impactWeight: Float = 0.2f,
    val recencyWeight: Float = 0.1f,

    // Thresholds
    val weakAreaThreshold: Float = 0.6f,      // < 60% = weak
    val criticalWeakThreshold: Float = 0.4f,  // < 40% = critical
    val streakRiskHours: Int = 20,            // Hours before streak at risk
    val unitNearCompletePercent: Float = 0.8f, // 80%+ = almost done
    val minQuestionsForStats: Int = 5,        // Minimum questions to consider stats

    // Behavior
    val maxRecommendationsPerSubject: Int = 2,
    val maxTotalRecommendations: Int = 3,
    val enableTimeBasedFiltering: Boolean = true
)

/**
 * Smart recommendation engine using multi-factor scoring
 */
@Singleton
class RecommendationEngine @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val quizBankRepository: QuizBankRepository,
    private val config: RecommendationConfig = RecommendationConfig()
) {

    /**
     * Generate top recommendations across all subjects
     */
    suspend fun getTopRecommendations(limit: Int = 3): List<ScoredRecommendation> {
        val subjects = lessonRepository.getAllSubjects().first()
        val allRecommendations = mutableListOf<ScoredRecommendation>()

        // Get current time context
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val isMorning = currentHour in 6..11

        // Generate recommendations for each subject
        for (subject in subjects) {
            val context = buildSubjectContext(subject.id)
            val subjectRecs = generateSubjectRecommendations(subject, context)
                .take(config.maxRecommendationsPerSubject)

            allRecommendations.addAll(subjectRecs)
        }

        // Sort by score and return top N
        return allRecommendations
            .sortedByDescending { it.score }
            .take(min(limit, config.maxTotalRecommendations))
    }

    /**
     * Generate recommendations for a specific subject
     */
    private suspend fun generateSubjectRecommendations(
        subject: Subject,
        context: SubjectContext
    ): List<ScoredRecommendation> {
        val candidates = mutableListOf<ScoredRecommendation>()

        // 1. Continue in-progress lesson
        val continueLesson = generateContinueLessonRec(subject, context)
        if (continueLesson != null) candidates.add(continueLesson)

        // 2. Review weak concepts
        val weakConceptRec = generateWeakConceptRec(subject, context)
        if (weakConceptRec != null) candidates.add(weakConceptRec)

        // 3. Complete almost-done unit
        val completeUnitRec = generateCompleteUnitRec(subject, context)
        if (completeUnitRec != null) candidates.add(completeUnitRec)

        // 4. Quick review
        if (context.totalQuestionsAsked >= config.minQuestionsForStats) {
            val quickReviewRec = generateQuickReviewRec(subject, context)
            if (quickReviewRec != null) candidates.add(quickReviewRec)
        }

        // 5. Start new unit (if nothing else)
        if (candidates.isEmpty() && context.percentComplete < 1.0f) {
            val newUnitRec = generateStartNewUnitRec(subject, context)
            if (newUnitRec != null) candidates.add(newUnitRec)
        }

        return candidates.sortedByDescending { it.score }
    }

    /**
     * Build subject context from data
     */
    private suspend fun buildSubjectContext(subjectId: String): SubjectContext {
        val lessons = lessonRepository.getLessonsBySubject(subjectId).first()
        val completedLessons = lessonRepository.getCompletedLessons().first()
        val completedCount = completedLessons.count { progress ->
            lessons.any { it.id == progress.lessonId }
        }

        // Get quiz performance
        val questionCounts = quizBankRepository.getQuestionCounts(subjectId)
        val avgScore = quizBankRepository.getAverageScoreForSubject(subjectId).first()

        // Get weak concepts
        val weakConcepts = getWeakConcepts(subjectId)

        // Get recency data
        val recentLessons = lessonRepository.getRecentlyAccessedLessons(1).first()
        val lastStudied = recentLessons.firstOrNull()?.lastAccessedAt

        // Count today's sessions
        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }.timeInMillis

        val sessionsToday = lessonRepository.getRecentlyAccessedLessons(50).first()
            .count { it.lastAccessedAt >= todayStart }

        return SubjectContext(
            subjectId = subjectId,
            subjectName = lessonRepository.getSubjectById(subjectId)?.nameAr ?: "",
            lessonsCompleted = completedCount,
            totalLessons = lessons.size,
            percentComplete = if (lessons.isNotEmpty()) completedCount.toFloat() / lessons.size else 0f,
            averageSuccessRate = avgScore?.div(100f),
            weakConceptCount = weakConcepts.size,
            totalQuestionsAsked = questionCounts.total,
            lastStudiedTimestamp = lastStudied,
            studySessionsToday = sessionsToday,
            studySessionsThisWeek = 0, // TODO: implement week counting
            upcomingExamDays = null // TODO: implement exam tracking
        )
    }

    /**
     * Get concepts with low success rates
     */
    private suspend fun getWeakConcepts(subjectId: String): List<String> {
        // This would query QuestionStats to find concepts with < 60% success
        // For now, return empty - will implement when concept stats are available
        return emptyList()
    }

    // ==================== RECOMMENDATION GENERATORS ====================

    private suspend fun generateContinueLessonRec(
        subject: Subject,
        context: SubjectContext
    ): ScoredRecommendation? {
        // Find most recently accessed incomplete lesson
        val recentLessons = lessonRepository.getRecentlyAccessedLessons(5).first()
        val completedLessonIds = lessonRepository.getCompletedLessons().first().map { it.lessonId }.toSet()

        val inProgressLesson = recentLessons
            .firstOrNull { !completedLessonIds.contains(it.lessonId) }
            ?: return null

        val lesson = lessonRepository.getLessonById(inProgressLesson.lessonId) ?: return null
        val unit = lessonRepository.getUnitById(lesson.unitId) ?: return null

        val recommendation = Recommendation.ContinueLesson(
            lessonId = lesson.id,
            lessonTitle = lesson.title,
            unitTitle = unit.title,
            progress = 0.5f,
            estimatedMinutes = lesson.estimatedMinutes
        )

        val score = scoreContinueLesson(context, inProgressLesson.progress)
        val badge = when {
            inProgressLesson.progress > 0.5f -> RecommendationBadge.ALMOST_DONE
            context.studySessionsToday == 0 -> RecommendationBadge.HOT_STREAK
            else -> RecommendationBadge.QUICK_WIN
        }

        return ScoredRecommendation(
            subject = subject,
            recommendation = recommendation,
            score = score,
            badge = badge,
            reason = when {
                inProgressLesson.progress > 0.7f -> "أكمل ${((1 - inProgressLesson.progress) * 100).toInt()}% المتبقية"
                else -> "واصل من حيث توقفت"
            }
        )
    }

    private suspend fun generateWeakConceptRec(
        subject: Subject,
        context: SubjectContext
    ): ScoredRecommendation? {
        if (context.weakConceptCount == 0 || context.totalQuestionsAsked < config.minQuestionsForStats) {
            return null
        }

        // TODO: Get actual weak concept details when stats are available
        val recommendation = Recommendation.QuickReview(
            questionCount = 10,
            estimatedMinutes = 5
        )

        val score = scoreWeakConcept(context, 0.45f) // Placeholder success rate

        return ScoredRecommendation(
            subject = subject,
            recommendation = recommendation,
            score = score,
            badge = RecommendationBadge.WEAK_AREA,
            reason = "راجع ${context.weakConceptCount} مفهوم ضعيف"
        )
    }

    private suspend fun generateCompleteUnitRec(
        subject: Subject,
        context: SubjectContext
    ): ScoredRecommendation? {
        // Find units that are 80%+ complete
        val units = lessonRepository.getUnitsBySubject(subject.id).first()

        for (unit in units) {
            val lessons = lessonRepository.getLessonsByUnit(unit.id).first()
            val completedLessons = lessonRepository.getCompletedLessons().first()
            val completedCount = completedLessons.count { progress ->
                lessons.any { it.id == progress.lessonId }
            }

            val percentComplete = if (lessons.isNotEmpty()) {
                completedCount.toFloat() / lessons.size
            } else 0f

            if (percentComplete >= config.unitNearCompletePercent && percentComplete < 1.0f) {
                val recommendation = Recommendation.CompleteUnit(
                    unitId = unit.id,
                    unitTitle = unit.title,
                    lessonsCompleted = completedCount,
                    totalLessons = lessons.size,
                    percentComplete = percentComplete
                )

                val score = scoreCompleteUnit(context, percentComplete)

                return ScoredRecommendation(
                    subject = subject,
                    recommendation = recommendation,
                    score = score,
                    badge = RecommendationBadge.ALMOST_DONE,
                    reason = "باقي ${lessons.size - completedCount} دروس فقط!"
                )
            }
        }

        return null
    }

    private suspend fun generateQuickReviewRec(
        subject: Subject,
        context: SubjectContext
    ): ScoredRecommendation? {
        val recommendation = Recommendation.QuickReview(
            questionCount = 10,
            estimatedMinutes = 5
        )

        val score = scoreQuickReview(context)

        return ScoredRecommendation(
            subject = subject,
            recommendation = recommendation,
            score = score,
            badge = RecommendationBadge.QUICK_WIN,
            reason = "مراجعة سريعة - 10 أسئلة"
        )
    }

    private suspend fun generateStartNewUnitRec(
        subject: Subject,
        context: SubjectContext
    ): ScoredRecommendation? {
        // Find first incomplete unit
        val units = lessonRepository.getUnitsBySubject(subject.id).first()
        val completedLessons = lessonRepository.getCompletedLessons().first().map { it.lessonId }.toSet()

        for (unit in units) {
            val lessons = lessonRepository.getLessonsByUnit(unit.id).first()
            val hasIncomplete = lessons.any { !completedLessons.contains(it.id) }

            if (hasIncomplete) {
                val recommendation = Recommendation.StartNewUnit(
                    unitId = unit.id,
                    unitTitle = unit.title,
                    lessonCount = lessons.size
                )

                val score = scoreStartNewUnit(context)

                return ScoredRecommendation(
                    subject = subject,
                    recommendation = recommendation,
                    score = score,
                    badge = RecommendationBadge.NEW_CONTENT,
                    reason = "ابدأ وحدة جديدة"
                )
            }
        }

        return null
    }

    // ==================== SCORING FUNCTIONS ====================

    private fun scoreContinueLesson(context: SubjectContext, progress: Float): Float {
        val urgency = when {
            context.studySessionsToday == 0 -> 90f // Not studied today
            progress > 0.7f -> 85f // Almost done
            else -> 70f
        }

        val relevance = when {
            context.lastStudiedTimestamp != null &&
                    System.currentTimeMillis() - context.lastStudiedTimestamp < 24 * 60 * 60 * 1000 -> 90f
            else -> 60f
        }

        val impact = min(100f, progress * 100 + 30) // Higher progress = higher impact
        val recency = if (context.lastStudiedTimestamp != null) 80f else 40f

        return urgency * config.urgencyWeight +
                relevance * config.relevanceWeight +
                impact * config.impactWeight +
                recency * config.recencyWeight
    }

    private fun scoreWeakConcept(context: SubjectContext, successRate: Float): Float {
        val urgency = when {
            successRate < config.criticalWeakThreshold -> 95f // Critical
            successRate < config.weakAreaThreshold -> 80f // Weak
            else -> 50f
        }

        val relevance = if (context.totalQuestionsAsked > 20) 85f else 60f
        val impact = 100f - (successRate * 100) // Lower success = higher impact
        val recency = 70f

        return urgency * config.urgencyWeight +
                relevance * config.relevanceWeight +
                impact * config.impactWeight +
                recency * config.recencyWeight
    }

    private fun scoreCompleteUnit(context: SubjectContext, percentComplete: Float): Float {
        val urgency = min(100f, percentComplete * 100 + 20) // Higher % = more urgent
        val relevance = 75f
        val impact = 85f // Completing a unit is high impact
        val recency = 70f

        return urgency * config.urgencyWeight +
                relevance * config.relevanceWeight +
                impact * config.impactWeight +
                recency * config.recencyWeight
    }

    private fun scoreQuickReview(context: SubjectContext): Float {
        val urgency = if (context.studySessionsToday == 0) 65f else 40f
        val relevance = if (context.totalQuestionsAsked > 10) 70f else 40f
        val impact = 55f
        val recency = 60f

        return urgency * config.urgencyWeight +
                relevance * config.relevanceWeight +
                impact * config.impactWeight +
                recency * config.recencyWeight
    }

    private fun scoreStartNewUnit(context: SubjectContext): Float {
        val urgency = if (context.percentComplete > 0.5f) 40f else 60f
        val relevance = 50f
        val impact = 60f
        val recency = 50f

        return urgency * config.urgencyWeight +
                relevance * config.relevanceWeight +
                impact * config.impactWeight +
                recency * config.recencyWeight
    }
}