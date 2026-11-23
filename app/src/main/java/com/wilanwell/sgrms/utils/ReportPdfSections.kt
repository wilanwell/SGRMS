package com.wilanwell.sgrms.utils

import android.graphics.Paint
import android.graphics.Typeface
import com.wilanwell.sgrms.R
import com.wilanwell.sgrms.data.model.StudentModel

// Report body: student card, summary, grades table, remarks and signatures.

internal fun PdfContext.drawStudentInformationCard(
    canvas: android.graphics.Canvas,
    paints: PdfPaints,
    y: Float,
    student: StudentModel?,
    report: ReportPdfData
): Float {
    drawSectionTitle(
        canvas = canvas,
        paints = paints,
        title = strings.get(R.string.student_information),
        y = y
    )

    val top = y + 14f
    val bottom = top + 138f

    drawRoundedRect(
        canvas = canvas,
        left = LEFT,
        top = top,
        right = RIGHT,
        bottom = bottom,
        radius = 12f,
        paint = paints.cardBackground
    )

    canvas.drawText(
        strings.get(R.string.student_name_title),
        58f,
        top + 30f,
        paints.label
    )

    canvas.drawText(
        student?.studentName?.ifBlank { "-" } ?: strings.get(R.string.unknown_student_title),
        58f,
        top + 52f,
        paints.valueBold
    )

    canvas.drawText(
        strings.get(R.string.student_id),
        58f,
        top + 80f,
        paints.label
    )

    canvas.drawText(
        student?.studentCode?.ifBlank { "-" } ?: "-",
        58f,
        top + 100f,
        paints.value
    )

    canvas.drawText(
        strings.get(R.string.class_label),
        300f,
        top + 30f,
        paints.label
    )

    canvas.drawText(
        student?.className?.ifBlank { "-" } ?: strings.get(R.string.unknown_class),
        300f,
        top + 52f,
        paints.valueBold
    )

    canvas.drawText(
        strings.get(R.string.report_date_label),
        300f,
        top + 80f,
        paints.label
    )

    canvas.drawText(
        report.generatedDate.ifBlank { "-" },
        300f,
        top + 100f,
        paints.value
    )

    canvas.drawText(
        strings.get(R.string.class_teacher_title),
        58f,
        top + 126f,
        paints.label
    )

    canvas.drawText(
        report.classTeacherName.ifBlank { strings.get(R.string.not_assigned) },
        145f,
        top + 126f,
        paints.valueBold
    )

    return bottom
}

internal fun PdfContext.drawAcademicSummary(
    canvas: android.graphics.Canvas,
    paints: PdfPaints,
    y: Float,
    report: ReportPdfData,
    totalSubjects: Int
): Float {
    drawSectionTitle(
        canvas = canvas,
        paints = paints,
        title = strings.get(R.string.academic_summary),
        y = y
    )

    val top = y + 16f
    val boxWidth = 118f
    val gap = 11f
    val boxHeight = 72f

    val items = listOf(
        SummaryItem(strings.get(R.string.subjects), totalSubjects.toString(), LIGHT_BLUE),
        SummaryItem(strings.get(R.string.total_marks_title), "%.2f".format(report.totalMarks), LIGHT_GREY),
        SummaryItem(strings.get(R.string.average), "%.2f%%".format(report.averageMarks), LIGHT_BLUE),
        SummaryItem(strings.get(R.string.grade), report.overallGrade.ifBlank { "-" }, LIGHT_GREY)
    )

    items.forEachIndexed { index, item ->
        val left = LEFT + index * (boxWidth + gap)
        val right = left + boxWidth

        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = item.color
            style = Paint.Style.FILL
        }

        drawRoundedRect(
            canvas = canvas,
            left = left,
            top = top,
            right = right,
            bottom = top + boxHeight,
            radius = 10f,
            paint = backgroundPaint
        )

        canvas.drawText(
            item.label,
            left + 12f,
            top + 27f,
            paints.label
        )

        canvas.drawText(
            item.value,
            left + 12f,
            top + 54f,
            paints.summaryValue
        )
    }

    val status = strings.get(GradeCalculator.performanceStatus(report.averageMarks))
    val statusColor = getStatusColor(report.averageMarks)

    val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = statusColor
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    canvas.drawText(
        strings.get(R.string.performance_status, status),
        LEFT,
        top + boxHeight + 26f,
        statusPaint
    )

    return top + boxHeight + 34f
}

