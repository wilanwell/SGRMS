package com.wilanwell.sgrms.data.repository

import com.wilanwell.sgrms.data.firebase.GradeFirebaseService
import com.wilanwell.sgrms.data.model.GradeModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeFirebaseRepository @Inject constructor(
    private val gradeFirebaseService: GradeFirebaseService
) {
    suspend fun addGrade(grade: GradeModel): GradeModel {
        return gradeFirebaseService.addGrade(grade)
    }

    suspend fun getAllGrades(): List<GradeModel> {
        return gradeFirebaseService.getAllGrades()
    }

    suspend fun getGradeById(gradeId: String): GradeModel? {
        return gradeFirebaseService.getGradeById(gradeId)
    }

    suspend fun getGradesByStudentId(studentId: String): List<GradeModel> {
        return gradeFirebaseService.getGradesByStudentId(studentId)
    }

    suspend fun getGradesBySubjectId(subjectId: String): List<GradeModel> {
        return gradeFirebaseService.getGradesBySubjectId(subjectId)
    }

    suspend fun getGradeByStudentAndSubject(
        studentId: String,
        subjectId: String
    ): GradeModel? {
        return gradeFirebaseService.getGradeByStudentAndSubject(
            studentId = studentId,
            subjectId = subjectId
        )
    }

    suspend fun updateGrade(grade: GradeModel) {
        gradeFirebaseService.updateGrade(grade)
    }

    suspend fun deleteGrade(gradeId: String) {
        gradeFirebaseService.deleteGrade(gradeId)
    }

    suspend fun getGradesPage(
        pageSize: Long = DEFAULT_PAGE_SIZE,
        subjectId: String? = null,
        cursor: PageCursor? = null
    ): Page<GradeModel> = gradeFirebaseService.getGradesPage(pageSize, subjectId, cursor)
}
