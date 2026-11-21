package com.example.sgrms.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.sgrms.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val plusJakartaSans = GoogleFont("Plus Jakarta Sans")

/**
 * Plus Jakarta Sans, downloaded by Google Play services on the phone.
 * If the provider is missing or offline, Compose falls back to the system
 * font, so text never disappears.
 */
val SgrmsFontFamily = FontFamily(
    Font(googleFont = plusJakartaSans, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = plusJakartaSans, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = plusJakartaSans, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = plusJakartaSans, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = plusJakartaSans, fontProvider = fontProvider, weight = FontWeight.ExtraBold)
)

private fun style(size: Int, line: Int, weight: FontWeight, spacing: Double = 0.0) = TextStyle(
    fontFamily = SgrmsFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing.sp
)

val Typography = Typography(
    displayLarge = style(48, 56, FontWeight.ExtraBold, -1.0),
    displayMedium = style(40, 48, FontWeight.ExtraBold, -1.0),
    displaySmall = style(32, 40, FontWeight.ExtraBold, -0.5),
    headlineLarge = style(28, 36, FontWeight.ExtraBold, -0.5),
    headlineMedium = style(24, 32, FontWeight.ExtraBold, -0.5),
    headlineSmall = style(22, 28, FontWeight.ExtraBold, -0.4),
    titleLarge = style(18, 26, FontWeight.Bold),
    titleMedium = style(16, 24, FontWeight.Bold),
    titleSmall = style(14, 20, FontWeight.SemiBold),
    bodyLarge = style(16, 24, FontWeight.Normal),
    bodyMedium = style(14, 20, FontWeight.Normal),
    bodySmall = style(12, 16, FontWeight.Medium),
    labelLarge = style(14, 20, FontWeight.SemiBold),
    labelMedium = style(12, 16, FontWeight.SemiBold),
    labelSmall = style(11, 16, FontWeight.SemiBold)
)
