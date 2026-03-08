package com.zeros.basheer.feature.analytics

import android.util.Log
import com.zeros.basheer.feature.analytics.domain.model.BasheerEvent
import com.zeros.basheer.feature.analytics.domain.model.FeedEndReason
import com.zeros.basheer.feature.analytics.domain.model.FeedInteraction
import com.zeros.basheer.feature.analytics.domain.model.LessonSource
import com.zeros.basheer.feature.analytics.domain.repository.AnalyticsRepository
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import com.zeros.basheer.feature.user.notifications.AchievementNotificationManager
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

    private var sessionStartMillis: Long = System.currentTimeMillis()
    private var sessionActivityCount: Int = 0

    // ─────────────────────────────────────────────────────────────────────────
    // Session lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param androidVersion  Build.VERSION.SDK_INT
     * @param deviceModel     Build.MODEL
     * @param networkType     "WIFI" | "MOBILE" | "NONE" — read from ConnectivityManager
     */
    fun onAppForegrounded(
        streakDays: Int,
        totalXp: Int,
        level: Int,
        studentPath: String,
        daysSinceLastOpen: Int,
        appVersion: String,
        androidVersion: Int,
        deviceModel: String,
        networkType: String?,
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
            androidVersion = androidVersion,
            deviceModel = deviceModel,
            networkType = networkType,
        ))
    }

    /**
     * Returns the session duration in seconds — useful for the caller
     * to decide whether to trigger an early sync.
     */
    fun onAppBackgrounded(): Int {
        val durationSeconds = ((System.currentTimeMillis() - sessionStartMillis) / 1000).toInt()
        track(BasheerEvent.AppSessionEnded(
            durationSeconds = durationSeconds,
            activitiesCount = sessionActivityCount,
        ))
        return durationSeconds
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

    /**
     * Call this from [LessonReaderViewModel] when the user taps the exit/back
     * button before reaching the final section. Do NOT call it when the lesson
     * is complete — [lessonCompleted] covers that path.
     */
    fun lessonAbandoned(
        lessonId: String,
        subjectId: String,
        unitId: String,
        abandonedAtPartIndex: Int,
        totalParts: Int,
        progressPercent: Int,
        timeSpentSeconds: Int,
        source: LessonSource = LessonSource.MAIN_SCREEN,
    ) = track(BasheerEvent.LessonAbandoned(
        lessonId = lessonId,
        subjectId = subjectId,
        unitId = unitId,
        abandonedAtPartIndex = abandonedAtPartIndex,
        totalParts = totalParts,
        progressPercent = progressPercent,
        timeSpentSeconds = timeSpentSeconds,
        source = source,
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

    /**
     * Call from [FeedsViewModel] when the user navigates away from the feed
     * screen or all cards are exhausted. Captures the full session picture.
     */
    fun feedSessionSummary(
        totalCards: Int,
        cardsReviewed: Int,
        cardsAnswered: Int,
        correctAnswers: Int,
        wrongAnswers: Int,
        skippedCards: Int,
        durationSeconds: Int,
        subjectIds: List<String>,
        endReason: FeedEndReason,
    ) = track(BasheerEvent.FeedSessionSummary(
        totalCards = totalCards,
        cardsReviewed = cardsReviewed,
        cardsAnswered = cardsAnswered,
        correctAnswers = correctAnswers,
        wrongAnswers = wrongAnswers,
        skippedCards = skippedCards,
        durationSeconds = durationSeconds,
        subjectIds = subjectIds,
        endReason = endReason,
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
    // Notifications
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Call from MainActivity.onCreate / onNewIntent when a notification intent
     * extra is detected. See NotificationEngaged for setup instructions.
     */
    fun notificationEngaged(
        notificationType: String,
        daysSinceLastOpen: Int,
        currentStreakDays: Int,
    ) = track(BasheerEvent.NotificationEngaged(
        notificationType = notificationType,
        daysSinceLastOpen = daysSinceLastOpen,
        currentStreakDays = currentStreakDays,
    ))

    // ─────────────────────────────────────────────────────────────────────────
    // Gamification
    // ─────────────────────────────────────────────────────────────────────────

    fun xpLevelUp(newLevel: Int, totalXp: Int, xpSource: String) {
        track(BasheerEvent.XpLevelUp(newLevel = newLevel, totalXp = totalXp, xpSource = xpSource))
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
        if (!preferencesRepository.getAnalyticsConsent().isEnabled) return
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