package com.example.sgrms.data.firebase

import com.example.sgrms.data.model.StudentModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class StudentFirebaseService(
    private val accountFunctions: AccountFunctionsService = AccountFunctionsService()
) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val studentsCollection = firestore.collection("students")
    private val usersCollection = firestore.collection("users")

    /**
     * Creates the login account and the student profile on the server
     * (Cloud Function `createStudentAccount`), so the admin stays signed in.
     */
    suspend fun addStudentWithAccount(
        studentCode: String,
        studentName: String,
        email: String,
        phoneNumber: String,
        classId: String,
        className: String,
        gender: String,
        status: String,
        password: String
    ): StudentModel {
        val studentId = accountFunctions.createStudentAccount(
            studentCode = studentCode,
            studentName = studentName,
            email = email,
            phoneNumber = phoneNumber,
            classId = classId,
            className = className,
            gender = gender,
            status = status,
            password = password
        )

        return getStudentById(studentId)
            ?: throw IllegalStateException("Student profile not found after creation.")
    }

    suspend fun getAllStudents(): List<StudentModel> {
        return studentsCollection
            .get()
            .await()
            .toObjects(StudentModel::class.java)
    }

    suspend fun getStudentById(
        studentId: String
    ): StudentModel? {
        return studentsCollection
            .document(studentId)
            .get()
            .await()
            .toObject(StudentModel::class.java)
    }

    suspend fun updateStudent(
        student: StudentModel
    ) {
        val batch = firestore.batch()

        // merge keeps fields that are not part of StudentModel, such as
        // fcmToken; a plain set() would erase it and stop push notifications.
        batch.set(
            studentsCollection.document(student.studentId),
            student.copy(updatedAt = System.currentTimeMillis()),
            SetOptions.merge()
        )

        if (student.userId.isNotBlank()) {
            batch.update(
                usersCollection.document(student.userId),
                mapOf(
                    "fullName" to student.studentName,
                    "email" to student.email,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
        }

        batch.commit().await()
    }

    /**
     * Deletes the login account, the profile documents, grades,
     * notifications and report remarks (Cloud Function `deleteUserAccount`).
     */
    suspend fun deleteStudent(
        studentId: String
    ) {
        accountFunctions.deleteAccount(studentId = studentId)
    }
}
