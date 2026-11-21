package com.example.sgrms.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.sgrms.utils.GradeCalculator
import com.example.sgrms.utils.PerformanceLevel

/** Colour for a mark or an average, shared by every screen. */
fun markColor(marks: Double): Color {
    return when (GradeCalculator.performanceLevel(marks)) {
        PerformanceLevel.EXCELLENT -> SgrmsColors.Success
        PerformanceLevel.GOOD,
        PerformanceLevel.SATISFACTORY -> ColorPrimary
        PerformanceLevel.NEEDS_IMPROVEMENT -> SgrmsColors.WarningBar
        PerformanceLevel.AT_RISK -> SgrmsColors.Error
    }
}
