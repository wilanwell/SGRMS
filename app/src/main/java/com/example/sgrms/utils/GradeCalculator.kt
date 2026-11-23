package com.example.sgrms.utils

import com.example.sgrms.data.model.GradeModel

/**
 * Performance band for a mark or an average, used for colours and labels.
 */
enum class PerformanceLevel(val label: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    SATISFACTORY("Satisfactory"),
    NEEDS_IMPROVEMENT("Needs Improvement"),
    AT_RISK("At Risk")
}

/**
 * Single source of truth for SGRMS grading rules.
 *
 * Every screen, the PDF report and the dashboards must use this object
 * instead of their own `when (marks)` blocks, so a grade is always the same
 * wherever it is shown.
 */
object GradeCalculator {

    const val MIN_MARKS = 0.0
    const val MAX_MARKS = 100.0
    const val PASS_MARKS = 50.0

    /** Shown when there is nothing to grade yet. */
    const val NO_GRADE = "-"

    /** Marks for display: 70.0 -> "70", 72.5 -> "72.5". */
    fun formatMarks(marks: Double): String =
        if (marks % 1.0 == 0.0) marks.toLong().toString() else marks.toString()

    /** Grade letter for a mark between 0 and 100. */
    fun gradeLetter(marks: Double): String {
        return when {
            marks >= 90 -> "A+"
            marks >= 80 -> "A"
            marks >= 70 -> "B"
            marks >= 60 -> "C"
            marks >= 50 -> "D"
            else -> "E"
        }
    }

    /** Grade letter for user input; blank when the text is not a number. */
    fun gradeLetter(marksText: String): String {
        val marks = marksText.trim().toDoubleOrNull() ?: return ""
        return gradeLetter(marks)
    }

    /** Overall grade for an average; [NO_GRADE] when there are no marks. */
    fun overallGrade(
        averageMarks: Double,
        hasGrades: Boolean = averageMarks > 0
    ): String {
        return if (hasGrades) gradeLetter(averageMarks) else NO_GRADE
    }

    fun isPass(marks: Double): Boolean = marks >= PASS_MARKS

    fun isValidMarks(marks: Double): Boolean = marks in MIN_MARKS..MAX_MARKS

    fun performanceLevel(marks: Double): PerformanceLevel {
        return when {
            marks >= 80 -> PerformanceLevel.EXCELLENT
            marks >= 70 -> PerformanceLevel.GOOD
            marks >= 60 -> PerformanceLevel.SATISFACTORY
            marks >= 50 -> PerformanceLevel.NEEDS_IMPROVEMENT
            else -> PerformanceLevel.AT_RISK
        }
    }

    /** Performance label for an average; [NO_GRADE] when there are no marks. */
    fun performanceStatus(
        averageMarks: Double,
        hasGrades: Boolean = averageMarks > 0
    ): String {
        return if (hasGrades) performanceLevel(averageMarks).label else NO_GRADE
    }

    fun averageMarks(grades: List<GradeModel>): Double {
        if (grades.isEmpty()) return 0.0
        return grades.sumOf { it.marks } / grades.size
    }
}
