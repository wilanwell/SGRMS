package com.wilanwell.sgrms.data.firebase

import com.wilanwell.sgrms.data.model.NotificationModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationFirebaseService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
     private val notificationsCollection = firestore.collection("notifications")

    suspend fun addNotification(
        notification: NotificationModel
    ): NotificationModel {
        val documentReference = if (notification.notificationId.isNotBlank()) {
            notificationsCollection.document(notification.notificationId)
        } else {
            notificationsCollection.document()
        }

        val notificationWithId = notification.copy(
            notificationId = documentReference.id,
            isRead = false,
            createdAt = if (notification.createdAt == 0L) {
                System.currentTimeMillis()
            } else {
                notification.createdAt
            }
        )

        documentReference
            .set(notificationWithId.toFirestoreMap())
            .await()

        return notificationWithId
    }

    suspend fun getNotificationsByStudentId(
        studentId: String
    ): List<NotificationModel> {
        if (studentId.isBlank()) {
            return emptyList()
        }

        return notificationsCollection
            .whereEqualTo("studentId", studentId)
            .get()
            .await()
            .documents
            .map { document ->
                document.toNotificationModel()
            }
            .sortedByDescending { notification ->
                notification.createdAt
            }
    }

    suspend fun markNotificationAsRead(
        notificationId: String
    ) {
        if (notificationId.isBlank()) {
            throw IllegalArgumentException("Notification ID is missing")
        }

        notificationsCollection
            .document(notificationId)
            .update(
                mapOf(
                    "isRead" to true,
                    "read" to true
                )
            )
            .await()
    }

    suspend fun markAllNotificationsAsRead(
        studentId: String
    ) {
        if (studentId.isBlank()) {
            throw IllegalArgumentException("Student ID is missing")
        }

        val unreadNotificationDocuments = notificationsCollection
            .whereEqualTo("studentId", studentId)
            .get()
            .await()
            .documents
            .filter { document ->
                val isReadValue = document.getBoolean("isRead")
                    ?: document.getBoolean("read")
                    ?: false

                !isReadValue
            }

        if (unreadNotificationDocuments.isEmpty()) {
            return
        }

        val batch = firestore.batch()

        unreadNotificationDocuments.forEach { document ->
            batch.update(
                document.reference,
                mapOf(
                    "isRead" to true,
                    "read" to true
                )
            )
        }

        batch.commit().await()
    }

    suspend fun deleteNotification(
        notificationId: String
    ) {
        if (notificationId.isBlank()) {
            throw IllegalArgumentException("Notification ID is missing")
        }

        notificationsCollection
            .document(notificationId)
            .delete()
            .await()
    }

    private fun NotificationModel.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "notificationId" to notificationId,
            "studentId" to studentId,
            "title" to title,
            "message" to message,
            "type" to type,
            "relatedGradeId" to relatedGradeId,
            "relatedSubjectId" to relatedSubjectId,
            "isRead" to isRead,
            "read" to isRead,
            "createdAt" to createdAt
        )
    }

    private fun DocumentSnapshot.toNotificationModel(): NotificationModel {
        return NotificationModel(
            notificationId = id,
            studentId = getString("studentId") ?: "",
            title = getString("title") ?: "",
            message = getString("message") ?: "",
            type = getString("type") ?: "",
            relatedGradeId = getString("relatedGradeId") ?: "",
            relatedSubjectId = getString("relatedSubjectId") ?: "",
            isRead = getBoolean("isRead") ?: getBoolean("read") ?: false,
            createdAt = getLong("createdAt") ?: 0L
        )
    }
}