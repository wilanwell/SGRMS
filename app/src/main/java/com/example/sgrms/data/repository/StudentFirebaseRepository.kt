package com.example.sgrms.data.repository

import com.example.sgrms.data.firebase.StudentFirebaseService
import com.example.sgrms.data.model.StudentModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentFirebaseRepository @Inject constructor(
    private val studentFirebaseService: StudentFirebaseService
) {
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
        return studentFirebaseService.addStudentWithAccount(
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
    }

    suspend fun getAllStudents(): List<StudentModel> {
        return studentFirebaseService.getAllStudents()
    }

    suspend fun getStudentById(
        studentId: String
    ): StudentModel? {
        return studentFirebaseService.getStudentById(studentId)
    }

    suspend fun updateStudent(
        student: StudentModel
    ) {
        studentFirebaseService.updateStudent(student)
    }

    suspend fun deleteStudent(
        studentId: String
    ) {
        studentFirebaseService.deleteStudent(studentId)
    }

    suspend fun updateOwnProfile(studentId: String, update: StudentProfileUpdate) =
        studentFirebaseService.updateOwnProfile(studentId, update)

    suspend fun updateProfileImage(studentId: String, imageUrl: String) =
        studentFirebaseService.updateProfileImage(studentId, imageUrl)

    suspend fun saveFcmToken(studentId: String, token: String) =
        studentFirebaseService.saveFcmToken(studentId, token)
}
