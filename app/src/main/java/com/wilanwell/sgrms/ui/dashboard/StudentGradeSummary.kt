package com.wilanwell.sgrms.ui.dashboard

import com.wilanwell.sgrms.data.model.GradeModel
import com.wilanwell.sgrms.data.model.SubjectModel
import com.wilanwell.sgrms.utils.GradeCalculator
import com.wilanwell.sgrms.utils.isComplete

data class StudentDashboardGradeItem(
    val gradeId: String,
    val subjectId: String,
    val subjectName: String,
    val marks: Double,
    val gradeLetter: String
)

data class StudentGradeSummary(
    val items: List<StudentDashboardGradeItem>,
    val averageMarks: Double,
    val overallGrade: String,
    val gradedCount: Int,
    val totalCount: Int
)

/** What the student dashboard shows: completed grades only, sorted by subject name. */
fun studentGradeSummary(
    grades: List<GradeModel>,
    subjects: List<SubjectModel>
): StudentGradeSummary {
    val names = subjects.associate { it.subjectId to it.subjectName }
    val completed = grades.filter { it.isComplete }

    val items = completed
        .map { grade ->
            StudentDashboardGradeItem(
                gradeId = grade.gradeId,
                subjectId = grade.subjectId,
                subjectName = names[grade.subjectId].orEmpty(),
                marks = grade.marks,
                gradeLetter = grade.gradeLetter.ifBlank { GradeCalculator.gradeLetter(grade.marks) }
            )
        }
        .sortedWith(compareBy<StudentDashboardGradeItem> { it.subjectName }.thenBy { it.gradeId })

    val average = GradeCalculator.averageMarks(completed)

    return StudentGradeSummary(
        items = items,
        averageMarks = average,
        overallGrade = GradeCalculator.overallGrade(average, hasGrades = completed.isNotEmpty()),
        gradedCount = completed.size,
        totalCount = grades.size
    )
}
