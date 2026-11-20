package com.example.sgrms.data.firebase

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calls the SGRMS Cloud Functions that create and delete login accounts.
 *
 * Accounts must be created on the server: calling
 * FirebaseAuth.createUserWithEmailAndPassword from the app signs the admin
 * out and signs in as the new user.
 */
@Singleton
class AccountFunctionsService @Inject constructor(
    private val functions: FirebaseFunctions
) {

    /** Returns the new teacher's document id. */
    suspend fun createTeacherAccount(
        staffId: String,
        teacherName: String,
        email: String,
        phoneNumber: String,
        department: String,
        position: String,
        password: String
    ): String {
        val result = call(
            name = "createTeacherAccount",
            data = mapOf(
                "staffId" to staffId,
                "teacherName" to teacherName,
                "email" to email,
                "phoneNumber" to phoneNumber,
                "department" to department,
                "position" to position,
                "password" to password
            )
        )

        return result["teacherId"] as? String
            ?: throw IllegalStateException("Teacher account was not created.")
    }

    /** Returns the new student's document id. */
    suspend fun createStudentAccount(
        studentCode: String,
        studentName: String,
        email: String,
        phoneNumber: String,
        classId: String,
        className: String,
        gender: String,
        status: String,
        password: String
    ): String {
        val result = call(
            name = "createStudentAccount",
            data = mapOf(
                "studentCode" to studentCode,
                "studentName" to studentName,
                "email" to email,
                "phoneNumber" to phoneNumber,
                "classId" to classId,
                "className" to className,
                "gender" to gender,
                "status" to status,
                "password" to password
            )
        )

        return result["studentId"] as? String
            ?: throw IllegalStateException("Student account was not created.")
    }

    /**
     * Deletes the login account, profile documents and related records.
     * Pass exactly one of the ids.
     */
    suspend fun deleteAccount(
        userId: String = "",
        teacherId: String = "",
        studentId: String = ""
    ) {
        call(
            name = "deleteUserAccount",
            data = mapOf(
                "userId" to userId,
                "teacherId" to teacherId,
                "studentId" to studentId
            )
        )
    }

    private suspend fun call(
        name: String,
        data: Map<String, Any>
    ): Map<*, *> {
        try {
            val result = functions
                .getHttpsCallable(name)
                .call(data)
                .await()

            return result.getData() as? Map<*, *> ?: emptyMap<String, Any>()
        } catch (e: FirebaseFunctionsException) {
            // Messages from HttpsError on the server are already readable.
            throw IllegalStateException(e.message ?: "Request failed.", e)
        }
    }
}
