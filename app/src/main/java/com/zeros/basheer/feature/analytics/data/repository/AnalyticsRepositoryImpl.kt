package com.zeros.basheer.feature.analytics.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.zeros.basheer.feature.analytics.data.dao.AnalyticsEventDao
import com.zeros.basheer.feature.analytics.data.entity.AnalyticsEventEntity
import com.zeros.basheer.feature.analytics.domain.model.BasheerEvent
import com.zeros.basheer.feature.analytics.domain.repository.AnalyticsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
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
) : AnalyticsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("basheer_analytics", Context.MODE_PRIVATE)

    // ─────────────────────────────────────────────────────────────────────────
    // Install identity — stable, no auth required
    // ─────────────────────────────────────────────────────────────────────────

    val installId: String
        get() = prefs.getString(KEY_INSTALL_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_INSTALL_ID, it).apply()
        }

    // ─────────────────────────────────────────────────────────────────────────
    // Session tracking (in-memory, rebuilt each foreground)
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

    // ─────────────────────────────────────────────────────────────────────────
    // Upload — one Firestore document per day bucket
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
     *     events: [
     *       { type: "LessonCompleted", occurredAt: 1234567890, sessionId: "...", ...payload },
     *       ...
     *     ]
     *   }
     *
     * Using SetOptions.merge() means a second upload for the same day *appends*
     * new events rather than overwriting — safe for partial-day uploads.
     *
     * Free-tier cost: 1 document write per bucket (day) per upload run.
     * With daily batching: ~1 write/user/day. Very comfortable under the 20k/day limit.
     */
    override suspend fun uploadPendingBatches(): Result<Int> = runCatching {
        val buckets = dao.getUnsyncedDateBuckets()
        var totalUploaded = 0

        for (bucket in buckets) {
            val rows = dao.getUnsyncedForDate(bucket)
            if (rows.isEmpty()) continue

            val eventsArray = rows.map { row ->
                val base = JSONObject(row.payload)
                base.put("_type", row.eventType)
                base.put("_occurredAt", row.occurredAt)
                base.put("_sessionId", row.sessionId)
                base
            }

            val docData = hashMapOf(
                "installId" to installId,
                "date" to bucket,
                "appVersion" to appVersion(),
                "uploadedAt" to com.google.firebase.Timestamp.now(),
                "events" to eventsArray.map { it.toMap() },
            )

            firestore
                .collection("analytics")
                .document(installId)
                .collection("days")
                .document(bucket)
                .set(docData, SetOptions.merge())
                .await()

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

    private fun todayBucket(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun appVersion(): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    }.getOrDefault("unknown")

    companion object {
        private const val KEY_INSTALL_ID = "install_id"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BasheerEvent → JSON serialization
// Pure reflection-free manual mapping keeps the APK lean (no Gson/Moshi dep needed
// beyond what Firebase already brings).
// ─────────────────────────────────────────────────────────────────────────────

private fun BasheerEvent.toJson(): String = when (this) {
    is BasheerEvent.AppSessionStarted -> JSONObject().apply {
        put("streakDays", streakDays)
        put("totalXp", totalXp)
        put("level", level)
        put("studentPath", studentPath)
        put("daysSinceLastOpen", daysSinceLastOpen)
        put("appVersion", appVersion)
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
        put("hasTargetExamDate", hasTargetExamDate)
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
    is BasheerEvent.ExamCompleted -> JSONObject().apply {
        put("examId", examId)
        put("subjectId", subjectId)
        put("examType", examType)
        put("totalQuestions", totalQuestions)
        put("score", score)
        put("durationSeconds", durationSeconds)
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

/** JSONObject → Map<String, Any> for Firestore. */
@Suppress("UNCHECKED_CAST")
private fun JSONObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    keys().forEach { key -> map[key] = get(key) }
    return map
}