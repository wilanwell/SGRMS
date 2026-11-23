package com.wilanwell.sgrms.data.repository

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the student's FCM token on students/{studentId}.fcmToken so the
 * optional Cloud Function sendNotificationPush (Blaze plan only) can reach
 * this phone. On Spark, NotificationCheckWorker shows notifications instead.
 */
@Singleton
class DeviceTokenRepository @Inject constructor(
    private val messaging: FirebaseMessaging,
    private val studentRepository: StudentFirebaseRepository
) {

    suspend fun registerCurrentDevice(studentId: String) {
        if (studentId.isBlank()) return

        val token = messaging.token.await()
        if (token.isNotBlank()) {
            studentRepository.saveFcmToken(studentId = studentId, token = token)
        }
    }

    suspend fun saveToken(studentId: String, token: String) {
        if (studentId.isBlank() || token.isBlank()) return
        studentRepository.saveFcmToken(studentId = studentId, token = token)
    }
}
