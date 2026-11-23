package com.wilanwell.sgrms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.SgrmsRadius
import com.wilanwell.sgrms.ui.theme.SgrmsSpacing

// ---------------------------------------------------------------- initials

private val PATRONYMIC = setOf("bin", "binti", "bt", "bte", "b.", "bt.", "a/l", "a/p", "al", "ap", "s/o", "d/o")
private val TITLES = setOf("mr", "mr.", "mrs", "mrs.", "ms", "ms.", "dr", "dr.", "cik", "en", "en.", "pn", "pn.", "prof", "prof.")

/**
 * Two-letter initials for an avatar. Malay and Indian patronymics are
 * understood: "Adam bin Anuar" → "AA", "Sarah Hadizah binti Abdullah" → "SH".
 */
fun initialsOf(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() && it.lowercase() !in TITLES }
    if (words.isEmpty()) return "?"

    val markerIndex = words.indexOfFirst { it.lowercase() in PATRONYMIC }
    val given = if (markerIndex >= 0) words.take(markerIndex) else words
    val family = if (markerIndex >= 0) words.drop(markerIndex + 1) else emptyList()

    val picked = when {
        given.size >= 2 -> listOf(given[0], given[1])
        given.size == 1 && family.isNotEmpty() -> listOf(given[0], family[0])
        given.isNotEmpty() -> listOf(given[0])
        else -> listOf(words[0])
    }

    return picked.joinToString("") { it.first().uppercaseChar().toString() }
}

// ---------------------------------------------------------------- header

/**
 * Screen header on the background (no coloured bar). With [onBack] it shows
 * a back button; [trailing] holds an avatar or icon buttons.
 */
@Composable
fun SgrmsPageHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = if (onBack != null) 8.dp else SgrmsSpacing.ScreenPadding, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(4.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            if (subtitle != null && onBack == null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = title,
                style = if (onBack != null) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null && onBack != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), content = trailing)
    }
}

// ---------------------------------------------------------------- avatar & tiles

private val avatarPalette = listOf(
    Color(0xFFE0E7FF) to Color(0xFF3730A3),
    Color(0xFFFCE7F3) to Color(0xFF9D174D),
    Color(0xFFDCFCE7) to Color(0xFF166534),
    Color(0xFFFEF3C7) to Color(0xFF92400E),
    Color(0xFFE0F2FE) to Color(0xFF075985)
)

/** Round avatar: the photo when there is one, otherwise coloured initials. */
@Composable
fun SgrmsAvatar(
    name: String,
    size: Dp = 40.dp,
    imageUrl: String = "",
    legacyImageBase64: String = ""
) {
    if (imageUrl.isNotBlank() || legacyImageBase64.isNotBlank()) {
        ProfileImageView(imageUrl = imageUrl, legacyImageBase64 = legacyImageBase64, size = size.value.toInt())
        return
    }

    val (container, content) = avatarPalette[(name.hashCode() and Int.MAX_VALUE) % avatarPalette.size]
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialsOf(name),
            color = content,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.36f).sp
        )
    }
}

/** Small rounded square holding an icon, used on stat cards and rows. */
@Composable
fun SgrmsIconTile(
    icon: ImageVector,
    tint: Color,
    container: Color,
    size: Dp = 36.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(SgrmsRadius.IconTile))
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

// ---------------------------------------------------------------- chips

enum class ChipTone { Neutral, Primary, Success, Warning, Error }

@Composable
private fun ChipTone.colors(): Pair<Color, Color> = when (this) {
    ChipTone.Neutral -> Color(0xFFF3F4F6) to Color(0xFF374151)
    ChipTone.Primary -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    ChipTone.Success -> SgrmsColors.SuccessContainer to SgrmsColors.Success
    ChipTone.Warning -> SgrmsColors.WarningContainer to SgrmsColors.Warning
    ChipTone.Error -> SgrmsColors.ErrorContainer to SgrmsColors.Error
}

