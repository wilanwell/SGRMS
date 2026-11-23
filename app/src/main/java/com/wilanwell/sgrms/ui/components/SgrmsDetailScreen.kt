package com.wilanwell.sgrms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.SgrmsRadius
import com.wilanwell.sgrms.ui.theme.SgrmsSpacing
import com.wilanwell.sgrms.ui.theme.markColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Template for detail screens: back header, scrolling [content], and an
 * optional bottom action bar that stays visible.
 */
@Composable
fun SgrmsDetailScreen(
    title: String,
    onBack: (() -> Unit)?,
    isLoading: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier,
    headerActions: @Composable RowScope.() -> Unit = {},
    bottomBar: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SgrmsPageHeader(title = title, onBack = onBack, trailing = headerActions)

        Box(modifier = Modifier.weight(1f)) {
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = SgrmsSpacing.ScreenPadding)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (errorMessage.isNotBlank()) {
                        Surface(shape = RoundedCornerShape(SgrmsRadius.Field), color = SgrmsColors.ErrorContainer, modifier = Modifier.fillMaxWidth()) {
                            Text(errorMessage, color = SgrmsColors.Error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
                        }
                    }
                    content()
                }
            }
        }

        if (bottomBar != null) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = SgrmsSpacing.ScreenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    content = bottomBar
                )
            }
        }
    }
}

/** Big centred hero: avatar or tile, name, a subtitle line and chips. */
@Composable
fun SgrmsDetailHero(
    title: String,
    subtitle: String?,
    leading: @Composable () -> Unit,
    chips: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(SgrmsRadius.Hero),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leading()
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, content = chips)
        }
    }
}

/** Section title plus a white card for [SgrmsInfoRow]s or other rows. */
@Composable
fun SgrmsInfoSection(
    title: String,
    trailing: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SgrmsSectionTitle(title, trailing = trailing)
        SgrmsGroupedCard(content = content)
    }
}

/** A subject / grade row: name, marks, coloured bar, grade chip. */
@Composable
fun SgrmsMarkRow(
    title: String,
    subtitle: String?,
    marks: Double?,
    grade: String,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (!subtitle.isNullOrBlank()) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Text(if (marks != null) "%.0f".format(marks) else "-", style = MaterialTheme.typography.titleSmall)
            }
            if (marks != null) {
                LinearProgressIndicator(
                    progress = { (marks / 100.0).toFloat().coerceIn(0f, 1f) },
                    color = markColor(marks),
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(3.dp))
                )
            }
        }
        SgrmsStatusChip(grade.ifBlank { "-" }, if (marks == null) ChipTone.Warning else toneForGrade(grade))
        Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
    }
}

/** Settings-style row: icon tile, title, chevron; red when [destructive]. */
@Composable
fun SgrmsSettingsRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    val tint = if (destructive) SgrmsColors.Error else MaterialTheme.colorScheme.primary
    val container = if (destructive) SgrmsColors.ErrorContainer else MaterialTheme.colorScheme.primaryContainer
    SgrmsListRow(
        title = title,
        subtitle = null,
        leading = { SgrmsIconTile(icon = icon, tint = tint, container = container) },
        trailing = {
            if (!destructive) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        onClick = onClick
    )
}
