package com.example.sgrms.data.repository

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the student's FCM token on students/{studentId}.fcmToken so the
 * Cloud Function sendNotificationPush can reach this phone.
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
