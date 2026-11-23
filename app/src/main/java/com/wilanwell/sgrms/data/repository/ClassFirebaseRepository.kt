package com.wilanwell.sgrms.data.repository

import com.wilanwell.sgrms.data.firebase.ClassFirebaseService
import com.wilanwell.sgrms.data.model.ClassModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassFirebaseRepository @Inject constructor(
    private val classFirebaseService: ClassFirebaseService
) {
    suspend fun addClass(
        className: String,
        academicYear: String,
        classTeacherId: String,
        classTeacherName: String,
        description: String
    ): ClassModel {
        return classFirebaseService.addClass(
            className = className,
            academicYear = academicYear,
            classTeacherId = classTeacherId,
            classTeacherName = classTeacherName,
            description = description
        )
    }

    suspend fun getAllClasses(): List<ClassModel> {
        return classFirebaseService.getAllClasses()
    }

    suspend fun getClassById(
        classId: String
    ): ClassModel? {
        return classFirebaseService.getClassById(classId)
    }

    suspend fun updateClass(
        schoolClass: ClassModel
    ) {
        classFirebaseService.updateClass(schoolClass)
    }

    suspend fun deleteClass(
        classId: String
    ) {
        classFirebaseService.deleteClass(classId)
    }
}