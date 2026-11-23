package com.example.sgrms.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sgrms.data.model.UserRole
import com.example.sgrms.data.model.userRole
import com.example.sgrms.data.repository.AuthRepository
import com.example.sgrms.data.repository.NotificationFirebaseRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Push notifications on the free Spark plan.
 *
 * Without Cloud Functions nothing can send FCM messages, so a student's
 * phone checks Firestore for new notifications about every 15 minutes
 * (the WorkManager minimum) and shows them as phone notifications.
 * Notifications created before the last check are never shown twice.
 */
class NotificationCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun authRepository(): AuthRepository
        fun notificationRepository(): NotificationFirebaseRepository
    }

    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java)

        val user = try {
            deps.authRepository().getCurrentUserProfile()
        } catch (e: Exception) {
            return Result.retry()
        }

        // Logged out, or not a student: nothing to show.
        if (user == null || user.userRole != UserRole.STUDENT || user.studentId.isBlank()) {
            return Result.success()
        }

        val store = NotificationCheckStore(applicationContext)
        val lastChecked = store.lastCheckedAt(user.studentId)

        if (lastChecked == null) {
            // First run for this student: start from now, do not replay history.
            store.setLastCheckedAt(user.studentId, System.currentTimeMillis())
            return Result.success()
        }

        val notifications = try {
            deps.notificationRepository().getNotificationsByStudentId(user.studentId)
        } catch (e: Exception) {
            return Result.retry()
        }

        val fresh = notifications
            .filter { it.createdAt > lastChecked && !it.isRead }
            .sortedBy { it.createdAt }

        fresh.forEach { notification ->
            GradeNotifications.show(
                context = applicationContext,
                title = notification.title.ifBlank { "SGRMS" },
                body = notification.message.ifBlank { "You have a new notification." },
                notificationKey = notification.notificationId
            )
        }

        val newest = notifications.maxOfOrNull { it.createdAt } ?: lastChecked
        store.setLastCheckedAt(user.studentId, maxOf(newest, lastChecked))

        return Result.success()
    }
}

/** Remembers, per student, the time of the newest notification already handled. */
internal class NotificationCheckStore(context: Context) {

    private val prefs = context.getSharedPreferences("sgrms_notification_check", Context.MODE_PRIVATE)

    fun lastCheckedAt(studentId: String): Long? =
        prefs.getLong(key(studentId), -1L).takeIf { it >= 0 }

    fun setLastCheckedAt(studentId: String, time: Long) {
        prefs.edit().putLong(key(studentId), time).apply()
    }

    private fun key(studentId: String) = "last_checked_$studentId"
}
