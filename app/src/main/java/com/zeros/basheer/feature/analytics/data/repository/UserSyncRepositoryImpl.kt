package com.zeros.basheer.feature.analytics.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.zeros.basheer.feature.analytics.domain.model.AnalyticsConsent
import com.zeros.basheer.feature.analytics.domain.repository.AnalyticsRepository
import com.zeros.basheer.feature.analytics.domain.repository.UserSyncRepository
import com.zeros.basheer.feature.practice.data.dao.PracticeSessionDao
import com.zeros.basheer.feature.progress.data.dao.ProgressDao
import com.zeros.basheer.feature.streak.data.dao.DailyActivityDao
import com.zeros.basheer.feature.user.data.dao.XpDao
import com.zeros.basheer.feature.user.domain.repository.UserPreferencesRepository
import com.zeros.basheer.feature.user.domain.repository.UserProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val analyticsRepository: AnalyticsRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val userProfileRepository: UserProfileRepository,
    private val xpDao: XpDao,
    private val dailyActivityDao: DailyActivityDao,
    private val progressDao: ProgressDao,
    private val practiceSessionDao: PracticeSessionDao,
) : UserSyncRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("basheer_user_sync", Context.MODE_PRIVATE)

    override suspend fun syncUser(): Result<Unit> = runCatching {
        val consent = preferencesRepository.getAnalyticsConsent()

        // NONE consent → no Firestore writes at all
        if (!consent.isEnabled) return@runCatching

        val installId = analyticsRepository.installId
        val isFirstSync = !prefs.getBoolean(KEY_HAS_SYNCED, false)

        // ── Stats (always collected for FULL + ANONYMOUS) ─────────────────────
        val totalXp          = xpDao.getTotalXp()
        val lessonsCompleted = progressDao.getCompletedLessonsCount().first()
        val questionsAnswered = dailyActivityDao.getTotalQuestionsAnswered().first() ?: 0
        val cardsReviewed    = dailyActivityDao.getTotalCardsReviewed().first() ?: 0
        val practiceCompleted = practiceSessionDao.getTotalCompletedSessions()
        val totalTimeSeconds = dailyActivityDao.getTotalTimeSpent().first() ?: 0L
        val activeDays       = dailyActivityDao.getActiveDaysCount(EPOCH_DATE)

        val stats = mapOf(
            "totalXp"                    to totalXp,
            "lessonsCompleted"           to lessonsCompleted,
            "questionsAnswered"          to questionsAnswered,
            "cardsReviewed"              to cardsReviewed,
            "practiceSessionsCompleted"  to practiceCompleted,
            "totalStudyMinutes"          to (totalTimeSeconds / 60).toInt(),
            "activeDays"                 to activeDays,
        )

        // ── Build document ────────────────────────────────────────────────────
        val doc = mutableMapOf<String, Any>(
            "installId"    to installId,
            "consentTier"  to consent.name,
            "appVersion"   to appVersion(),
            "lastSeenAt"   to Timestamp.now(),
            "stats"        to stats,
        )

        // firstSeenAt only written on first sync — merge() won't overwrite it on subsequent calls
        // but we set it explicitly on creation so it reflects the real first sync time
        if (isFirstSync) {
            doc["firstSeenAt"] = Timestamp.now()
        }

        // ── Profile (FULL consent only) ───────────────────────────────────────
        if (consent == AnalyticsConsent.FULL) {
            val profile = userProfileRepository.getProfileOnce()
            if (profile != null) {
                val profileMap = mutableMapOf<String, Any>(
                    "studentPath" to profile.studentPath.name,
                )
                // Only include optional fields if they were provided
                profile.state?.let        { profileMap["state"]        = it }
                profile.city?.let         { profileMap["city"]         = it }
                profile.schoolName?.let   { profileMap["schoolName"]   = it }
                profile.major?.let        { profileMap["major"]        = it }
                profileMap["dailyStudyMinutes"] = profile.dailyStudyMinutes

                doc["profile"] = profileMap
            }
        }

        // ── Upsert ────────────────────────────────────────────────────────────
        firestore
            .collection(COLLECTION_USERS)
            .document(installId)
            .set(doc, SetOptions.merge())
            .await()

        // Mark first sync complete
        if (isFirstSync) {
            prefs.edit().putBoolean(KEY_HAS_SYNCED, true).apply()
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun appVersion(): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    }.getOrDefault("unknown")

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val KEY_HAS_SYNCED   = "has_synced"
        // Far-past date so getActiveDaysCount returns all-time count
        private const val EPOCH_DATE       = "1970-01-01"
    }
}