package com.example.sgrms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sgrms.ui.theme.SgrmsColors
import com.example.sgrms.ui.theme.SgrmsRadius
import com.example.sgrms.ui.theme.SgrmsSpacing

/** One single-choice group of filter chips ("All classes", "5 Bestari", …). */
data class ChipGroup(
    val options: List<String>,
    val selected: String,
    val onSelect: (String) -> Unit
)

data class ListFab(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

data class EmptyContent(
    val title: String,
    val message: String,
    val icon: ImageVector
)

/** Several chip groups in one horizontally scrolling row, separated by a thin rule. */
@Composable
fun SgrmsChipGroups(groups: List<ChipGroup>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = SgrmsSpacing.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        groups.forEachIndexed { index, group ->
            if (index > 0) {
                Box(
                    Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
            group.options.forEach { option ->
                val isSelected = option == group.selected
                FilterChip(
                    selected = isSelected,
                    onClick = { group.onSelect(option) },
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
}

/** White rounded row card used for every item of a list screen. */
@Composable
fun SgrmsListItemCard(
    title: String,
    subtitle: String?,
    leading: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.padding(start = 14.dp, end = 4.dp)) {
            SgrmsListRow(title = title, subtitle = subtitle, leading = leading, trailing = trailing, onClick = onClick)
        }
    }
}

/**
 * Template for every list screen: header (with optional back and actions),
 * search field, chip filters, then the list, an extended FAB, and empty /
 * loading / error states.
 */
@Composable
fun SgrmsListScreen(
    title: String,
    subtitle: String?,
    searchText: String,
    onSearchChange: (String) -> Unit,
    searchPlaceholder: String,
    isLoading: Boolean,
    errorMessage: String,
    isEmpty: Boolean,
    empty: EmptyContent,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    chipGroups: List<ChipGroup> = emptyList(),
    fab: ListFab? = null,
    headerActions: @Composable RowScope.() -> Unit = {},
    summary: (@Composable () -> Unit)? = null,
    items: LazyListScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = if (fab != null) 96.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "header") {
                SgrmsPageHeader(title = title, subtitle = subtitle, onBack = onBack, trailing = headerActions)
            }

            item(key = "search") {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchChange,
                    placeholder = { Text(searchPlaceholder, maxLines = 1) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(SgrmsRadius.Field),
                    colors = sgrmsFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SgrmsSpacing.ScreenPadding)
                )
            }

            if (summary != null) {
                item(key = "summary") {
                    Box(Modifier.padding(horizontal = SgrmsSpacing.ScreenPadding)) { summary() }
                }
            }

            if (chipGroups.isNotEmpty()) {
                item(key = "chips") { SgrmsChipGroups(chipGroups) }
            }

            when {
                isLoading -> item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage.isNotBlank() -> item(key = "error") {
                    Surface(
                        shape = RoundedCornerShape(SgrmsRadius.Field),
                        color = SgrmsColors.ErrorContainer,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = SgrmsSpacing.ScreenPadding)
                    ) {
                        Text(errorMessage, color = SgrmsColors.Error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
                    }
                }

                isEmpty -> item(key = "empty") {
                    SgrmsEmptyState(
                        title = empty.title,
                        message = empty.message,
                        icon = empty.icon,
                        modifier = Modifier.padding(horizontal = SgrmsSpacing.ScreenPadding)
                    )
                }

                else -> items()
            }
        }

        if (fab != null) {
            ExtendedFloatingActionButton(
                onClick = fab.onClick,
                icon = { Icon(fab.icon, contentDescription = null) },
                text = { Text(fab.label, fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(SgrmsRadius.Card),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = SgrmsSpacing.ScreenPadding, bottom = 16.dp)
            )
        }
    }
}

/** Horizontal padding for list items inside [SgrmsListScreen]. */
val ListItemPadding = Modifier.padding(horizontal = SgrmsSpacing.ScreenPadding)