/*
    Updated column guide:

    No.      x = 40
    Subject  x = 92
    Code     x = 310
    Marks    x = 380
    Grade    x = 440
    Status   x = 490
*/
internal fun PdfContext.drawTableHeader(
    canvas: android.graphics.Canvas,
    paints: PdfPaints,
    y: Float
): Float {
    val headerY = y + 18f
    val lineY = y + 31f

    canvas.drawText(
        "No.",
        LEFT,
        headerY,
        paints.cleanTableHeader
    )

    canvas.drawText(
        strings.get(R.string.subject),
        92f,
        headerY,
        paints.cleanTableHeader
    )

    canvas.drawText(
        strings.get(R.string.code_header),
        310f,
        headerY,
        paints.cleanTableHeader
    )

    canvas.drawText(
        strings.get(R.string.marks),
        380f,
        headerY,
        paints.cleanTableHeader
    )

    canvas.drawText(
        strings.get(R.string.grade),
        440f,
        headerY,
        paints.cleanTableHeader
    )

    canvas.drawText(
        strings.get(R.string.status),
        490f,
        headerY,
        paints.cleanTableHeader
    )

    canvas.drawLine(
        LEFT,
        lineY,
        RIGHT,
        lineY,
        paints.cleanTableLine
    )

    return lineY + 28f
}

internal fun PdfContext.drawTableRow(
    canvas: android.graphics.Canvas,
    paints: PdfPaints,
    y: Float,
    index: Int,
    item: ReportPdfGradeItem
): Float {
    canvas.drawText(
        index.toString(),
        LEFT,
        y,
        paints.cleanTableText
    )

    canvas.drawText(
        item.subjectName.ifBlank { "-" }.take(22),
        92f,
        y,
        paints.cleanTableText
    )

    canvas.drawText(
        item.subjectCode.ifBlank { "-" }.take(9),
        310f,
        y,
        paints.cleanTableText
    )

    canvas.drawText(
        "%.2f".format(item.grade.marks),
        380f,
        y,
        paints.cleanTableText
    )

    canvas.drawText(
        item.grade.gradeLetter.ifBlank { "-" }.take(4),
        440f,
        y,
        paints.cleanTableText
    )

    canvas.drawText(
        item.grade.status.ifBlank { "-" }.take(10),
        490f,
        y,
        paints.cleanTableText
    )

    return y + 25f
}

internal fun PdfContext.drawRemarksSection(
    canvas: android.graphics.Canvas,
    paints: PdfPaints,
    y: Float,
    remarks: String
): Float {
    drawSectionTitle(
        canvas = canvas,
        paints = paints,
        title = strings.get(R.string.report_remarks),
        y = y
    )

    val top = y + 16f
    val bottom = top + 86f

    drawRoundedRect(
        canvas = canvas,
        left = LEFT,
        top = top,
        right = RIGHT,
        bottom = bottom,
        radius = 10f,
        paint = paints.cardBackground
    )

    val remarksText = remarks.ifBlank {
        strings.get(R.string.no_specific_remarks_from_grade_records)
    }

    val lines = splitTextByWidth(
        text = remarksText,
        paint = paints.body,
        maxWidth = 485f
    ).take(4)

    var textY = top + 28f

    lines.forEach { line ->
        canvas.drawText(
            line,
            58f,
            textY,
            paints.body
        )

        textY += 18f
    }

    return bottom
}

internal fun PdfContext.drawSignatureSection(
    canvas: android.graphics.Canvas,
    paints: PdfPaints,
    y: Float
) {
    val top = y
    val lineY = top + 58f

    canvas.drawText(
        strings.get(R.string.prepared_by),
        70f,
        top + 18f,
        paints.label
    )

    canvas.drawLine(
        60f,
        lineY,
        230f,
        lineY,
        paints.signatureLine
    )

    canvas.drawText(
        strings.get(R.string.teacher_academic_officer),
        72f,
        lineY + 20f,
        paints.smallGrey
    )

    canvas.drawText(
        strings.get(R.string.verified_by),
        360f,
        top + 18f,
        paints.label
    )

    canvas.drawLine(
        350f,
        lineY,
        520f,
        lineY,
        paints.signatureLine
    )

    canvas.drawText(
        strings.get(R.string.school_administrator),
        380f,
        lineY + 20f,
        paints.smallGrey
    )
}

internal fun PdfContext.getStatusColor(
    averageMarks: Double
): Int {
    return when {
        averageMarks >= 70 -> SUCCESS_GREEN
        averageMarks >= 50 -> WARNING_ORANGE
        averageMarks > 0 -> DANGER_RED
        else -> TEXT_GREY
    }
}
