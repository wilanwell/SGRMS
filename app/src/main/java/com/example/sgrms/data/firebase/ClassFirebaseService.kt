package com.example.sgrms.data.firebase

import com.example.sgrms.data.model.ClassModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ClassFirebaseService {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val classesCollection = firestore.collection("classes")

    suspend fun addClass(
        className: String,
        academicYear: String,
        classTeacherId: String,
        classTeacherName: String,
        description: String
    ): ClassModel {
        val classDocument = classesCollection.document()

        val schoolClass = ClassModel(
            classId = classDocument.id,
            className = className,
            academicYear = academicYear,
            classTeacherId = classTeacherId,
            classTeacherName = classTeacherName,
            description = description,
            createdAt = System.currentTimeMillis()
        )

        classDocument
            .set(schoolClass)
            .await()

        return schoolClass
    }

    suspend fun getAllClasses(): List<ClassModel> {
        return classesCollection
            .get()
            .await()
            .toObjects(ClassModel::class.java)
    }

    suspend fun getClassById(
        classId: String
    ): ClassModel? {
        return classesCollection
            .document(classId)
            .get()
            .await()
            .toObject(ClassModel::class.java)
    }

    suspend fun updateClass(
        schoolClass: ClassModel
    ) {
        classesCollection
            .document(schoolClass.classId)
            .set(schoolClass)
            .await()
    }

    suspend fun deleteClass(
        classId: String
    ) {
        classesCollection
            .document(classId)
            .delete()
            .await()
    }
}