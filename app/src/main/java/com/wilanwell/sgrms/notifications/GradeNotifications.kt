package com.wilanwell.sgrms.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.wilanwell.sgrms.MainActivity
import com.wilanwell.sgrms.R

/** Shows SGRMS grade notifications on the phone (from FCM or the worker). */
object GradeNotifications {

    private const val CHANNEL_ID = "sgrms_grade_notifications"

    fun show(
        context: Context,
        title: String,
        body: String,
        notificationKey: String = System.currentTimeMillis().toString()
    ) {
        val manager = NotificationManagerCompat.from(context)

        // Android 13+: the user may have refused POST_NOTIFICATIONS.
        if (!manager.areNotificationsEnabled()) return

        ensureChannel(context)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_sgrms)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            manager.notify(notificationKey.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission revoked between the check and the call.
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Grade Notifications", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }
}
