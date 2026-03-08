package com.zeros.basheer.feature.analytics.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.zeros.basheer.feature.analytics.domain.repository.AnalyticsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Periodic WorkManager job that drains the local analytics queue to Firestore.
 *
 * Schedule: once per day, requires unmetered network.
 *
 * Free-tier math:
 *   - 1 Firestore document write per day bucket per user
 *   - Daily schedule → ≤1 write/user/day
 *   - 1,000 DAU → ~1,000 writes/day (free tier allows 20,000/day ✓)
 *   - Each document holds all events for that day (~2–10 KB)
 *   - Storage: 1,000 users × 365 days × 5 KB avg = ~1.8 GB/year (free tier = 1 GB,
 *     so plan to add TTL cleanup rules in Firestore console after launch)
 *
 * Retry strategy: exponential backoff, max 3 attempts.
 * If upload fails, events stay in Room and will be included in the next run.
 */
@HiltWorker
class AnalyticsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: AnalyticsRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting analytics sync")

        return repository.uploadPendingBatches()
            .onSuccess { count ->
                Log.d(TAG, "Uploaded $count events")
                repository.pruneOldSyncedEvents(daysToKeep = 30)
            }
            .onFailure { e ->
                Log.w(TAG, "Upload failed: ${e.message}")
            }
            .fold(
                onSuccess = { Result.success() },
                onFailure = {
                    if (runAttemptCount < MAX_RETRIES) Result.retry()
                    else Result.failure()
                }
            )
    }

    companion object {
        private const val TAG = "AnalyticsSyncWorker"
        private const val WORK_NAME = "analytics_daily_sync"
        private const val WORK_NAME_IMMEDIATE = "analytics_immediate_sync"
        private const val MAX_RETRIES = 3

        /**
         * Schedule the daily sync job.
         * Call this from [BasheerApp.onCreate].
         *
         * [ExistingPeriodicWorkPolicy.KEEP] ensures we don't reset the timer
         * if the app restarts mid-day.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<AnalyticsSyncWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS,
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.MINUTES,
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )

            Log.d(TAG, "Daily analytics sync scheduled")
        }

        /**
         * Schedule a one-time immediate sync.
         *
         * Called from [AppLifecycleObserver.onStop] when a meaningful session ends
         * (>= [MIN_SESSION_SECONDS_FOR_EARLY_SYNC] seconds).
         *
         * This ensures events from an active study session reach Firestore within
         * minutes rather than waiting for the next daily window — critical for
         * detecting churned users and triggering timely re-engagement.
         *
         * [ExistingWorkPolicy.KEEP] prevents resetting a queued sync if the user
         * rapidly opens and closes the app multiple times.
         */
        fun scheduleImmediate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<AnalyticsSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15, TimeUnit.MINUTES,
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_IMMEDIATE,
                ExistingWorkPolicy.KEEP,
                request,
            )

            Log.d(TAG, "Immediate analytics sync scheduled")
        }

        /** Sessions shorter than this are noise (e.g. accidental opens). */
        const val MIN_SESSION_SECONDS_FOR_EARLY_SYNC = 120
    }
}