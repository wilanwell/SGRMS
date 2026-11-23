package com.wilanwell.sgrms.utils

import com.wilanwell.sgrms.R
import com.wilanwell.sgrms.data.model.GradeModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GradeCalculatorTest {

    @Test
    fun gradeLetter_boundaries() {
        assertEquals("A+", GradeCalculator.gradeLetter(100.0))
        assertEquals("A+", GradeCalculator.gradeLetter(90.0))
        assertEquals("A", GradeCalculator.gradeLetter(89.99))
        assertEquals("A", GradeCalculator.gradeLetter(80.0))
        assertEquals("B", GradeCalculator.gradeLetter(70.0))
        assertEquals("C", GradeCalculator.gradeLetter(60.0))
        assertEquals("D", GradeCalculator.gradeLetter(50.0))
        assertEquals("E", GradeCalculator.gradeLetter(49.99))
        assertEquals("E", GradeCalculator.gradeLetter(0.0))
    }

    @Test
    fun gradeLetter_fromText() {
        assertEquals("A+", GradeCalculator.gradeLetter(" 95 "))
        assertEquals("", GradeCalculator.gradeLetter(""))
        assertEquals("", GradeCalculator.gradeLetter("abc"))
    }

    @Test
    fun overallGrade_withoutGradesIsDash() {
        assertEquals("-", GradeCalculator.overallGrade(0.0))
        assertEquals("-", GradeCalculator.overallGrade(75.0, hasGrades = false))
        assertEquals("E", GradeCalculator.overallGrade(0.0, hasGrades = true))
        assertEquals("A+", GradeCalculator.overallGrade(92.0))
    }

    @Test
    fun performanceStatus_labels() {
        assertEquals(R.string.performance_excellent, GradeCalculator.performanceStatus(80.0))
        assertEquals(R.string.performance_good, GradeCalculator.performanceStatus(70.0))
        assertEquals(R.string.performance_satisfactory, GradeCalculator.performanceStatus(60.0))
        assertEquals(R.string.performance_needs_improvement, GradeCalculator.performanceStatus(50.0))
        assertEquals(R.string.performance_at_risk, GradeCalculator.performanceStatus(10.0))
        assertEquals(R.string.no_grade_dash, GradeCalculator.performanceStatus(0.0))
    }

    @Test
    fun passAndValidity() {
        assertTrue(GradeCalculator.isPass(50.0))
        assertFalse(GradeCalculator.isPass(49.9))
        assertTrue(GradeCalculator.isValidMarks(0.0))
        assertTrue(GradeCalculator.isValidMarks(100.0))
        assertFalse(GradeCalculator.isValidMarks(100.5))
        assertFalse(GradeCalculator.isValidMarks(-1.0))
    }

    @Test
    fun averageMarks() {
        assertEquals(0.0, GradeCalculator.averageMarks(emptyList()), 0.0)
        val grades = listOf(GradeModel(marks = 60.0), GradeModel(marks = 90.0))
        assertEquals(75.0, GradeCalculator.averageMarks(grades), 0.0001)
    }

    @Test
    fun formatMarks_dropsATrailingZeroOnly() {
        assertEquals("70", GradeCalculator.formatMarks(70.0))
        assertEquals("72.5", GradeCalculator.formatMarks(72.5))
        assertEquals("100", GradeCalculator.formatMarks(100.0))
    }
}
