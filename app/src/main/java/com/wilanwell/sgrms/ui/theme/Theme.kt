package com.wilanwell.sgrms.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ColorPrimary,
    onPrimary = ColorOnPrimary,
    primaryContainer = ColorPrimaryContainer,
    onPrimaryContainer = ColorOnPrimaryContainer,
    secondary = ColorPrimary,
    onSecondary = ColorOnPrimary,
    secondaryContainer = ColorPrimaryContainer,
    onSecondaryContainer = ColorOnPrimaryContainer,
    background = ColorBackground,
    onBackground = ColorOnSurface,
    surface = ColorSurface,
    onSurface = ColorOnSurface,
    surfaceVariant = ColorBackground,
    onSurfaceVariant = ColorOnSurfaceVariant,
    surfaceContainerLowest = ColorSurface,
    surfaceContainerLow = ColorSurface,
    surfaceContainer = ColorSurface,
    surfaceContainerHigh = ColorSurface,
    surfaceContainerHighest = ColorBackground,
    outline = ColorOutline,
    outlineVariant = ColorOutlineVariant,
    error = SgrmsColors.Error,
    errorContainer = SgrmsColors.ErrorContainer,
    onError = ColorOnPrimary,
    onErrorContainer = SgrmsColors.Error
)

/** SGRMS "Indigo Clean" theme: light colour scheme, Plus Jakarta Sans, rounded shapes. */
@Composable
fun SGRMSTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = SgrmsShapes,
        content = content
    )
}
