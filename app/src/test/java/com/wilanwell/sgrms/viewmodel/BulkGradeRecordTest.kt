package com.wilanwell.sgrms.viewmodel

import com.wilanwell.sgrms.data.model.GradeModel
import org.junit.Assert.assertEquals
import org.junit.Test

class BulkGradeRecordTest {

    private val entry = GradeBulkEntry(
        studentId = "s1",
        subjectId = "math",
        marks = 75.0,
        gradeLetter = "B",
        status = "Complete",
        subjectName = "Mathematics"
    )

    @Test
    fun newGrade_recordsTheTeacherWhoEnteredIt() {
        val grade = bulkGradeRecord(existing = null, entry = entry, teacherId = "t1", now = 1000L)

        assertEquals("s1", grade.studentId)
        assertEquals("math", grade.subjectId)
        assertEquals(75.0, grade.marks, 0.0)
        assertEquals("B", grade.gradeLetter)
        assertEquals("t1", grade.createdByTeacherId)
        assertEquals(1000L, grade.createdAt)
    }

    @Test
    fun update_keepsExistingRemarksAndCreator() {
        val existing = GradeModel(
            gradeId = "g1", studentId = "s1", subjectId = "math", marks = 40.0, gradeLetter = "D",
            status = "Pending", remarks = "Needs practice", createdByTeacherId = "t0", createdAt = 5L
        )

        val grade = bulkGradeRecord(existing = existing, entry = entry, teacherId = "t1", now = 1000L)

        assertEquals("g1", grade.gradeId)
        assertEquals(75.0, grade.marks, 0.0)
        assertEquals("B", grade.gradeLetter)
        assertEquals("Complete", grade.status)
        assertEquals("Needs practice", grade.remarks)
        assertEquals("t0", grade.createdByTeacherId)
        assertEquals(5L, grade.createdAt)
    }

    @Test
    fun update_usesNewRemarksWhenGiven_andFillsAMissingCreator() {
        val existing = GradeModel(gradeId = "g1", studentId = "s1", subjectId = "math", remarks = "old")

        val grade = bulkGradeRecord(existing = existing, entry = entry.copy(remarks = "Good work"), teacherId = "t1", now = 1000L)

        assertEquals("Good work", grade.remarks)
        assertEquals("t1", grade.createdByTeacherId)
    }
}
