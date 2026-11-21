package com.example.sgrms.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Screen header. Kept for existing callers; it now draws [SgrmsPageHeader]
 * on the page background instead of a blue app bar. [showIcon] and [icon]
 * are no longer drawn.
 */
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") showIcon: Boolean = true,
    @Suppress("UNUSED_PARAMETER") icon: ImageVector = Icons.Default.School,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    SgrmsPageHeader(
        title = title,
        subtitle = subtitle,
        onBack = if (showBack) onBackClick else null,
        modifier = modifier,
        trailing = actions
    )
}
