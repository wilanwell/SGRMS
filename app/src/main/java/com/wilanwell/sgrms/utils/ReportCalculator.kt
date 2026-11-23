package com.wilanwell.sgrms.utils

import com.wilanwell.sgrms.data.model.GradeModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Values stored in grades/{id}.status. */
object GradeStatus {
    const val COMPLETE = "Complete"
    const val PENDING = "Pending"
}

val GradeModel.isComplete: Boolean
    get() = status.equals(GradeStatus.COMPLETE, ignoreCase = true)

/** The words [ReportCalculator.buildRemarks] uses, so they can come from strings.xml. */
class RemarkTexts(
    val unknownSubject: String,
    val noRemarks: String,
    val pending: (count: Int) -> String
)

/** Report rules shared by the report list, report detail, student summary and PDF. */
object ReportCalculator {

    /**
     * One line per completed grade that has teacher remarks, plus a line
     * saying how many subjects are still pending.
     *
     * @param subjectNameOf returns the display name for a subjectId.
     */
    fun buildRemarks(
        grades: List<GradeModel>,
        texts: RemarkTexts,
        subjectNameOf: (String) -> String?
    ): String {
        val lines = grades
            .filter { it.isComplete && it.remarks.isNotBlank() }
            .map { grade ->
                val subjectName = subjectNameOf(grade.subjectId) ?: texts.unknownSubject
                "$subjectName: ${grade.remarks}"
            }
            .toMutableList()

        val pendingCount = grades.count { !it.isComplete }
        if (pendingCount > 0) {
            lines += texts.pending(pendingCount)
        }

        return lines.joinToString(separator = "\n").ifBlank { texts.noRemarks }
    }

    /**
     * Students whose report is ready: at least one grade and none pending.
     * Same rule as the "Generated" status on the report list.
     */
    fun readyReportCount(grades: List<GradeModel>): Int =
        grades.groupBy { it.studentId }.count { (_, studentGrades) -> studentGrades.all { it.isComplete } }

    /** Date printed on reports, e.g. 21/11/2026. */
    fun todayText(now: Date = Date()): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
}
