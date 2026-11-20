package com.example.sgrms.data.repository

import com.example.sgrms.data.model.ClassModel
import com.example.sgrms.data.model.SubjectModel
import com.example.sgrms.data.model.TeacherModel
import com.example.sgrms.data.model.UserRole
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class AdminSummary(
    val totalAccounts: Int = 0,
    val adminAccounts: Int = 0,
    val teacherAccounts: Int = 0,
    val studentAccounts: Int = 0,
    val teachers: Int = 0,
    val students: Int = 0,
    val subjects: Int = 0,
    val classes: Int = 0
)

data class SubjectAverage(
    val subjectName: String,
    val averageMarks: Double
)

data class TeacherDashboardSummary(
    val students: Int = 0,
    val subjects: Int = 0,
    val grades: Int = 0,
    val reports: Int = 0,
    val subjectAverages: List<SubjectAverage> = emptyList()
)

data class TeacherAssignments(
    val classes: List<ClassModel> = emptyList(),
    val subjects: List<SubjectModel> = emptyList(),
    val studentCount: Int = 0,
    val reportCount: Int = 0
)

/**
 * Dashboard figures computed with Firestore aggregation queries
 * (`count()` / `average()`), so the app downloads a few numbers instead of
 * every document in each collection.
 */
@Singleton
class DashboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getAdminSummary(): AdminSummary = coroutineScope {
        val users = firestore.collection("users")

        val total = async { count(users) }
        val admins = async { count(users.whereEqualTo("role", UserRole.ADMIN.value)) }
        val teacherAccounts = async { count(users.whereEqualTo("role", UserRole.TEACHER.value)) }
        val studentAccounts = async { count(users.whereEqualTo("role", UserRole.STUDENT.value)) }
        val teachers = async { count(firestore.collection("teachers")) }
        val students = async { count(firestore.collection("students")) }
        val subjects = async { count(firestore.collection("subjects")) }
        val classes = async { count(firestore.collection("classes")) }

        AdminSummary(
            totalAccounts = total.await(),
            adminAccounts = admins.await(),
            teacherAccounts = teacherAccounts.await(),
            studentAccounts = studentAccounts.await(),
            teachers = teachers.await(),
            students = students.await(),
            subjects = subjects.await(),
            classes = classes.await()
        )
    }

    suspend fun getTeacherDashboardSummary(): TeacherDashboardSummary = coroutineScope {
        val grades = firestore.collection("grades")

        val students = async { count(firestore.collection("students")) }
        val gradeCount = async { count(grades) }
        val reports = async { count(firestore.collection("reports")) }

        val subjectList = firestore.collection("subjects")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(SubjectModel::class.java)?.copy(subjectId = it.id) }

        val averages = subjectList.map { subject ->
            async {
                val average = grades
                    .whereEqualTo("subjectId", subject.subjectId)
                    .aggregate(AggregateField.average("marks"))
                    .get(AggregateSource.SERVER)
                    .await()
                    .get(AggregateField.average("marks"))
                    ?: 0.0

                SubjectAverage(subjectName = subject.subjectName, averageMarks = average)
            }
        }.awaitAll()

        TeacherDashboardSummary(
            students = students.await(),
            subjects = subjectList.size,
            grades = gradeCount.await(),
            reports = reports.await(),
            subjectAverages = averages.sortedByDescending { it.averageMarks }
        )
    }

    /** Classes and subjects assigned to [teacher], and the students in those classes. */
    suspend fun getTeacherAssignments(teacher: TeacherModel): TeacherAssignments = coroutineScope {
        val classesQuery = async {
            firestore.collection("classes")
                .whereEqualTo("classTeacherId", teacher.teacherId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(ClassModel::class.java)?.copy(classId = it.id) }
        }

        val subjectsQuery = async {
            firestore.collection("subjects")
                .whereEqualTo("teacherId", teacher.teacherId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(SubjectModel::class.java)?.copy(subjectId = it.id) }
        }

        val classes = classesQuery.await()
        val classIds = classes.map { it.classId }

        // whereIn accepts at most 30 values per query.
        val studentIds = classIds.chunked(30).map { chunk ->
            async {
                firestore.collection("students")
                    .whereIn("classId", chunk)
                    .get()
                    .await()
                    .documents
                    .map { it.id }
            }
        }.awaitAll().flatten()

        val reportCount = studentIds.chunked(30).map { chunk ->
            async { count(firestore.collection("reports").whereIn("studentId", chunk)) }
        }.awaitAll().sum()

        TeacherAssignments(
            classes = classes.sortedBy { it.className },
            subjects = subjectsQuery.await().sortedBy { it.subjectName },
            studentCount = studentIds.size,
            reportCount = reportCount
        )
    }

    private suspend fun count(query: Query): Int {
        return query
            .count()
            .get(AggregateSource.SERVER)
            .await()
            .count
            .toInt()
    }
}
