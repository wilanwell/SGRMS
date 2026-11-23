package com.wilanwell.sgrms.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class NotificationModel(
    val notificationId: String = "",
    val studentId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val relatedGradeId: String = "",
    val relatedSubjectId: String = "",
    /** Values for grade notifications, so the reader's phone can build the text in its own language. */
    val subjectName: String = "",
    val marks: Double = 0.0,
    val gradeLetter: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)