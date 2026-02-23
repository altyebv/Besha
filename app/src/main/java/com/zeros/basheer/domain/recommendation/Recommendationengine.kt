package com.zeros.basheer.domain.recommendation

import com.zeros.basheer.domain.repository.ContentRepository
import com.zeros.basheer.domain.model.*
import com.zeros.basheer.feature.quizbank.domain.repository.QuizBankRepository
import com.zeros.basheer.feature.subject.data.entity.StudentPath
import com.zeros.basheer.feature.subject.domain.model.Subject
import com.zeros.basheer.feature.user.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
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
 * Smart recommendation engine using multi-factor scoring.
 *
 * Recommendations are scoped to the student's academic path (SCIENCE, LITERARY, COMMON)
 * so students only see subjects relevant to them.
 */
@Singleton
class RecommendationEngine @Inject constructor(
    private val contentRepository: ContentRepository,
    private val quizBankRepository: QuizBankRepository,
    private val practiceRepository: com.zeros.basheer.feature.practice.domain.repository.PracticeRepository,
    private val userProfileRepository: UserProfileRepository,
    private val config: RecommendationConfig = RecommendationConfig()
) {

    /**
     * Generate top recommendations scoped to the student's academic path.
     */
    suspend fun getTopRecommendations(limit: Int = 3): List<ScoredRecommendation> {
        // Resolve path filter from profile — fall back to COMMON only if no profile yet
        val profile = userProfileRepository.getProfileOnce()
        val pathFilter = profile?.subjectsFilter ?: listOf(StudentPath.COMMON)

        // Only recommend subjects that belong to this student's path
        val subjects = contentRepository.getSubjectsByPathFilter(pathFilter).first()

        val allRecommendations = mutableListOf<ScoredRecommendation>()

        for (subject in subjects) {
            val context = buildSubjectContext(subject.id)
            val subjectRecs = generateSubjectRecommendations(subject, context)
                .take(config.maxRecommendationsPerSubject)
            allRecommendations.addAll(subjectRecs)
        }

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
        val lessons = contentRepository.getLessonsBySubject(subjectId).first()
        val completedLessons = contentRepository.getCompletedLessons().first()
        val completedCount = completedLessons.count { progress ->
            lessons.any { it.id == progress.lessonId }
        }

        val questionCounts = quizBankRepository.getQuestionCounts(subjectId)
        val avgScore = practiceRepository.getAverageScore(subjectId).first()
        val weakConcepts = getWeakConcepts(subjectId)

        val recentLessons = contentRepository.getRecentlyAccessedLessons(1).first()
        val lastStudied = recentLessons.firstOrNull()?.lastAccessedAt

        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }.timeInMillis

        val sessionsToday = contentRepository.getRecentlyAccessedLessons(50).first()
            .count { it.lastAccessedAt >= todayStart }

        return SubjectContext(
            subjectId = subjectId,
            subjectName = contentRepository.getSubjectNameAr(subjectId),
            lessonsCompleted = completedCount,
            totalLessons = lessons.size,
            percentComplete = if (lessons.isNotEmpty()) completedCount.toFloat() / lessons.size else 0f,
            averageSuccessRate = avgScore?.div(100f),
            weakConceptCount = weakConcepts.size,
            totalQuestionsAsked = questionCounts.total,
            lastStudiedTimestamp = lastStudied,
            studySessionsToday = sessionsToday,
            studySessionsThisWeek = 0,
            upcomingExamDays = null
        )
    }

    private suspend fun getWeakConcepts(subjectId: String): List<String> = emptyList()

    // ==================== RECOMMENDATION GENERATORS ====================

    private suspend fun generateContinueLessonRec(
        subject: Subject,
        context: SubjectContext
    ): ScoredRecommendation? {
        val recentLessons = contentRepository.getRecentlyAccessedLessons(5).first()
        val completedLessonIds = contentRepository.getCompletedLessons().first().map { it.lessonId }.toSet()

        // Only consider lessons that belong to this subject
        val subjectLessonIds = contentRepository.getLessonsBySubject(subject.id).first()
            .map { it.id }.toSet()

        val inProgressLesson = recentLessons
            .filter { subjectLessonIds.contains(it.lessonId) }
            .firstOrNull { !completedLessonIds.contains(it.lessonId) }
            ?: return null

        val lesson = contentRepository.getLessonById(inProgressLesson.lessonId) ?: return null
        val unit = contentRepository.getUnitById(lesson.unitId) ?: return null

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

        val recommendation = Recommendation.QuickReview(
            questionCount = 10,
            estimatedMinutes = 5
        )

        val score = scoreWeakConcept(context, 0.45f)

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
        val units = contentRepository.getUnitsBySubject(subject.id).first()

        for (unit in units) {
            val lessons = contentRepository.getLessonsByUnit(unit.id).first()
            val completedLessons = contentRepository.getCompletedLessons().first()
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

                return ScoredRecommendation(
                    subject = subject,
                    recommendation = recommendation,
                    score = scoreCompleteUnit(context, percentComplete),
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

        return ScoredRecommendation(
            subject = subject,
            recommendation = recommendation,
            score = scoreQuickReview(context),
            badge = RecommendationBadge.QUICK_WIN,
            reason = "مراجعة سريعة - 10 أسئلة"
        )
    }

    private suspend fun generateStartNewUnitRec(
        subject: Subject,
        context: SubjectContext
    ): ScoredRecommendation? {
        val units = contentRepository.getUnitsBySubject(subject.id).first()
        val completedLessons = contentRepository.getCompletedLessons().first().map { it.lessonId }.toSet()

        for (unit in units) {
            val lessons = contentRepository.getLessonsByUnit(unit.id).first()
            val hasIncomplete = lessons.any { !completedLessons.contains(it.id) }

            if (hasIncomplete) {
                val recommendation = Recommendation.StartNewUnit(
                    unitId = unit.id,
                    unitTitle = unit.title,
                    lessonCount = lessons.size
                )

                return ScoredRecommendation(
                    subject = subject,
                    recommendation = recommendation,
                    score = scoreStartNewUnit(context),
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
            context.studySessionsToday == 0 -> 90f
            progress > 0.7f -> 85f
            else -> 70f
        }
        val relevance = when {
            context.lastStudiedTimestamp != null &&
                    System.currentTimeMillis() - context.lastStudiedTimestamp < 24 * 60 * 60 * 1000 -> 90f
            else -> 60f
        }
        val impact = min(100f, progress * 100 + 30)
        val recency = if (context.lastStudiedTimestamp != null) 80f else 40f

        return urgency * config.urgencyWeight +
                relevance * config.relevanceWeight +
                impact * config.impactWeight +
                recency * config.recencyWeight
    }

    private fun scoreWeakConcept(context: SubjectContext, successRate: Float): Float {
        val urgency = when {
            successRate < config.criticalWeakThreshold -> 95f
            successRate < config.weakAreaThreshold -> 80f
            else -> 50f
        }
        val relevance = if (context.totalQuestionsAsked > 20) 85f else 60f
        val impact = 100f - (successRate * 100)
        val recency = 70f

        return urgency * config.urgencyWeight +
                relevance * config.relevanceWeight +
                impact * config.impactWeight +
                recency * config.recencyWeight
    }

    private fun scoreCompleteUnit(context: SubjectContext, percentComplete: Float): Float {
        val urgency = min(100f, percentComplete * 100 + 20)
        val relevance = 75f
        val impact = 85f
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