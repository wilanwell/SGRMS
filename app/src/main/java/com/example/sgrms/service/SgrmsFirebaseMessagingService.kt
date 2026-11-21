package com.example.sgrms.service

import com.example.sgrms.data.repository.DeviceTokenRepository
import com.example.sgrms.data.session.SessionManager
import com.example.sgrms.notifications.GradeNotifications
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@AndroidEntryPoint
class SgrmsFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var deviceTokenRepository: DeviceTokenRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val studentId = sessionManager.currentStudentId
        if (studentId.isBlank()) return

        serviceScope.launch {
            runCatching { deviceTokenRepository.saveToken(studentId = studentId, token = token) }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "SGRMS"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new notification."

        GradeNotifications.show(
            context = this,
            title = title,
            body = body,
            notificationKey = message.data["notificationId"] ?: message.messageId.orEmpty()
        )
    }

}