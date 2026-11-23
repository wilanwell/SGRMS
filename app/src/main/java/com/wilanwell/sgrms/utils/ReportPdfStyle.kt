package com.wilanwell.sgrms.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.wilanwell.sgrms.utils.Strings

// Page geometry, colours, paints and the drawing context.

/*
    PDF COORDINATE SYSTEM GUIDE

    x = horizontal position
        Smaller x = more left
        Bigger x = more right

    y = vertical position
        Smaller y = higher
        Bigger y = lower
*/

internal const val PAGE_WIDTH = 595
internal const val PAGE_HEIGHT = 842
internal const val LEFT = 40f
internal const val RIGHT = 555f
internal const val PRIMARY_BLUE = 0xFF4F46E5.toInt() // brand indigo
internal const val LIGHT_BLUE = 0xFFEEF2FF.toInt() // brand indigo, light
internal const val LIGHT_GREY = 0xFFF4F6F8.toInt()
internal const val BORDER_GREY = 0xFFBDBDBD.toInt()
internal const val TEXT_DARK = 0xFF212121.toInt()
internal const val TEXT_GREY = 0xFF616161.toInt()
internal const val SUCCESS_GREEN = 0xFF2E7D32.toInt()
internal const val WARNING_ORANGE = 0xFFEF6C00.toInt()
internal const val DANGER_RED = 0xFFC62828.toInt()

/** What every drawing function needs: the app context and the report text. */
internal class PdfContext(
    val context: Context,
    val strings: Strings
)

internal data class SummaryItem(
    val label: String,
    val value: String,
    val color: Int
)

internal class PdfPaints {

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
