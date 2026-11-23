package com.wilanwell.sgrms.data.firebase

import com.wilanwell.sgrms.utils.UserMessageException
import com.wilanwell.sgrms.R
import android.content.Context
import com.wilanwell.sgrms.data.model.UserRole
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates and deletes login accounts from the Admin's phone, without Cloud
 * Functions, so SGRMS runs on the free Spark plan.
 *
 * Creating: `createUserWithEmailAndPassword` signs in as the new user, so it
 * runs on a second FirebaseApp instance ([ACCOUNT_CREATOR_APP]). The Admin's
 * own session on the default app is never touched. The profile documents
 * are then written by the Admin, which firestore.rules allows only for
 * Admins and only for the Teacher / Student roles.
 *
 * Deleting: the profile, linked records and photo are removed. The Firebase
 * Auth login itself can only be deleted with the Admin SDK (a server), so it
 * stays in Authentication but is useless: with no users/{uid} profile the
 * app signs it out at once and the rules deny it all data.
 */
@Singleton
class AccountService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val users = firestore.collection("users")
    private val teachers = firestore.collection("teachers")
    private val students = firestore.collection("students")

    private val creatorAuth: FirebaseAuth by lazy {
        val app = FirebaseApp.getApps(context).firstOrNull { it.name == ACCOUNT_CREATOR_APP }
            ?: FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, ACCOUNT_CREATOR_APP)
        FirebaseAuth.getInstance(app)
    }

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
        val teacherRef = teachers.document()
        val now = System.currentTimeMillis()

        createLoginThen(email = email, password = password) { uid ->
            firestore.batch()
                .set(
                    teacherRef,
                    mapOf(
                        "teacherId" to teacherRef.id,
                        "staffId" to staffId.trim(),
                        "teacherName" to teacherName.trim(),
                        "email" to email.trim().lowercase(),
                        "phoneNumber" to phoneNumber.trim(),
                        "department" to department.trim(),
                        "position" to position.trim().ifBlank { UserRole.TEACHER.value },
                        "userId" to uid,
                        "createdAt" to now,
                        "updatedAt" to now
                    )
                )
                .set(users.document(uid), userProfile(uid, teacherName, email, UserRole.TEACHER, teacherId = teacherRef.id, now = now))
                .commit()
                .await()
        }

        return teacherRef.id
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
        val studentRef = students.document()
        val now = System.currentTimeMillis()

        createLoginThen(email = email, password = password) { uid ->
            firestore.batch()
                .set(
                    studentRef,
                    mapOf(
                        "studentId" to studentRef.id,
                        "studentCode" to studentCode.trim(),
                        "studentName" to studentName.trim(),
                        "email" to email.trim().lowercase(),
                        "phoneNumber" to phoneNumber.trim(),
                        "classId" to classId,
                        "className" to className,
                        "userId" to uid,
                        "gender" to gender,
                        "status" to status.ifBlank { "Active" },
                        "createdAt" to now,
                        "updatedAt" to now
                    )
                )
                .set(users.document(uid), userProfile(uid, studentName, email, UserRole.STUDENT, studentId = studentRef.id, now = now))
                .commit()
                .await()
        }

        return studentRef.id
    }

    /**
     * Deletes the profile documents and everything linked to the account.
     * Pass exactly one of the ids.
     */
    suspend fun deleteAccount(
        userId: String = "",
        teacherId: String = "",
        studentId: String = ""
    ) {
        var uid = userId
        var tId = teacherId
        var sId = studentId

        if (tId.isNotBlank() && uid.isBlank()) uid = teachers.document(tId).get().await().getString("userId").orEmpty()
        if (sId.isNotBlank() && uid.isBlank()) uid = students.document(sId).get().await().getString("userId").orEmpty()

        if (uid.isNotBlank()) {
            val user = users.document(uid).get().await()

            if (user.getString("role") == UserRole.ADMIN.value) {
                throw UserMessageException(R.string.admin_accounts_cannot_be_deleted_from)
            }

            if (tId.isBlank()) tId = user.getString("teacherId").orEmpty()
            if (sId.isBlank()) sId = user.getString("studentId").orEmpty()
        }

        if (uid.isNotBlank() && uid == firebaseAuth.currentUser?.uid) {
            throw UserMessageException(R.string.you_cannot_delete_your_own_account)
        }

        // Linked records first, so a failure never leaves orphans behind a
        // deleted profile.
        if (sId.isNotBlank()) {
            deleteAll(firestore.collection("grades").whereEqualTo("studentId", sId))
            deleteAll(firestore.collection("notifications").whereEqualTo("studentId", sId))
            deleteAll(firestore.collection("reports").whereEqualTo("studentId", sId))
        }

        if (tId.isNotBlank()) {
            updateAll(
                firestore.collection("subjects").whereEqualTo("teacherId", tId),
                mapOf("teacherId" to "", "teacherName" to "")
            )
            updateAll(
                firestore.collection("classes").whereEqualTo("classTeacherId", tId),
                mapOf("classTeacherId" to "", "classTeacherName" to "")
            )
        }

        val profileDocs = buildList<DocumentReference> {
            if (sId.isNotBlank()) {
                add(students.document(sId))
                add(firestore.collection("reportRemarks").document(sId))
            }
            if (tId.isNotBlank()) add(teachers.document(tId))
            if (uid.isNotBlank()) {
                add(firestore.collection("profileImages").document(uid))
                add(users.document(uid))
            }
        }

        firestore.batch().apply { profileDocs.forEach { delete(it) } }.commit().await()
    }

    private suspend fun createLoginThen(
        email: String,
        password: String,
        writeProfile: suspend (uid: String) -> Unit
    ) {
        val result = creatorAuth
            .createUserWithEmailAndPassword(email.trim(), password)
            .await()
        val newUser = result.user ?: throw UserMessageException(R.string.failed_to_create_the_login_account)

        try {
            writeProfile(newUser.uid)
        } catch (e: Exception) {
            // Still signed in as the new user on the creator app, so the
            // half-created login can be removed again.
            runCatching { newUser.delete().await() }
            throw e
        } finally {
            creatorAuth.signOut()
        }
    }

    private fun userProfile(
        uid: String,
        fullName: String,
        email: String,
        role: UserRole,
        studentId: String = "",
        teacherId: String = "",
        now: Long
    ) = mapOf(
        "userId" to uid,
        "fullName" to fullName.trim(),
        "email" to email.trim().lowercase(),
        "role" to role.value,
        "studentId" to studentId,
        "teacherId" to teacherId,
        "status" to "Active",
        "createdAt" to now,
        "updatedAt" to now
    )

    private suspend fun deleteAll(query: Query) {
        query.get().await().documents.chunked(BATCH_LIMIT).forEach { chunk ->
            firestore.batch().apply { chunk.forEach { delete(it.reference) } }.commit().await()
        }
    }

    private suspend fun updateAll(query: Query, changes: Map<String, Any>) {
        val update = changes + ("updatedAt" to System.currentTimeMillis())
        query.get().await().documents.chunked(BATCH_LIMIT).forEach { chunk ->
            firestore.batch().apply { chunk.forEach { update(it.reference, update) } }.commit().await()
        }
    }

    private companion object {
        const val ACCOUNT_CREATOR_APP = "sgrms-account-creator"
        const val BATCH_LIMIT = 400
    }
}
