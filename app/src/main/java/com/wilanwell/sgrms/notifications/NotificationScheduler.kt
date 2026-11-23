package com.wilanwell.sgrms.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Starts / stops the periodic [NotificationCheckWorker] for a student session. */
@Singleton
class NotificationScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun startForStudent() {
        val request = PeriodicWorkRequestBuilder<NotificationCheckWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Stops checking on logout. The per-student "last checked" time is kept on
     * purpose: grades entered while the student was logged out are then shown
     * as phone notifications at their next login instead of being skipped.
     */
    fun stop() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "sgrms-notification-check"
    }
}
