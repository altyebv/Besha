package com.zeros.basheer

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.zeros.basheer.feature.analytics.sync.AnalyticsSyncWorker
import com.zeros.basheer.feature.analytics.sync.AppLifecycleObserver
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
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
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        AnalyticsSyncWorker.schedule(this)
    }
}