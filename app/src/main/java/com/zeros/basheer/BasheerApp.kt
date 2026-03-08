package com.zeros.basheer

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.zeros.basheer.feature.analytics.sync.AnalyticsSyncWorker
import com.zeros.basheer.feature.analytics.sync.AppLifecycleObserver
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.zeros.basheer.feature.user.notifications.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BasheerApp : Application(), Configuration.Provider {

    @Inject lateinit var appLifecycleObserver: AppLifecycleObserver
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // ── Notification channels ─────────────────────────────────────────────
        // Must run before any notification can be posted. Idempotent — safe to
        // call on every launch. Doing it here (Application level) ensures channels
        // exist even if a notification fires before MainActivity is created
        // (e.g. ReminderReceiver fires while app is closed).
        NotificationChannels.createAll(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        AnalyticsSyncWorker.schedule(this)
    }
}