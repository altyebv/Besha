package com.zeros.basheer.feature.analytics.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.zeros.basheer.feature.analytics.data.dao.AnalyticsEventDao
import com.zeros.basheer.feature.analytics.data.entity.AnalyticsEventEntity
import com.zeros.basheer.feature.analytics.domain.model.AnalyticsConsent
import com.zeros.basheer.feature.analytics.domain.model.BasheerEvent
import com.zeros.basheer.feature.analytics.domain.model.LearningSignal
import com.zeros.basheer.feature.analytics.domain.repository.AnalyticsRepository
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import com.zeros.basheer.feature.user.domain.repository.UserProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: AnalyticsEventDao,
    private val firestore: FirebaseFirestore,
    private val preferencesRepository: UserPreferencesRepository,
    private val userProfileRepository: UserProfileRepository,
) : AnalyticsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("basheer_analytics", Context.MODE_PRIVATE)

    // ─────────────────────────────────────────────────────────────────────────
    // Install identity
    // ─────────────────────────────────────────────────────────────────────────

    override val installId: String
        get() = prefs.getString(KEY_INSTALL_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_INSTALL_ID, it).apply()
        }

    // ─────────────────────────────────────────────────────────────────────────
    // Session tracking
    // ─────────────────────────────────────────────────────────────────────────

    private var _currentSessionId: String = buildSessionId()

    val currentSessionId: String get() = _currentSessionId

    fun rotateSession() {
        _currentSessionId = buildSessionId()
    }

    private fun buildSessionId(): String = "${installId}_${System.currentTimeMillis()}"

    // ─────────────────────────────────────────────────────────────────────────
    // Enqueue
    // ─────────────────────────────────────────────────────────────────────────

    override suspend fun enqueue(event: BasheerEvent) {
        val entity = AnalyticsEventEntity(
            eventType = event::class.simpleName ?: "Unknown",
            payload = event.toJson(),
            dateBucket = todayBucket(),
            sessionId = _currentSessionId,
        )
        dao.insert(entity)
    }

    override suspend fun enqueueSignal(signal: LearningSignal) {
        val entity = AnalyticsEventEntity(
            eventType = signal::class.simpleName ?: "Unknown",
            payload = signal.toJson(),
            dateBucket = todayBucket(),
            sessionId = _currentSessionId,
        )
        dao.insert(entity)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Upload
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Firestore structure:
     *
     *   analytics/{installId}/days/{YYYY-MM-DD}
     *   {
     *     installId: "...",
     *     date: "2025-01-15",
     *     appVersion: "1.0.0",
     *     uploadedAt: <server timestamp>,
     *     events: {
     *       "42": { _type: "LessonCompleted", _occurredAt: 1234567890, ... },
     *       "43": { _type: "QuestionAnswered", ... },
     *     }
     *   }
     *
     * IMPORTANT: events is a MAP keyed by Room row ID, NOT an array.
     * Firestore merges maps field-by-field, so a second upload on the same day
     * adds new keys without overwriting earlier entries.
     * (Arrays are atomic — a second set() call would replace, not append.)
     *
     * High-value events (LessonCompleted, LessonAbandoned, PracticeSessionCompleted,
     * ExamCompleted) are ALSO written to flat cross-user queryable collections.
     * This lets you query "give me all LessonCompleted for subjectId=PHYSICS_U3"
     * without reading every user's subcollection.
     *
     * Lesson/daily aggregates use FieldValue.increment() for atomic counters —
     * no read needed, safe for concurrent uploads from different devices.
     *
     * Free-tier write budget per day at 1,000 DAU:
     *   - analytics/days documents:       ~1,000  (daily per-user batch)
     *   - flat event documents:           ~3,000  (avg 3 high-value events/user)
     *   - lesson aggregate writes:        ~2,000  (view + complete per lesson touched)
     *   - daily aggregate write:          ~1,000  (1 per user per sync)
     *   Total:                            ~7,000  (free tier = 20,000/day ✓)
     */
    override suspend fun uploadPendingBatches(): Result<Int> = runCatching {
        val consent = preferencesRepository.getAnalyticsConsent()
        if (!consent.isEnabled) return@runCatching 0

        val buckets = dao.getUnsyncedDateBuckets()

        var totalUploaded = 0

        val profileSnapshot: Map<String, Any?>? = if (consent == AnalyticsConsent.FULL) {
            userProfileRepository.getProfileOnce()?.let { p ->
                mapOf(
                    "studentPath" to p.studentPath.name,
                    "state"       to p.state,
                    "city"        to p.city,
                    "schoolName"  to p.schoolName,
                    "major"       to p.major,
                    "dailyStudyMinutes" to p.dailyStudyMinutes,
                )
            }
        } else null

        for (bucket in buckets) {
            val rows = dao.getUnsyncedForDate(bucket)
            if (rows.isEmpty()) continue

            // ── 1. Per-user day document (events as map, not array) ───────────
            //
            // Key: row.id.toString() — stable Room primary key.
            // Using merge() means a re-upload of the same day appends new event
            // keys without touching ones already written.
            val eventsMap: Map<String, Any> = rows.associate { row ->
                row.id.toString() to buildEventMap(row)
            }

            val docData = hashMapOf<String, Any?>(
                "installId"   to installId,
                "date"        to bucket,
                "consentTier" to consent.name,
                "appVersion"  to appVersion(),
                "uploadedAt" to FieldValue.serverTimestamp(),
                "events"      to eventsMap,
            )
            if (profileSnapshot != null) {
                docData["userProfile"] = profileSnapshot
            }

            firestore
                .collection(COLLECTION_ANALYTICS)
                .document(installId)
                .collection("days")
                .document(bucket)
                .set(docData, SetOptions.merge())
                .await()

            // ── 2. Flat cross-user event documents (high-value events only) ───
            //
            // Written individually so they can be queried by subjectId, lessonId,
            // date, studentPath, etc. across all users without subcollection scans.
            for (row in rows) {
                val eventJson = JSONObject(row.payload)
                when (row.eventType) {
                    "LessonCompleted", "LessonAbandoned",
                    "PracticeSessionCompleted", "ExamCompleted" -> {
                        val flatDoc = buildFlatEventDoc(row, eventJson, consent, profileSnapshot)
                        firestore
                            .collection(COLLECTION_EVENTS_FLAT)
                            .document("${installId}_${row.id}")
                            .set(flatDoc)
                            .await()
                    }
                }
            }

            // ── 3. Lesson-level aggregates ────────────────────────────────────
            //
            // Atomic server-side counters using FieldValue.increment().
            // No read needed. Safe for concurrent uploads.
            for (row in rows) {
                val eventJson = JSONObject(row.payload)
                when (row.eventType) {
                    "LessonViewed" -> {
                        val lessonId = eventJson.optString("lessonId")
                        if (lessonId.isNotBlank()) {
                            firestore.collection(COLLECTION_AGGREGATES_LESSONS)
                                .document(lessonId)
                                .set(
                                    mapOf("viewCount" to FieldValue.increment(1)),
                                    SetOptions.merge(),
                                ).await()
                        }
                    }
                    "LessonCompleted" -> {
                        val lessonId = eventJson.optString("lessonId")
                        if (lessonId.isNotBlank()) {
                            firestore.collection(COLLECTION_AGGREGATES_LESSONS)
                                .document(lessonId)
                                .set(
                                    mapOf("completeCount" to FieldValue.increment(1)),
                                    SetOptions.merge(),
                                ).await()
                        }
                    }
                    "LessonAbandoned" -> {
                        val lessonId = eventJson.optString("lessonId")
                        if (lessonId.isNotBlank()) {
                            firestore.collection(COLLECTION_AGGREGATES_LESSONS)
                                .document(lessonId)
                                .set(
                                    mapOf("abandonCount" to FieldValue.increment(1)),
                                    SetOptions.merge(),
                                ).await()
                        }
                    }
                }
            }

            // ── 4. Daily aggregate ────────────────────────────────────────────
            //
            // One write per sync per user per day. Gives you DAU-ish counts
            // and session totals without touching individual event records.
            val sessionCount = rows.count { it.eventType == "AppSessionStarted" }
            if (sessionCount > 0) {
                firestore.collection(COLLECTION_AGGREGATES_DAILY)
                    .document(bucket)
                    .set(
                        mapOf("sessionCount" to FieldValue.increment(sessionCount.toLong())),
                        SetOptions.merge(),
                    ).await()
            }

            dao.markSynced(rows.map { it.id })
            totalUploaded += rows.size
        }

        totalUploaded
    }

    override suspend fun pruneOldSyncedEvents(daysToKeep: Int) {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysToKeep.toLong())
        dao.deleteSyncedBefore(cutoff)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Builds the map representation of a single Room row for the events map. */
    private fun buildEventMap(row: AnalyticsEventEntity): Map<String, Any> {
        val base = JSONObject(row.payload)
        base.put("_type", row.eventType)
        base.put("_occurredAt", row.occurredAt)
        base.put("_sessionId", row.sessionId)
        return base.toMap()
    }

    /**
     * Builds a flat document for the cross-user queryable events collection.
     * Only called for high-value session-level events.
     */
    private fun buildFlatEventDoc(
        row: AnalyticsEventEntity,
        eventJson: JSONObject,
        consent: AnalyticsConsent,
        profileSnapshot: Map<String, Any?>?,
    ): Map<String, Any?> {
        val doc = mutableMapOf<String, Any?>(
            "installId"   to installId,
            "eventType"   to row.eventType,
            "occurredAt"  to row.occurredAt,
            "date"        to row.dateBucket,
            "sessionId"   to row.sessionId,
            "consentTier" to consent.name,
        )
        // Flatten key fields to top level for Firestore queries
        listOf("lessonId", "subjectId", "unitId", "examId", "examType",
            "sessionId", "score", "durationSeconds", "wasAbandoned",
            "abandonedAtPartIndex", "progressPercent", "isFirstCompletion"
        ).forEach { key ->
            if (eventJson.has(key)) doc[key] = eventJson.get(key)
        }
        // Attach path for cohort splits — only if consent allows
        if (consent.includesProfile && profileSnapshot != null) {
            doc["studentPath"] = profileSnapshot["studentPath"]
            doc["state"] = profileSnapshot["state"]
        }
        return doc
    }

    private fun todayBucket(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun appVersion(): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    }.getOrDefault("unknown")

    companion object {
        private const val KEY_INSTALL_ID = "install_id"
        private const val COLLECTION_ANALYTICS          = "analytics"
        private const val COLLECTION_EVENTS_FLAT        = "events_flat"
        private const val COLLECTION_AGGREGATES_LESSONS = "aggregates_lessons"
        private const val COLLECTION_AGGREGATES_DAILY   = "aggregates_daily"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BasheerEvent → JSON serialization
// ─────────────────────────────────────────────────────────────────────────────

private fun BasheerEvent.toJson(): String = when (this) {
    is BasheerEvent.AppSessionStarted -> JSONObject().apply {
        put("streakDays", streakDays)
        put("totalXp", totalXp)
        put("level", level)
        put("studentPath", studentPath)
        put("daysSinceLastOpen", daysSinceLastOpen)
        put("appVersion", appVersion)
        put("androidVersion", androidVersion)
        put("deviceModel", deviceModel)
        networkType?.let { put("networkType", it) }
    }
    is BasheerEvent.AppSessionEnded -> JSONObject().apply {
        put("durationSeconds", durationSeconds)
        put("activitiesCount", activitiesCount)
    }
    is BasheerEvent.DailyGoalReached -> JSONObject().apply {
        put("streakDays", streakDays)
        put("streakLevel", streakLevel)
        put("minutesSpent", minutesSpent)
    }
    is BasheerEvent.OnboardingCompleted -> JSONObject().apply {
        put("studentPath", studentPath)
        put("hasSchoolName", hasSchoolName)
        put("hasEmail", hasEmail)
        state?.let { put("state", it) }
        major?.let { put("major", it) }
        put("dailyStudyMinutes", dailyStudyMinutes)
        put("reminderEnabled", reminderEnabled)
        put("consentTier", consentTier)
        put("durationSeconds", durationSeconds)
    }
    is BasheerEvent.LessonViewed -> JSONObject().apply {
        put("lessonId", lessonId)
        put("subjectId", subjectId)
        put("unitId", unitId)
        put("source", source.name)
        put("wasCompleted", wasCompleted)
    }
    is BasheerEvent.LessonCompleted -> JSONObject().apply {
        put("lessonId", lessonId)
        put("subjectId", subjectId)
        put("unitId", unitId)
        put("timeSpentSeconds", timeSpentSeconds)
        put("isFirstCompletion", isFirstCompletion)
        put("sectionsCount", sectionsCount)
    }
    is BasheerEvent.LessonAbandoned -> JSONObject().apply {
        put("lessonId", lessonId)
        put("subjectId", subjectId)
        put("unitId", unitId)
        put("abandonedAtPartIndex", abandonedAtPartIndex)
        put("totalParts", totalParts)
        put("progressPercent", progressPercent)
        put("timeSpentSeconds", timeSpentSeconds)
        put("source", source.name)
    }
    is BasheerEvent.PracticeSessionStarted -> JSONObject().apply {
        put("sessionId", sessionId)
        put("subjectId", subjectId)
        put("generationType", generationType)
        put("questionCount", questionCount)
        put("hasTimeLimit", hasTimeLimit)
    }
    is BasheerEvent.PracticeSessionCompleted -> JSONObject().apply {
        put("sessionId", sessionId)
        put("subjectId", subjectId)
        put("generationType", generationType)
        put("questionCount", questionCount)
        put("correctCount", correctCount)
        put("wrongCount", wrongCount)
        put("skippedCount", skippedCount)
        put("score", score)
        put("durationSeconds", durationSeconds)
        put("wasAbandoned", wasAbandoned)
    }
    is BasheerEvent.QuestionAnswered -> JSONObject().apply {
        put("questionId", questionId)
        put("subjectId", subjectId)
        put("questionType", questionType)
        put("isCorrect", isCorrect)
        put("timeSpentSeconds", timeSpentSeconds)
        put("sessionId", sessionId)
        put("attemptNumber", attemptNumber)
    }
    is BasheerEvent.FeedCardInteracted -> JSONObject().apply {
        put("cardId", cardId)
        put("subjectId", subjectId)
        put("cardType", cardType)
        put("interaction", interaction.name)
        wasCorrect?.let { put("wasCorrect", it) }
    }
    is BasheerEvent.FeedSessionSummary -> JSONObject().apply {
        put("totalCards", totalCards)
        put("cardsReviewed", cardsReviewed)
        put("cardsAnswered", cardsAnswered)
        put("correctAnswers", correctAnswers)
        put("wrongAnswers", wrongAnswers)
        put("skippedCards", skippedCards)
        put("durationSeconds", durationSeconds)
        put("subjectIds", JSONArray(subjectIds))
        put("endReason", endReason.name)
    }
    is BasheerEvent.ExamCompleted -> JSONObject().apply {
        put("examId", examId)
        put("subjectId", subjectId)
        put("examType", examType)
        put("totalQuestions", totalQuestions)
        put("score", score)
        put("durationSeconds", durationSeconds)
    }
    is BasheerEvent.NotificationEngaged -> JSONObject().apply {
        put("notificationType", notificationType)
        put("daysSinceLastOpen", daysSinceLastOpen)
        put("currentStreakDays", currentStreakDays)
    }
    is BasheerEvent.XpLevelUp -> JSONObject().apply {
        put("newLevel", newLevel)
        put("totalXp", totalXp)
        put("xpSource", xpSource)
    }
    is BasheerEvent.StreakMilestone -> JSONObject().apply {
        put("streakDays", streakDays)
        put("streakLevel", streakLevel)
    }
}.toString()

// ─────────────────────────────────────────────────────────────────────────────
// LearningSignal → JSON serialization
// ─────────────────────────────────────────────────────────────────────────────

private fun LearningSignal.toJson(): String = when (this) {
    is LearningSignal.CheckpointAttempted -> JSONObject().apply {
        put("questionId", questionId)
        put("lessonId", lessonId)
        put("sectionId", sectionId)
        put("subjectId", subjectId)
        put("unitId", unitId)
        put("partIndex", partIndex)
        put("questionType", questionType)
        put("userAnswer", userAnswer)
        put("correctAnswer", correctAnswer)
        put("isCorrect", isCorrect)
        put("timeSpentSeconds", timeSpentSeconds)
    }
    is LearningSignal.FeedQuestionAnswered -> JSONObject().apply {
        put("questionId", questionId)
        put("feedCardId", feedCardId)
        put("subjectId", subjectId)
        conceptId?.let { put("conceptId", it) }
        put("questionType", questionType)
        put("userAnswer", userAnswer)
        put("correctAnswer", correctAnswer)
        put("isCorrect", isCorrect)
        put("timeSpentSeconds", timeSpentSeconds)
        put("cardPositionInSession", cardPositionInSession)
        put("srIntervalDaysBefore", srIntervalDaysBefore)
    }
    is LearningSignal.PracticeQuestionAnswered -> JSONObject().apply {
        put("questionId", questionId)
        put("sessionId", sessionId)
        put("subjectId", subjectId)
        unitId?.let { put("unitId", it) }
        lessonId?.let { put("lessonId", it) }
        put("questionType", questionType)
        put("generationType", generationType)
        put("userAnswer", userAnswer)
        put("correctAnswer", correctAnswer)
        put("isCorrect", isCorrect)
        put("wasSkipped", wasSkipped)
        put("timeSpentSeconds", timeSpentSeconds)
        put("positionInSession", positionInSession)
        put("attemptNumber", attemptNumber)
        put("difficulty", difficulty)
        put("cognitiveLevel", cognitiveLevel)
    }
    is LearningSignal.ExamQuestionEvaluated -> JSONObject().apply {
        put("questionId", questionId)
        put("attemptId", attemptId)
        put("examId", examId)
        put("subjectId", subjectId)
        put("examType", examType)
        sectionTitle?.let { put("sectionTitle", it) }
        put("questionType", questionType)
        put("userAnswer", userAnswer)
        put("correctAnswer", correctAnswer)
        put("isCorrect", isCorrect)
        put("wasUnanswered", wasUnanswered)
        put("wasFlagged", wasFlagged)
        put("positionInExam", positionInExam)
        put("pointsAwarded", pointsAwarded)
        put("pointsAvailable", pointsAvailable)
        put("difficulty", difficulty)
        put("cognitiveLevel", cognitiveLevel)
        put("source", source)
        sourceYear?.let { put("sourceYear", it) }
    }
}.toString()

/** JSONObject → Map<String, Any> for Firestore. */
@Suppress("UNCHECKED_CAST")
private fun JSONObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    keys().forEach { key -> map[key] = get(key) }
    return map
}