/** Rounded status pill, e.g. "Active", "Pending", a grade letter. */
@Composable
fun SgrmsStatusChip(text: String, tone: ChipTone) {
    val (container, content) = tone.colors()
    Text(
        text = text,
        color = content,
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        modifier = Modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Tone for a status string stored in Firestore. */
fun toneForStatus(status: String): ChipTone = when (status.trim().lowercase()) {
    "active", "complete", "completed", "passed" -> ChipTone.Success
    "pending", "in progress" -> ChipTone.Warning
    "inactive", "failed", "at risk" -> ChipTone.Error
    else -> ChipTone.Neutral
}

/** Tone for a grade letter (A+ … E). */
fun toneForGrade(grade: String): ChipTone = when (grade.trim().uppercase()) {
    "A+", "A" -> ChipTone.Primary
    "B" -> ChipTone.Neutral
    "C", "D" -> ChipTone.Warning
    "E", "F" -> ChipTone.Error
    else -> ChipTone.Neutral
}

/** One horizontally scrolling row of single-choice filter chips. */
@Composable
fun SgrmsFilterChips(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = SgrmsSpacing.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option) },
                label = { Text(option, maxLines = 1) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    selectedBorderColor = Color.Transparent
                ),
                modifier = Modifier.heightIn(min = 40.dp)
            )
        }
    }
}

// ---------------------------------------------------------------- cards & rows

/** White 20 dp card whose children are rows separated by thin dividers. */
@Composable
fun SgrmsGroupedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SgrmsRadius.Card),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), content = content)
    }
}

/** Thin divider between rows inside a [SgrmsGroupedCard]. */
@Composable
fun SgrmsRowDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/** One row: leading avatar or tile, a one-line title and subtitle, trailing chips or a menu. */
@Composable
fun SgrmsListRow(
    title: String,
    subtitle: String?,
    leading: @Composable () -> Unit,
    trailing: @Composable RowScope.() -> Unit = {},
    onClick: (() -> Unit)? = null,
    subtitleMaxLines: Int = 1
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        leading()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = subtitleMaxLines,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), content = trailing)
    }
}

data class RowAction(
    val label: String,
    val icon: ImageVector,
    val destructive: Boolean = false,
    val onClick: () -> Unit
)

/** ⋮ button that opens a menu of [actions] (View / Edit / Delete …). */
@Composable
fun SgrmsOverflowMenu(
    actions: List<RowAction>,
    contentDescription: String = "More actions"
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(SgrmsRadius.Field),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            actions.forEach { action ->
                val color = if (action.destructive) SgrmsColors.Error else MaterialTheme.colorScheme.onSurface
                DropdownMenuItem(
                    text = { Text(action.label, color = color) },
                    leadingIcon = { Icon(action.icon, contentDescription = null, tint = color) },
                    onClick = {
                        expanded = false
                        action.onClick()
                    }
                )
            }
        }
    }
}

/** Labelled single-choice segmented control for 2–3 options (gender, status). */
@Composable
fun SgrmsSegmentedField(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    enabled = enabled,
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    label = { Text(option, maxLines = 1) }
                )
            }
        }
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = SgrmsColors.Error)
        }
    }
}

/** Dashboard number card: icon tile, big value, label. */
@Composable
fun SgrmsStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    tint: Color,
    container: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(SgrmsRadius.Card),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick ?: {},
        enabled = onClick != null
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SgrmsIconTile(icon = icon, tint = tint, container = container)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Friendly empty state: icon tile, title and one line of guidance. */
@Composable
fun SgrmsEmptyState(
    title: String,
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SgrmsRadius.Card),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SgrmsIconTile(
                icon = icon,
                tint = MaterialTheme.colorScheme.primary,
                container = MaterialTheme.colorScheme.primaryContainer,
                size = 52.dp
            )
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/** Section title above a card group. */
@Composable
fun SgrmsSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
    }
}
