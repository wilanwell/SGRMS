package com.wilanwell.sgrms.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.wilanwell.sgrms.R
import java.io.File
import java.io.FileOutputStream

// Pages, drawing helpers and saving the file to Downloads.

internal fun PdfContext.startPage(
    pdfDocument: PdfDocument,
    pageNumber: Int
): PdfDocument.Page {
    val pageInfo = PdfDocument.PageInfo
        .Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber)
        .create()

    return pdfDocument.startPage(pageInfo)
}

internal fun PdfContext.drawRoundedRect(
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

internal fun PdfContext.splitTextByWidth(
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

internal fun PdfContext.savePdfToDownloads(
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
                strings.get(R.string.report_saved_as_pdf_in_downloads),
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                strings.get(R.string.failed_to_create_pdf_file),
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
            strings.get(R.string.report_saved_as_pdf_in_downloads),
            Toast.LENGTH_LONG
        ).show()
    }
}

internal fun PdfContext.createFileName(
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
