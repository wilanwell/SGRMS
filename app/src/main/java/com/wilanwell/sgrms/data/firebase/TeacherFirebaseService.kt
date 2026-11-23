package com.wilanwell.sgrms.data.firebase

import com.wilanwell.sgrms.utils.UserMessageException
import com.wilanwell.sgrms.R
import com.wilanwell.sgrms.data.model.TeacherModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.wilanwell.sgrms.data.repository.TeacherProfileUpdate
import com.wilanwell.sgrms.data.repository.profileImageFields
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherFirebaseService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accountService: AccountService
) {
    private val teachersCollection = firestore.collection("teachers")
    private val usersCollection = firestore.collection("users")

    /**
     * Creates the login account and the teacher profile on the server
     * (AccountService, on a second FirebaseApp), so the admin stays signed in.
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
        val teacherId = accountService.createTeacherAccount(
            staffId = staffId,
            teacherName = teacherName,
            email = email,
            phoneNumber = phoneNumber,
            department = department,
            position = position,
            password = password
        )

        return getTeacherById(teacherId)
            ?: throw UserMessageException(R.string.teacher_profile_not_found_after_creation)
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
     * teacher's subjects and classes (AccountService.deleteAccount).
     */
    suspend fun deleteTeacher(
        teacherId: String
    ) {
        accountService.deleteAccount(teacherId = teacherId)
    }

    /** Finds the teacher profile linked to a login account. */
    suspend fun getTeacherByUserId(
        userId: String
    ): TeacherModel? {
        val document = teachersCollection
            .whereEqualTo("userId", userId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?: return null

        return document.toObject(TeacherModel::class.java)?.copy(teacherId = document.id)
    }

    suspend fun updateOwnProfile(
        teacherId: String,
        update: TeacherProfileUpdate
    ) {
        teachersCollection
            .document(teacherId)
            .update(
                mapOf(
                    "phoneNumber" to update.phoneNumber,
                    "department" to update.department,
                    "position" to update.position,
                    "officeLocation" to update.officeLocation,
                    "qualification" to update.qualification,
                    "specialization" to update.specialization,
                    "address" to update.address,
                    "profileNote" to update.profileNote,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    /** Pass an empty [imageUrl] to remove the photo. */
    suspend fun updateProfileImage(
        teacherId: String,
        imageUrl: String
    ) {
        teachersCollection
            .document(teacherId)
            .update(profileImageFields(imageUrl))
            .await()
    }
}
