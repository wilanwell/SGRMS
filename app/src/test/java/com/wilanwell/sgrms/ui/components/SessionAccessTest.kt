package com.wilanwell.sgrms.ui.components

import com.wilanwell.sgrms.data.model.UserRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Must agree with isMyTeacherId / teachesSubject in firestore.rules. */
class SessionAccessTest {

    @Test
    fun admin_canWriteEverySubject() {
        val admin = SessionAccess(UserRole.ADMIN, teacherId = "")
        assertTrue(admin.canWriteSubject("t1"))
        assertTrue(admin.canWriteSubject(""))
    }

    @Test
    fun teacher_canWriteOnlyTheirOwnSubjects() {
        val teacher = SessionAccess(UserRole.TEACHER, teacherId = "t1")
        assertTrue(teacher.canWriteSubject("t1"))
        assertFalse(teacher.canWriteSubject("t2"))
        assertFalse(teacher.canWriteSubject(""))
    }

    @Test
    fun unlinkedTeacher_andStudent_canWriteNothing() {
        assertFalse(SessionAccess(UserRole.TEACHER, teacherId = "").canWriteSubject(""))
        assertFalse(SessionAccess(UserRole.STUDENT, teacherId = "").canWriteSubject("t1"))
        assertFalse(SessionAccess().canWriteSubject("t1"))
    }
}
