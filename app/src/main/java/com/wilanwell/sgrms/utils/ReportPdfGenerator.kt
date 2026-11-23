package com.wilanwell.sgrms.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.wilanwell.sgrms.R
import com.wilanwell.sgrms.data.model.GradeModel
import com.wilanwell.sgrms.data.model.StudentModel
import java.io.File
import java.io.FileOutputStream

/*
    PDF COORDINATE SYSTEM GUIDE

    x = horizontal position
        Smaller x = more left
        Bigger x = more right

    y = vertical position
        Smaller y = higher
        Bigger y = lower
*/

data class ReportPdfData(
    val reportId: String = "",
    val studentId: String = "",
    val totalMarks: Double = 0.0,
    val averageMarks: Double = 0.0,
    val overallGrade: String = "",
    val remarks: String = "",
    val generatedDate: String = "",
    val classTeacherName: String = ""
)

data class ReportPdfGradeItem(
    val grade: GradeModel,
    val subjectName: String,
    val subjectCode: String
)

object ReportPdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    private const val LEFT = 40f
    private const val RIGHT = 555f

    private const val PRIMARY_BLUE = 0xFF4F46E5.toInt() // brand indigo
    private const val LIGHT_BLUE = 0xFFEEF2FF.toInt() // brand indigo, light
    private const val LIGHT_GREY = 0xFFF4F6F8.toInt()
    private const val BORDER_GREY = 0xFFBDBDBD.toInt()
    private const val TEXT_DARK = 0xFF212121.toInt()
    private const val TEXT_GREY = 0xFF616161.toInt()
    private const val SUCCESS_GREEN = 0xFF2E7D32.toInt()
    private const val WARNING_ORANGE = 0xFFEF6C00.toInt()
    private const val DANGER_RED = 0xFFC62828.toInt()

    fun saveReportAsPdf(
        context: Context,
        report: ReportPdfData,
        student: StudentModel?,
        gradeItems: List<ReportPdfGradeItem>
    ) {
        val pdfDocument = PdfDocument()

        try {
            var pageNumber = 1
            var page = startPage(pdfDocument, pageNumber)
            var canvas = page.canvas

            val paints = PdfPaints()

            drawPageHeader(
                context = context,
                canvas = canvas,
                paints = paints,
                report = report
            )

            var y = 168f

            y = drawStudentInformationCard(
                canvas = canvas,
                paints = paints,
                y = y,
                student = student,
                report = report
            )

            y += 18f

            y = drawAcademicSummary(
                canvas = canvas,
                paints = paints,
                y = y,
                report = report,
                totalSubjects = gradeItems.size
            )

            y += 22f

            drawSectionTitle(
                canvas = canvas,
                paints = paints,
                title = "Subject Performance",
                y = y
            )

            y += 14f

            y = drawTableHeader(
                canvas = canvas,
                paints = paints,
                y = y
            )

            if (gradeItems.isEmpty()) {
                canvas.drawText(
                    "No completed subject grade records found.",
                    LEFT,
                    y,
                    paints.bodyGrey
                )

                y += 26f
            } else {
                gradeItems.forEachIndexed { index, item ->

                    if (y > 705f) {
                        drawFooter(
                            canvas = canvas,
                            paints = paints,
                            pageNumber = pageNumber
                        )

                        pdfDocument.finishPage(page)

                        pageNumber += 1
                        page = startPage(pdfDocument, pageNumber)
                        canvas = page.canvas

                        drawContinuationHeader(
                            canvas = canvas,
                            paints = paints,
                            report = report,
                            pageNumber = pageNumber
                        )

                        y = 112f

                        drawSectionTitle(
                            canvas = canvas,
                            paints = paints,
                            title = "Subject Performance Continued",
                            y = y
                        )

                        y += 14f

                        y = drawTableHeader(
                            canvas = canvas,
                            paints = paints,
                            y = y
                        )
                    }

                    y = drawTableRow(
                        canvas = canvas,
                        paints = paints,
                        y = y,
                        index = index + 1,
                        item = item
                    )
                }
            }

            y += 24f

            if (y > 625f) {
                drawFooter(
                    canvas = canvas,
                    paints = paints,
                    pageNumber = pageNumber
                )

                pdfDocument.finishPage(page)

                pageNumber += 1
                page = startPage(pdfDocument, pageNumber)
                canvas = page.canvas

                drawContinuationHeader(
                    canvas = canvas,
                    paints = paints,
                    report = report,
                    pageNumber = pageNumber
                )

                y = 120f
            }

            y = drawRemarksSection(
                canvas = canvas,
                paints = paints,
                y = y,
                remarks = report.remarks
            )

            y += 26f

            if (y > 680f) {
                drawFooter(
                    canvas = canvas,
                    paints = paints,
                    pageNumber = pageNumber
                )

                pdfDocument.finishPage(page)

                pageNumber += 1
                page = startPage(pdfDocument, pageNumber)
                canvas = page.canvas

                drawContinuationHeader(
                    canvas = canvas,
                    paints = paints,
                    report = report,
                    pageNumber = pageNumber
                )

                y = 125f
            }

            drawSignatureSection(
                canvas = canvas,
                paints = paints,
                y = y
            )

            drawFooter(
                canvas = canvas,
                paints = paints,
                pageNumber = pageNumber
            )

            pdfDocument.finishPage(page)

            savePdfToDownloads(
                context = context,
                pdfDocument = pdfDocument,
                fileName = createFileName(
                    studentName = student?.studentName ?: "student",
                    reportId = report.reportId
                )
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to save PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun startPage(
        pdfDocument: PdfDocument,
        pageNumber: Int
    ): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo
            .Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber)
            .create()

        return pdfDocument.startPage(pageInfo)
    }

    private fun drawPageHeader(
        context: Context,
        canvas: android.graphics.Canvas,
        paints: PdfPaints,
        report: ReportPdfData
    ) {
        canvas.drawRect(
            0f,
            0f,
            PAGE_WIDTH.toFloat(),
            108f,
            paints.primaryBackground
        )

        drawLogo(
            context = context,
            canvas = canvas,
            x = LEFT,
            y = 22f
        )

        canvas.drawText(
            "SCHOOL GRADE RECORD MANAGEMENT SYSTEM",
            118f,
            40f,
            paints.headerTitle
        )

        canvas.drawText(
            "Official Academic Performance Report",
            118f,
            62f,
            paints.headerSubtitle
        )

        canvas.drawText(
            "Generated Date: ${report.generatedDate.ifBlank { "-" }}",
            118f,
            82f,
            paints.headerSmall
        )

        drawRoundedRect(
            canvas = canvas,
            left = 405f,
            top = 46f,
            right = 555f,
            bottom = 96f,
            radius = 10f,
            paint = paints.whiteSoft
        )

        canvas.drawText(
            "REPORT ID",
            420f,
            64f,
            paints.tinyWhiteLabel
        )

        canvas.drawText(
            report.reportId.ifBlank { "-" }.take(18),
            420f,
            85f,
            paints.reportIdText
        )

        canvas.drawText(
            "Academic Report",
            LEFT,
            135f,
            paints.documentTitle
        )

        canvas.drawLine(
            LEFT,
            145f,
            RIGHT,
            145f,
            paints.primaryLine
        )
    }

    private fun drawContinuationHeader(
        canvas: android.graphics.Canvas,
        paints: PdfPaints,
        report: ReportPdfData,
        pageNumber: Int
    ) {
        canvas.drawRect(
            0f,
            0f,
            PAGE_WIDTH.toFloat(),
            78f,
            paints.primaryBackground
        )

        canvas.drawText(
            "SGRMS Academic Report",
            LEFT,
            38f,
            paints.headerTitle
        )

        canvas.drawText(
            "Report ID: ${report.reportId.ifBlank { "-" }}",
            LEFT,
            58f,
            paints.headerSmall
        )

        canvas.drawText(
            "Page $pageNumber",
            500f,
            48f,
            paints.headerSmall
        )
    }

    private fun drawStudentInformationCard(
        canvas: android.graphics.Canvas,
        paints: PdfPaints,
        y: Float,
        student: StudentModel?,
        report: ReportPdfData
    ): Float {
        drawSectionTitle(
            canvas = canvas,
            paints = paints,
            title = "Student Information",
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
            "Student Name",
            58f,
            top + 30f,
            paints.label
        )

        canvas.drawText(
            student?.studentName?.ifBlank { "-" } ?: "Unknown Student",
            58f,
            top + 52f,
            paints.valueBold
        )

        canvas.drawText(
            "Student ID",
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
            "Class",
            300f,
            top + 30f,
            paints.label
        )

        canvas.drawText(
            student?.className?.ifBlank { "-" } ?: "Unknown Class",
            300f,
            top + 52f,
            paints.valueBold
        )

        canvas.drawText(
            "Report Date",
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
            "Class Teacher",
            58f,
            top + 126f,
            paints.label
        )

        canvas.drawText(
            report.classTeacherName.ifBlank { "Not assigned" },
            145f,
            top + 126f,
            paints.valueBold
        )

        return bottom
    }

    private fun drawAcademicSummary(
        canvas: android.graphics.Canvas,
        paints: PdfPaints,
        y: Float,
        report: ReportPdfData,
        totalSubjects: Int
    ): Float {
        drawSectionTitle(
            canvas = canvas,
            paints = paints,
            title = "Academic Summary",
            y = y
        )

        val top = y + 16f
        val boxWidth = 118f
        val gap = 11f
        val boxHeight = 72f

        val items = listOf(
            SummaryItem("Subjects", totalSubjects.toString(), LIGHT_BLUE),
            SummaryItem("Total Marks", "%.2f".format(report.totalMarks), LIGHT_GREY),
            SummaryItem("Average", "%.2f%%".format(report.averageMarks), LIGHT_BLUE),
            SummaryItem("Grade", report.overallGrade.ifBlank { "-" }, LIGHT_GREY)
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

        val status = GradeCalculator.performanceStatus(report.averageMarks)
        val statusColor = getStatusColor(report.averageMarks)

        val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = statusColor
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText(
            "Performance Status: $status",
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
    private fun drawTableHeader(
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
            "Subject",
            92f,
            headerY,
            paints.cleanTableHeader
        )

        canvas.drawText(
            "Code",
            310f,
            headerY,
            paints.cleanTableHeader
        )

        canvas.drawText(
            "Marks",
            380f,
            headerY,
            paints.cleanTableHeader
        )

        canvas.drawText(
            "Grade",
            440f,
            headerY,
            paints.cleanTableHeader
        )

        canvas.drawText(
            "Status",
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

    private fun drawTableRow(
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

    private fun drawRemarksSection(
        canvas: android.graphics.Canvas,
        paints: PdfPaints,
        y: Float,
        remarks: String
    ): Float {
        drawSectionTitle(
            canvas = canvas,
            paints = paints,
            title = "Report Remarks",
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
            "No specific remarks from grade records."
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

    private fun drawSignatureSection(
        canvas: android.graphics.Canvas,
        paints: PdfPaints,
        y: Float
    ) {
        val top = y
        val lineY = top + 58f

        canvas.drawText(
            "Prepared By",
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
            "Teacher / Academic Officer",
            72f,
            lineY + 20f,
            paints.smallGrey
        )

        canvas.drawText(
            "Verified By",
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
            "School Administrator",
            380f,
            lineY + 20f,
            paints.smallGrey
        )
    }

    private fun drawFooter(
        canvas: android.graphics.Canvas,
        paints: PdfPaints,
        pageNumber: Int
    ) {
        canvas.drawLine(
            LEFT,
            770f,
            RIGHT,
            770f,
            paints.footerLine
        )

        canvas.drawText(
            "This report is generated by SGRMS. Please verify academic records with the school office if required.",
            LEFT,
            790f,
            paints.footerText
        )

        canvas.drawText(
            "Page $pageNumber",
            505f,
            812f,
            paints.footerText
        )
    }

    private fun drawSectionTitle(
        canvas: android.graphics.Canvas,
        paints: PdfPaints,
        title: String,
        y: Float
    ) {
        canvas.drawText(
            title,
            LEFT,
            y,
            paints.sectionTitle
        )
    }

    private fun drawLogo(
        context: Context,
        canvas: android.graphics.Canvas,
        x: Float,
        y: Float
    ) {
        try {
            val logo = ContextCompat.getDrawable(context, R.drawable.sgrms_logo)

            if (logo != null) {
                logo.setBounds(x.toInt(), y.toInt(), (x + 58f).toInt(), (y + 58f).toInt())
                logo.draw(canvas)
            } else {
                drawLogoPlaceholder(
                    canvas = canvas,
                    x = x,
                    y = y
                )
            }
        } catch (e: Exception) {
            drawLogoPlaceholder(
                canvas = canvas,
                x = x,
                y = y
            )
        }
    }

    private fun drawLogoPlaceholder(
        canvas: android.graphics.Canvas,
        x: Float,
        y: Float
    ) {
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = PRIMARY_BLUE
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        canvas.drawCircle(
            x + 29f,
            y + 29f,
            28f,
            circlePaint
        )

        canvas.drawText(
            "SGRMS",
            x + 29f,
            y + 34f,
            textPaint
        )
    }

    private fun drawRoundedRect(
        canvas: android.graphics.Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radius: Float,
        paint: Paint
    ) {
        canvas.drawRoundRect(
            RectF(left, top, right, bottom),
            radius,
            radius,
            paint
        )
    }

    private fun splitTextByWidth(
        text: String,
        paint: Paint,
        maxWidth: Float
    ): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = ""

        words.forEach { word ->
            val testLine = if (currentLine.isBlank()) {
                word
            } else {
                "$currentLine $word"
            }

            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotBlank()) {
                    lines.add(currentLine)
                }

                currentLine = word
            }
        }

        if (currentLine.isNotBlank()) {
            lines.add(currentLine)
        }

        return lines
    }

    private fun getStatusColor(
        averageMarks: Double
    ): Int {
        return when {
            averageMarks >= 70 -> SUCCESS_GREEN
            averageMarks >= 50 -> WARNING_ORANGE
            averageMarks > 0 -> DANGER_RED
            else -> TEXT_GREY
        }
    }

    private fun savePdfToDownloads(
        context: Context,
        pdfDocument: PdfDocument,
        fileName: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )

            if (uri != null) {
                context.contentResolver.openOutputStream(uri).use { outputStream ->
                    if (outputStream != null) {
                        pdfDocument.writeTo(outputStream)
                    }
                }

                Toast.makeText(
                    context,
                    "Report saved as PDF in Downloads",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "Failed to create PDF file",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )

            if (!downloadsFolder.exists()) {
                downloadsFolder.mkdirs()
            }

            val file = File(downloadsFolder, fileName)
            pdfDocument.writeTo(FileOutputStream(file))

            Toast.makeText(
                context,
                "Report saved as PDF in Downloads",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createFileName(
        studentName: String,
        reportId: String
    ): String {
        val cleanName = studentName
            .replace(" ", "_")
            .replace(Regex("[^A-Za-z0-9_]"), "")

        val cleanReportId = reportId
            .replace(" ", "_")
            .replace(Regex("[^A-Za-z0-9_]"), "")

        return "SGRMS_Report_${cleanName}_$cleanReportId.pdf"
    }

    private data class SummaryItem(
        val label: String,
        val value: String,
        val color: Int
    )

    private class PdfPaints {

        val primaryBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = PRIMARY_BLUE
            style = Paint.Style.FILL
        }

        val primaryLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = PRIMARY_BLUE
            strokeWidth = 2.5f
        }

        val headerTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val headerSubtitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 12f
        }

        val headerSmall = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 10.5f
        }

        val documentTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_DARK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val sectionTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_DARK
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val cardBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val whiteSoft = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(45, 255, 255, 255)
            style = Paint.Style.FILL
        }

        val tinyWhiteLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val reportIdText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_GREY
            textSize = 10.5f
        }

        val value = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_DARK
            textSize = 12f
        }

        val valueBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_DARK
            textSize = 12.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val summaryValue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = PRIMARY_BLUE
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_DARK
            textSize = 11f
        }

        val bodyGrey = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_GREY
            textSize = 11f
        }

        val smallGrey = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_GREY
            textSize = 10f
        }

        val cleanTableHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val cleanTableText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 11f
        }

        val cleanTableLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 0.8f
        }

        val signatureLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_GREY
            strokeWidth = 1f
        }

        val footerLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BORDER_GREY
            strokeWidth = 1f
        }

        val footerText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_GREY
            textSize = 8.5f
        }
    }
}