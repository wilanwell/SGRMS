package com.wilanwell.sgrms.utils

import com.wilanwell.sgrms.R
import com.wilanwell.sgrms.data.model.NotificationModel
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationTextTest {

    /** Shows which resource and arguments were used, so a test can check them without Android. */
    private val strings = object : Strings {
        override fun get(id: Int, vararg args: Any): String = "$id" + args.joinToString("") { "|$it" }
    }

    @Test
    fun newGrade_isBuiltFromItsValues_inTheReadersLanguage() {
        val n = NotificationModel(
            type = NotificationText.TYPE_GRADE_NEW, subjectName = "Biology", marks = 70.0, gradeLetter = "B",
            title = "Gred baharu", message = "stored text in the teacher's language"
        )
        assertEquals("${R.string.new_grade}", NotificationText.title(n, strings))
        assertEquals("${R.string.your_grade_is_now}|Biology|70|B", NotificationText.body(n, strings))
    }

    @Test
    fun updatedGrade_usesTheUpdatedTitle() {
        val n = NotificationModel(type = NotificationText.TYPE_GRADE_UPDATED, subjectName = "Math", marks = 72.5, gradeLetter = "A")
        assertEquals("${R.string.grade_updated}", NotificationText.title(n, strings))
        assertEquals("${R.string.your_grade_is_now}|Math|72.5|A", NotificationText.body(n, strings))
    }

    @Test
    fun olderNotification_withoutValues_keepsItsStoredText() {
        val n = NotificationModel(type = NotificationText.TYPE_GRADE_UPDATED, title = "Grade updated", message = "Your BM grade changed.")
        assertEquals("Grade updated", NotificationText.title(n, strings))
        assertEquals("Your BM grade changed.", NotificationText.body(n, strings))
    }

    @Test
    fun emptyNotification_fallsBackToGenericText() {
        val n = NotificationModel()
        assertEquals("${R.string.notification}", NotificationText.title(n, strings))
        assertEquals("${R.string.notification_fallback_body}", NotificationText.body(n, strings))
    }
}
