package com.zeros.basheer.feature.analytics

import android.util.Log
import com.zeros.basheer.feature.analytics.domain.model.BasheerEvent
import com.zeros.basheer.feature.analytics.domain.model.FeedInteraction
import com.zeros.basheer.feature.analytics.domain.model.LessonSource
import com.zeros.basheer.feature.analytics.domain.repository.AnalyticsRepository
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import com.zeros.basheer.feature.user.notification.AchievementNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single analytics call-site for all app code.
 *
 * All calls are non-blocking — events are fire-and-forget into the Room queue.
 * The caller never waits for Firestore.
 *
 * Session lifecycle:
 *  - Call [onAppForegrounded] from your lifecycle observer
 *  - Call [onAppBackgrounded] when the app leaves foreground
 *
 * Adding a new event:
 *  1. Add the data class to [BasheerEvent]
 *  2. Add its JSON branch to [AnalyticsRepositoryImpl.toJson]
 *  3. Add a convenience function here
 *  Done — no other files need changes.
 */
@Singleton
class AnalyticsManager @Inject constructor(
    private val repository: AnalyticsRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val achievementNotificationManager: AchievementNotificationManager,

    ) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Track session start time for AppSessionEnded duration calculation
    private var sessionStartMillis: Long = System.currentTimeMillis()
    private var sessionActivityCount: Int = 0

    // ─────────────────────────────────────────────────────────────────────────
    // Session lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    fun onAppForegrounded(
        streakDays: Int,
        totalXp: Int,
        level: Int,
        studentPath: String,
        daysSinceLastOpen: Int,
        appVersion: String,
    ) {
        sessionStartMillis = System.currentTimeMillis()
        sessionActivityCount = 0
        track(BasheerEvent.AppSessionStarted(
            streakDays = streakDays,
            totalXp = totalXp,
            level = level,
            studentPath = studentPath,
            daysSinceLastOpen = daysSinceLastOpen,
            appVersion = appVersion,
        ))
    }

    fun onAppBackgrounded() {
        val durationSeconds = ((System.currentTimeMillis() - sessionStartMillis) / 1000).toInt()
        track(BasheerEvent.AppSessionEnded(
            durationSeconds = durationSeconds,
            activitiesCount = sessionActivityCount,
        ))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Onboarding
    // ─────────────────────────────────────────────────────────────────────────

    fun onboardingCompleted(
        studentPath: String,
        hasSchoolName: Boolean,
        hasEmail: Boolean,
        state: String?,
        major: String?,
        dailyStudyMinutes: Int,
        reminderEnabled: Boolean,
        consentTier: String,
        durationSeconds: Int,
    ) = track(BasheerEvent.OnboardingCompleted(
        studentPath = studentPath,
        hasSchoolName = hasSchoolName,
        hasEmail = hasEmail,
        state = state,
        major = major,
        dailyStudyMinutes = dailyStudyMinutes,
        reminderEnabled = reminderEnabled,
        consentTier = consentTier,
        durationSeconds = durationSeconds,
    ))

    // ─────────────────────────────────────────────────────────────────────────
    // Lessons
    // ─────────────────────────────────────────────────────────────────────────

    fun lessonViewed(
        lessonId: String,
        subjectId: String,
        unitId: String,
        source: LessonSource = LessonSource.MAIN_SCREEN,
        wasCompleted: Boolean = false,
    ) = track(BasheerEvent.LessonViewed(
        lessonId = lessonId,
        subjectId = subjectId,
        unitId = unitId,
        source = source,
        wasCompleted = wasCompleted,
    ))

    fun lessonCompleted(
        lessonId: String,
        subjectId: String,
        unitId: String,
        timeSpentSeconds: Int,
        isFirstCompletion: Boolean,
        sectionsCount: Int,
    ) = track(BasheerEvent.LessonCompleted(
        lessonId = lessonId,
        subjectId = subjectId,
        unitId = unitId,
        timeSpentSeconds = timeSpentSeconds,
        isFirstCompletion = isFirstCompletion,
        sectionsCount = sectionsCount,
    ))

    // ─────────────────────────────────────────────────────────────────────────
    // Practice
    // ─────────────────────────────────────────────────────────────────────────

    fun practiceSessionStarted(
        sessionId: Long,
        subjectId: String,
        generationType: String,
        questionCount: Int,
        hasTimeLimit: Boolean,
    ) = track(BasheerEvent.PracticeSessionStarted(
        sessionId = sessionId,
        subjectId = subjectId,
        generationType = generationType,
        questionCount = questionCount,
        hasTimeLimit = hasTimeLimit,
    ))

    fun practiceSessionCompleted(
        sessionId: Long,
        subjectId: String,
        generationType: String,
        questionCount: Int,
        correctCount: Int,
        wrongCount: Int,
        skippedCount: Int,
        score: Float,
        durationSeconds: Int,
        wasAbandoned: Boolean = false,
    ) = track(BasheerEvent.PracticeSessionCompleted(
        sessionId = sessionId,
        subjectId = subjectId,
        generationType = generationType,
        questionCount = questionCount,
        correctCount = correctCount,
        wrongCount = wrongCount,
        skippedCount = skippedCount,
        score = score,
        durationSeconds = durationSeconds,
        wasAbandoned = wasAbandoned,
    ))

    fun questionAnswered(
        questionId: String,
        subjectId: String,
        questionType: String,
        isCorrect: Boolean,
        timeSpentSeconds: Int,
        sessionId: Long,
        attemptNumber: Int = 1,
    ) = track(BasheerEvent.QuestionAnswered(
        questionId = questionId,
        subjectId = subjectId,
        questionType = questionType,
        isCorrect = isCorrect,
        timeSpentSeconds = timeSpentSeconds,
        sessionId = sessionId,
        attemptNumber = attemptNumber,
    ))

    // ─────────────────────────────────────────────────────────────────────────
    // Feed
    // ─────────────────────────────────────────────────────────────────────────

    fun feedCardInteracted(
        cardId: String,
        subjectId: String,
        cardType: String,
        interaction: FeedInteraction,
        wasCorrect: Boolean? = null,
    ) = track(BasheerEvent.FeedCardInteracted(
        cardId = cardId,
        subjectId = subjectId,
        cardType = cardType,
        interaction = interaction,
        wasCorrect = wasCorrect,
    ))

    // ─────────────────────────────────────────────────────────────────────────
    // Exams
    // ─────────────────────────────────────────────────────────────────────────

    fun examCompleted(
        examId: String,
        subjectId: String,
        examType: String,
        totalQuestions: Int,
        score: Float,
        durationSeconds: Int,
    ) = track(BasheerEvent.ExamCompleted(
        examId = examId,
        subjectId = subjectId,
        examType = examType,
        totalQuestions = totalQuestions,
        score = score,
        durationSeconds = durationSeconds,
    ))

    // ─────────────────────────────────────────────────────────────────────────
    // Gamification
    // ─────────────────────────────────────────────────────────────────────────

    fun xpLevelUp(newLevel: Int, totalXp: Int, xpSource: String) {
        track(BasheerEvent.XpLevelUp(newLevel = newLevel, totalXp = totalXp, xpSource = xpSource))
        // Notify immediately — the user is in-app when this fires so it should celebrate
        // as an in-moment reward, not as a background notification.
        achievementNotificationManager.showLevelUp(newLevel, totalXp)
    }

    fun streakMilestone(streakDays: Int, streakLevel: String) {
        if (streakDays in STREAK_MILESTONES) {
            track(BasheerEvent.StreakMilestone(streakDays = streakDays, streakLevel = streakLevel))
            achievementNotificationManager.showStreakMilestone(streakDays, streakLevel)
        }
    }

    fun dailyGoalReached(streakDays: Int, streakLevel: String, minutesSpent: Int) =
        track(BasheerEvent.DailyGoalReached(
            streakDays = streakDays,
            streakLevel = streakLevel,
            minutesSpent = minutesSpent,
        ))

    // ─────────────────────────────────────────────────────────────────────────
    // Core dispatch
    // ─────────────────────────────────────────────────────────────────────────

    private fun track(event: BasheerEvent) {
        if (!preferencesRepository.getAnalyticsConsent().isEnabled) return  // ← consent gate
        sessionActivityCount++
        scope.launch {
            runCatching { repository.enqueue(event) }
                .onFailure { Log.w(TAG, "Failed to enqueue ${event::class.simpleName}", it) }
        }
    }

    companion object {
        private const val TAG = "AnalyticsManager"
        private val STREAK_MILESTONES = setOf(3, 7, 14, 30, 60, 100)
    }
}