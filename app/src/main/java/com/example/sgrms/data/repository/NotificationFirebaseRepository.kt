package com.example.sgrms.data.repository

import com.example.sgrms.data.firebase.NotificationFirebaseService
import com.example.sgrms.data.model.NotificationModel

class NotificationFirebaseRepository(
    private val notificationFirebaseService: NotificationFirebaseService
) {
    suspend fun addNotification(
        notification: NotificationModel
    ): NotificationModel {
        return notificationFirebaseService.addNotification(notification)
    }

    suspend fun getNotificationsByStudentId(
        studentId: String
    ): List<NotificationModel> {
        return notificationFirebaseService.getNotificationsByStudentId(studentId)
    }

    suspend fun markNotificationAsRead(
        notificationId: String
    ) {
        notificationFirebaseService.markNotificationAsRead(notificationId)
    }

    suspend fun markAllNotificationsAsRead(
        studentId: String
    ) {
        notificationFirebaseService.markAllNotificationsAsRead(studentId)
    }

    suspend fun deleteNotification(
        notificationId: String
    ) {
        notificationFirebaseService.deleteNotification(notificationId)
    }
}