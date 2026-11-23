package com.wilanwell.sgrms.utils

import com.wilanwell.sgrms.R
import com.wilanwell.sgrms.data.model.NotificationModel

/**
 * Text for a grade notification, built on the reader's phone in the reader's
 * language from the values stored with it (subject, marks, grade).
 *
 * The teacher's phone also stores a finished title / message: notifications
 * saved before these values existed, and older app versions, still use that.
 */
object NotificationText {

    const val TYPE_GRADE_NEW = "GRADE_NEW"
    const val TYPE_GRADE_UPDATED = "GRADE_UPDATED"

    private fun hasGradeValues(n: NotificationModel) =
        n.type in setOf(TYPE_GRADE_NEW, TYPE_GRADE_UPDATED) && n.subjectName.isNotBlank() && n.gradeLetter.isNotBlank()

    fun title(n: NotificationModel, strings: Strings): String = when {
        hasGradeValues(n) && n.type == TYPE_GRADE_NEW -> strings.get(R.string.new_grade)
        hasGradeValues(n) -> strings.get(R.string.grade_updated)
        n.title.isNotBlank() -> n.title
        else -> strings.get(R.string.notification)
    }

    fun body(n: NotificationModel, strings: Strings): String = when {
        hasGradeValues(n) ->
            strings.get(R.string.your_grade_is_now, n.subjectName, GradeCalculator.formatMarks(n.marks), n.gradeLetter)
        n.message.isNotBlank() -> n.message
        else -> strings.get(R.string.notification_fallback_body)
    }
}
