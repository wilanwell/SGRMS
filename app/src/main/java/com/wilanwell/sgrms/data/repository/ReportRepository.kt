package com.wilanwell.sgrms.data.repository

import com.wilanwell.sgrms.data.model.LEGACY_NOT_ASSIGNED
import com.wilanwell.sgrms.data.model.StudentModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Teacher-written remarks for a student's report and other report data. */
@Singleton
class ReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val remarksCollection = firestore.collection("reportRemarks")

    /** Remarks are stored at reportRemarks/{studentId}. */
    suspend fun getRemarks(studentId: String): String {
        if (studentId.isBlank()) return ""

        return remarksCollection
            .document(studentId)
            .get()
            .await()
            .getString("remarks")
            .orEmpty()
    }

    suspend fun saveRemarks(
        studentId: String,
        remarks: String
    ) {
        remarksCollection
            .document(studentId)
            .set(
                mapOf(
                    "studentId" to studentId,
                    "remarks" to remarks,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    /** Class teacher shown on the PDF report, or "" (the PDF then shows "Not assigned" in the reader's language). */
    suspend fun getClassTeacherName(student: StudentModel?): String {
        val notAssigned = ""
        if (student == null) return notAssigned

        return try {
            val classes = firestore.collection("classes")

            val classDocument = if (student.classId.isNotBlank()) {
                classes.document(student.classId).get().await()
            } else {
                classes
                    .whereEqualTo("className", student.className)
                    .limit(1)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
            }

            if (classDocument == null || !classDocument.exists()) return notAssigned

            classDocument.getString("classTeacherName")?.takeIf { it.isNotBlank() && it != LEGACY_NOT_ASSIGNED }
                ?: classDocument.getString("classTeacherId")
                    ?.takeIf { it.isNotBlank() }
                    ?.let { teacherId ->
                        firestore.collection("teachers")
                            .document(teacherId)
                            .get()
                            .await()
                            .getString("teacherName")
                    }
                    ?.takeIf { it.isNotBlank() }
                ?: notAssigned
        } catch (e: Exception) {
            notAssigned
        }
    }
}
