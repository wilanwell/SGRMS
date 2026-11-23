package com.wilanwell.sgrms.utils

import com.wilanwell.sgrms.data.model.GradeModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReportCalculatorTest {

    private val names = mapOf("bm" to "Bahasa Melayu", "math" to "Matematik")

    @Test
    fun readyReportCount_countsStudentsWhoseGradesAreAllComplete() {
        val grades = listOf(
            GradeModel(studentId = "a", status = "Complete"),
            GradeModel(studentId = "a", status = "complete"),
            GradeModel(studentId = "b", status = "Complete"),
            GradeModel(studentId = "b", status = "Pending"),
            GradeModel(studentId = "c", status = "Complete")
        )

        assertEquals(2, ReportCalculator.readyReportCount(grades))
    }

    @Test
    fun readyReportCount_isZeroWithoutGrades() {
        assertEquals(0, ReportCalculator.readyReportCount(emptyList()))
    }

    @Test
    fun buildRemarks_listsCompletedRemarksAndPendingCount() {
        val grades = listOf(
            GradeModel(subjectId = "bm", status = "Complete", remarks = "Bagus"),
            GradeModel(subjectId = "math", status = "complete", remarks = "Perlu latihan"),
            GradeModel(subjectId = "sci", status = "Pending", remarks = "ignored")
        )

        val remarks = ReportCalculator.buildRemarks(grades) { names[it] }

        assertEquals(
            "Bahasa Melayu: Bagus\n" +
                "Matematik: Perlu latihan\n" +
                "Needs update: 1 subject grade record(s) are still pending.",
            remarks
        )
    }

    @Test
    fun buildRemarks_unknownSubjectAndEmpty() {
        val grades = listOf(GradeModel(subjectId = "x", status = "Complete", remarks = "Ok"))
        assertEquals("Unknown Subject: Ok", ReportCalculator.buildRemarks(grades) { null })
        assertEquals(ReportCalculator.NO_REMARKS, ReportCalculator.buildRemarks(emptyList()) { null })
    }

    @Test
    fun isComplete_ignoresCase() {
        assertTrue(GradeModel(status = "COMPLETE").isComplete)
        assertFalse(GradeModel(status = "Pending").isComplete)
    }

    @Test
    fun todayText_format() {
        val date = Calendar.getInstance().apply { set(2026, Calendar.NOVEMBER, 21) }.time
        assertEquals("21/11/2026", ReportCalculator.todayText(date))
    }
}
