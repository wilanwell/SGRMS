package com.example.sgrms.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorContrastTest {

    private fun assertPasses(foreground: Color, background: Color, label: String) {
        val ratio = contrastRatio(foreground, background)
        assertTrue("$label contrast was $ratio, needs >= 4.5", ratio >= 4.5)
    }

    @Test
    fun secondaryTextPassesOnBackgroundAndSurface() {
        assertPasses(ColorOnSurfaceVariant, ColorBackground, "secondary text on background")
        assertPasses(ColorOnSurfaceVariant, ColorSurface, "secondary text on surface")
    }

    @Test
    fun statusPairsPass() {
        assertPasses(SgrmsColors.Success, SgrmsColors.SuccessContainer, "success")
        assertPasses(SgrmsColors.Warning, SgrmsColors.WarningContainer, "warning")
        assertPasses(SgrmsColors.Error, SgrmsColors.ErrorContainer, "error")
    }

    @Test
    fun primaryButtonTextPasses() {
        assertPasses(Color.White, ColorPrimary, "white on primary")
        assertPasses(ColorOnPrimaryContainer, ColorPrimaryContainer, "primary container text")
    }

    @Test
    fun contrastOfBlackOnWhiteIs21() {
        val ratio = contrastRatio(Color.Black, Color.White)
        assertTrue("was $ratio", ratio in 20.9..21.1)
    }
}
