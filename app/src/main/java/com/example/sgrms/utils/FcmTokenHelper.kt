package com.example.sgrms.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object FcmTokenHelper {

    fun saveStudentFcmToken(
        context: Context,
        studentId: String
    ) {
        if (studentId.isBlank()) {
            return
        }

        context.getSharedPreferences("sgrms_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("currentStudentId", studentId)
            .apply()

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (token.isNotBlank()) {
                    FirebaseFirestore.getInstance()
                        .collection("students")
                        .document(studentId)
                        .update("fcmToken", token)
                }
            }
    }
}