package com.example.sgrms.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

// "Indigo Clean" palette. Inside composables prefer MaterialTheme.colorScheme;
// these constants feed the scheme and code outside composition.

val ColorPrimary = Color(0xFF4F46E5)
val ColorOnPrimary = Color(0xFFFFFFFF)
val ColorPrimaryContainer = Color(0xFFEEF2FF)
val ColorOnPrimaryContainer = Color(0xFF3730A3)

val ColorBackground = Color(0xFFF5F6FA)
val ColorSurface = Color(0xFFFFFFFF)
val ColorOnSurface = Color(0xFF111827)
val ColorOnSurfaceVariant = Color(0xFF676E7E) // spec said #6B7280; that is 4.48:1 on the background
val ColorOutline = Color(0xFFD1D5DB)
val ColorOutlineVariant = Color(0xFFF1F2F6)

/** Status and accent colours that Material's ColorScheme has no slot for. */
object SgrmsColors {
    val Success = Color(0xFF047857)
    val SuccessContainer = Color(0xFFECFDF5)

    val Warning = Color(0xFF92400E)
    val WarningContainer = Color(0xFFFFFBEB)
    val WarningBar = Color(0xFFF59E0B)

    val Error = Color(0xFFB91C1C)
    val ErrorContainer = Color(0xFFFEF2F2)

    val Orange = Color(0xFFC2410C)
    val OrangeContainer = Color(0xFFFFF7ED)

    val Pink = Color(0xFFBE185D)
    val PinkContainer = Color(0xFFFDF2F8)
}

/** WCAG 2.x contrast ratio between two opaque colours (1.0 to 21.0). */
fun contrastRatio(a: Color, b: Color): Double {
    val la = relativeLuminance(a)
    val lb = relativeLuminance(b)
    return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
}

private fun relativeLuminance(color: Color): Double {
    fun channel(c: Float): Double {
        val v = c.toDouble()
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
}
