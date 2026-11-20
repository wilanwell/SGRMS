package com.example.sgrms.data.firebase

import com.example.sgrms.data.model.GradeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeFirebaseService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
     private val gradesCollection = firestore.collection("grades")

    suspend fun addGrade(
        grade: GradeModel
    ): GradeModel {
        val existingGrade = getGradeByStudentAndSubject(
            studentId = grade.studentId,
            subjectId = grade.subjectId
        )

        if (existingGrade != null) {
            val updatedGrade = grade.copy(
                gradeId = existingGrade.gradeId,
                createdAt = existingGrade.createdAt
            )

            gradesCollection
                .document(existingGrade.gradeId)
                .set(updatedGrade)
                .await()

            return updatedGrade
        }

        val gradeDocument = gradesCollection.document()

        val newGrade = grade.copy(
            gradeId = gradeDocument.id,
            createdAt = if (grade.createdAt == 0L) {
                System.currentTimeMillis()
            } else {
                grade.createdAt
            }
        )

        gradeDocument
            .set(newGrade)
            .await()

        return newGrade
    }

    suspend fun getAllGrades(): List<GradeModel> {
        return gradesCollection
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(GradeModel::class.java)?.copy(
                    gradeId = document.id
                )
            }
    }

    suspend fun getGradeById(
        gradeId: String
    ): GradeModel? {
        return gradesCollection
            .document(gradeId)
            .get()
            .await()
            .toObject(GradeModel::class.java)
            ?.copy(
                gradeId = gradeId
            )
    }

    suspend fun getGradesByStudentId(
        studentId: String
    ): List<GradeModel> {
        return gradesCollection
            .whereEqualTo("studentId", studentId)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(GradeModel::class.java)?.copy(
                    gradeId = document.id
                )
            }
    }

    suspend fun getGradesBySubjectId(
        subjectId: String
    ): List<GradeModel> {
        return gradesCollection
            .whereEqualTo("subjectId", subjectId)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(GradeModel::class.java)?.copy(
                    gradeId = document.id
                )
            }
    }

    suspend fun getGradeByStudentAndSubject(
        studentId: String,
        subjectId: String
    ): GradeModel? {
        return gradesCollection
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("subjectId", subjectId)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.let { document ->
                document.toObject(GradeModel::class.java)?.copy(
                    gradeId = document.id
                )
            }
    }

    suspend fun updateGrade(
        grade: GradeModel
    ) {
        if (grade.gradeId.isBlank()) {
            addGrade(grade)
            return
        }

        gradesCollection
            .document(grade.gradeId)
            .set(grade)
            .await()
    }

    suspend fun deleteGrade(
        gradeId: String
    ) {
        gradesCollection
            .document(gradeId)
            .delete()
            .await()
    }
}