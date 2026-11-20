package com.example.sgrms.data.repository

import com.example.sgrms.data.firebase.TeacherFirebaseService
import com.example.sgrms.data.model.TeacherModel

class TeacherFirebaseRepository(
    private val teacherFirebaseService: TeacherFirebaseService
) {
    suspend fun addTeacherWithAccount(
        staffId: String,
        teacherName: String,
        email: String,
        phoneNumber: String,
        department: String,
        position: String,
        password: String
    ): TeacherModel {
        return teacherFirebaseService.addTeacherWithAccount(
            staffId = staffId,
            teacherName = teacherName,
            email = email,
            phoneNumber = phoneNumber,
            department = department,
            position = position,
            password = password
        )
    }

    suspend fun getAllTeachers(): List<TeacherModel> {
        return teacherFirebaseService.getAllTeachers()
    }

    suspend fun getTeacherById(
        teacherId: String
    ): TeacherModel? {
        return teacherFirebaseService.getTeacherById(
            teacherId = teacherId
        )
    }

    suspend fun updateTeacher(
        teacher: TeacherModel
    ) {
        teacherFirebaseService.updateTeacher(
            teacher = teacher
        )
    }

    suspend fun deleteTeacher(
        teacherId: String
    ) {
        teacherFirebaseService.deleteTeacher(
            teacherId = teacherId
        )
    }
}