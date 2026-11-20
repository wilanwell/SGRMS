package com.example.sgrms.utils

import com.example.sgrms.data.model.StudentModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

suspend fun createReportPdfDataFromDatabase(
    reportId: String,
    studentId: String,
    totalMarks: Double,
    averageMarks: Double,
    overallGrade: String,
    remarks: String,
    generatedDate: String,
    student: StudentModel?
): ReportPdfData {
    val firestore = FirebaseFirestore.getInstance()

    val classTeacherName = try {
        val classDocument = if (student?.classId?.isNotBlank() == true) {
            firestore
                .collection("classes")
                .document(student.classId)
                .get()
                .await()
        } else {
            firestore
                .collection("classes")
                .whereEqualTo("className", student?.className.orEmpty())
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
        }

        if (classDocument != null && classDocument.exists()) {
            val teacherNameFromClass = classDocument
                .getString("classTeacherName")
                .orEmpty()

            if (teacherNameFromClass.isNotBlank()) {
                teacherNameFromClass
            } else {
                val teacherId = classDocument
                    .getString("classTeacherId")
                    .orEmpty()

                if (teacherId.isNotBlank()) {
                    val teacherDocument = firestore
                        .collection("teachers")
                        .document(teacherId)
                        .get()
                        .await()

                    teacherDocument
                        .getString("teacherName")
                        ?.takeIf { teacherName ->
                            teacherName.isNotBlank()
                        }
                        ?: "Not assigned"
                } else {
                    "Not assigned"
                }
            }
        } else {
            "Not assigned"
        }
    } catch (e: Exception) {
        "Not assigned"
    }

    return ReportPdfData(
        reportId = reportId,
        studentId = studentId,
        totalMarks = totalMarks,
        averageMarks = averageMarks,
        overallGrade = overallGrade,
        remarks = remarks,
        generatedDate = generatedDate,
        classTeacherName = classTeacherName
    )
}