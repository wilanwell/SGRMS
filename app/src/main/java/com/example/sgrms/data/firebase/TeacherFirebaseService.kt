package com.example.sgrms.data.firebase

import com.example.sgrms.data.model.TeacherModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class TeacherFirebaseService(
    private val accountFunctions: AccountFunctionsService = AccountFunctionsService()
) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val teachersCollection = firestore.collection("teachers")
    private val usersCollection = firestore.collection("users")

    /**
     * Creates the login account and the teacher profile on the server
     * (Cloud Function `createTeacherAccount`), so the admin stays signed in.
     */
    suspend fun addTeacherWithAccount(
        staffId: String,
        teacherName: String,
        email: String,
        phoneNumber: String,
        department: String,
        position: String,
        password: String
    ): TeacherModel {
        val teacherId = accountFunctions.createTeacherAccount(
            staffId = staffId,
            teacherName = teacherName,
            email = email,
            phoneNumber = phoneNumber,
            department = department,
            position = position,
            password = password
        )

        return getTeacherById(teacherId)
            ?: throw IllegalStateException("Teacher profile not found after creation.")
    }

    suspend fun getAllTeachers(): List<TeacherModel> {
        return teachersCollection
            .get()
            .await()
            .toObjects(TeacherModel::class.java)
    }

    suspend fun getTeacherById(
        teacherId: String
    ): TeacherModel? {
        return teachersCollection
            .document(teacherId)
            .get()
            .await()
            .toObject(TeacherModel::class.java)
    }

    suspend fun updateTeacher(
        teacher: TeacherModel
    ) {
        val batch = firestore.batch()

        // merge keeps fields that are not part of TeacherModel.
        batch.set(
            teachersCollection.document(teacher.teacherId),
            teacher.copy(updatedAt = System.currentTimeMillis()),
            SetOptions.merge()
        )

        if (teacher.userId.isNotBlank()) {
            batch.update(
                usersCollection.document(teacher.userId),
                mapOf(
                    "fullName" to teacher.teacherName,
                    "email" to teacher.email,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
        }

        batch.commit().await()
    }

    /**
     * Deletes the login account, the profile documents and unassigns the
     * teacher's subjects and classes (Cloud Function `deleteUserAccount`).
     */
    suspend fun deleteTeacher(
        teacherId: String
    ) {
        accountFunctions.deleteAccount(teacherId = teacherId)
    }
}
