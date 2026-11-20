package com.example.sgrms.data.model

data class NotificationModel(
    val notificationId: String = "",
    val studentId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val relatedGradeId: String = "",
    val relatedSubjectId: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)