package com.example.sgrms.data.firebase

import com.example.sgrms.data.model.SubjectModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectFirebaseService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
     private val subjectsCollection = firestore.collection("subjects")

    suspend fun addSubject(
        subjectCode: String,
        subjectName: String,
        teacherId: String,
        teacherName: String,
        category: String
    ): SubjectModel {
        val subjectDocument = subjectsCollection.document()

        val subject = SubjectModel(
            subjectId = subjectDocument.id,
            subjectCode = subjectCode,
            subjectName = subjectName,
            teacherId = teacherId,
            teacherName = teacherName,
            category = category,
            createdAt = System.currentTimeMillis()
        )

        subjectDocument.set(subject).await()

        return subject
    }

    suspend fun getAllSubjects(): List<SubjectModel> {
        return subjectsCollection
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(SubjectModel::class.java)?.copy(
                    subjectId = document.id
                )
            }
    }

    suspend fun getSubjectById(subjectId: String): SubjectModel? {
        return subjectsCollection
            .document(subjectId)
            .get()
            .await()
            .toObject(SubjectModel::class.java)
            ?.copy(subjectId = subjectId)
    }

    suspend fun updateSubject(subject: SubjectModel) {
        subjectsCollection
            .document(subject.subjectId)
            .set(subject)
            .await()
    }

    suspend fun deleteSubject(subjectId: String) {
        subjectsCollection
            .document(subjectId)
            .delete()
            .await()
    }
}