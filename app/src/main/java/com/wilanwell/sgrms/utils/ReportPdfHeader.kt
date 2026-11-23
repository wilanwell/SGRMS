package com.wilanwell.sgrms.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import com.wilanwell.sgrms.R

// Page header, continuation header, logo, footer and section titles.

internal fun PdfContext.drawPageHeader(
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
        strings.get(R.string.school_grade_record_management_system),
        118f,
        40f,
        paints.headerTitle
    )

    canvas.drawText(
        strings.get(R.string.official_academic_performance_report),
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
        strings.get(R.string.report_id),
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
        strings.get(R.string.academic_report),
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

internal fun PdfContext.drawContinuationHeader(
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
        strings.get(R.string.sgrms_academic_report),
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
        strings.get(R.string.page, pageNumber),
        500f,
        48f,
        paints.headerSmall
    )
}

internal fun PdfContext.drawLogo(
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

internal fun PdfContext.drawLogoPlaceholder(
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

internal fun PdfContext.drawFooter(
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
        strings.get(R.string.this_report_is_generated_by_sgrms),
        LEFT,
        790f,
        paints.footerText
    )

    canvas.drawText(
        strings.get(R.string.page, pageNumber),
        505f,
        812f,
        paints.footerText
    )
}

internal fun PdfContext.drawSectionTitle(
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
