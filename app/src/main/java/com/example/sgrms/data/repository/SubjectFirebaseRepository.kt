package com.example.sgrms.data.repository

import com.example.sgrms.data.firebase.SubjectFirebaseService
import com.example.sgrms.data.model.SubjectModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectFirebaseRepository @Inject constructor(
    private val subjectFirebaseService: SubjectFirebaseService
) {
    suspend fun addSubject(
        subjectCode: String,
        subjectName: String,
        teacherId: String,
        teacherName: String,
        category: String
    ): SubjectModel {
        return subjectFirebaseService.addSubject(
            subjectCode = subjectCode,
            subjectName = subjectName,
            teacherId = teacherId,
            teacherName = teacherName,
            category = category
        )
    }

    suspend fun getAllSubjects(): List<SubjectModel> {
        return subjectFirebaseService.getAllSubjects()
    }

    suspend fun getSubjectById(subjectId: String): SubjectModel? {
        return subjectFirebaseService.getSubjectById(subjectId)
    }

    suspend fun updateSubject(subject: SubjectModel) {
        subjectFirebaseService.updateSubject(subject)
    }

    suspend fun deleteSubject(subjectId: String) {
        subjectFirebaseService.deleteSubject(subjectId)
    }
}