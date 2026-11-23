package com.wilanwell.sgrms.utils

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import com.wilanwell.sgrms.R
import com.wilanwell.sgrms.data.model.StudentModel
import com.wilanwell.sgrms.utils.AndroidStrings

// Builds the student report PDF: page order and pagination. The drawing lives in ReportPdf*.kt.

object ReportPdfGenerator {

    fun saveReportAsPdf(
        context: Context,
        report: ReportPdfData,
        student: StudentModel?,
        gradeItems: List<ReportPdfGradeItem>
    ) {
        val pdfDocument = PdfDocument()

        with(PdfContext(context.applicationContext, AndroidStrings(context.applicationContext))) {
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
                    title = strings.get(R.string.subject_performance),
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
                        strings.get(R.string.no_completed_subject_grade_records_found),
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
                                title = strings.get(R.string.subject_performance_continued),
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
                    e.toUserMessage(strings, R.string.failed_to_create_pdf_file),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                pdfDocument.close()
            }
        }
    }
}
