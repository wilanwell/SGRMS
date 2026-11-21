package com.example.sgrms.ui.dashboard

import com.example.sgrms.data.model.GradeModel
import com.example.sgrms.data.model.SubjectModel
import org.junit.Assert.assertEquals
import org.junit.Test

class StudentGradeSummaryTest {

    private val subjects = listOf(
        SubjectModel(subjectId = "m", subjectName = "Mathematics"),
        SubjectModel(subjectId = "b", subjectName = "Biology")
    )

    @Test
    fun averagesOnlyCompletedGradesAndCountsGraded() {
        val grades = listOf(
            GradeModel(gradeId = "1", subjectId = "m", marks = 92.0, gradeLetter = "A+", status = "Complete"),
            GradeModel(gradeId = "2", subjectId = "b", marks = 68.0, gradeLetter = "C", status = "complete"),
            GradeModel(gradeId = "3", subjectId = "x", marks = 0.0, status = "Pending")
        )

        val summary = studentGradeSummary(grades, subjects)

        assertEquals(80.0, summary.averageMarks, 0.0001)
        assertEquals("A", summary.overallGrade)
        assertEquals(2, summary.gradedCount)
        assertEquals(3, summary.totalCount)
        assertEquals(listOf("Biology", "Mathematics"), summary.items.map { it.subjectName })
    }

    @Test
    fun unknownSubjectAndNoGrades() {
        val summary = studentGradeSummary(
            listOf(GradeModel(gradeId = "1", subjectId = "zz", marks = 50.0, status = "Complete")),
            subjects
        )
        assertEquals("Unknown Subject", summary.items.single().subjectName)

        val empty = studentGradeSummary(emptyList(), subjects)
        assertEquals(0.0, empty.averageMarks, 0.0)
        assertEquals("-", empty.overallGrade)
        assertEquals(0, empty.gradedCount)
    }
}
