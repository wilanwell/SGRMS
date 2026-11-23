package com.wilanwell.sgrms.ui.notification

import com.wilanwell.sgrms.ui.components.rememberStrings
import com.wilanwell.sgrms.utils.NotificationText
import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.viewmodel.NotificationFirebaseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.foundation.lazy.items
import com.wilanwell.sgrms.ui.components.ChipGroup
import com.wilanwell.sgrms.ui.components.EmptyContent
import com.wilanwell.sgrms.ui.components.ListItemPadding
import com.wilanwell.sgrms.ui.components.RowAction
import com.wilanwell.sgrms.ui.components.SgrmsIconTile
import com.wilanwell.sgrms.ui.components.SgrmsListItemCard
import com.wilanwell.sgrms.ui.components.SgrmsListScreen
import com.wilanwell.sgrms.ui.components.SgrmsOverflowMenu
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

@Composable
fun StudentNotificationScreen(
    studentId: String,
    onBackClick: () -> Unit = {}
) {
    val strings = rememberStrings()
    val gradeUpdatesFromYourTeachersWillText = stringResource(R.string.grade_updates_from_your_teachers_will)
    val unreadText = stringResource(R.string.unread)
    val youReAllCaughtUpText = stringResource(R.string.you_re_all_caught_up)
    val notificationViewModel: NotificationFirebaseViewModel = hiltViewModel()

    val notificationUiState by notificationViewModel.uiState.collectAsState()

    LaunchedEffect(studentId) {
        if (studentId.isNotBlank()) {
            notificationViewModel.loadNotificationsByStudentId(studentId)
        }
    }

    val notifications = notificationUiState.notifications
    val unreadCount = notifications.count { notification ->
        !notification.isRead
    }

    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val shown = notifications
        .filter { selectedFilter == "All" || !it.isRead }
        .filter {
            searchText.isBlank() ||
                NotificationText.title(it, strings).contains(searchText, ignoreCase = true) ||
                NotificationText.body(it, strings).contains(searchText, ignoreCase = true)
        }
        .sortedByDescending { it.createdAt }

    SgrmsListScreen(
        title = stringResource(R.string.notifications),
        subtitle = if (unreadCount == 0) youReAllCaughtUpText else unreadText.format(unreadCount),
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = stringResource(R.string.search_notifications),
        isLoading = notificationUiState.isLoading && notifications.isEmpty(),
        errorMessage = notificationUiState.errorMessage,
        isEmpty = shown.isEmpty(),
        empty = EmptyContent(stringResource(R.string.no_notifications), gradeUpdatesFromYourTeachersWillText, Icons.Default.Notifications),
        chipGroups = listOf(ChipGroup(listOf("All", "Unread"), selectedFilter) { selectedFilter = it }),
        headerActions = {
            if (unreadCount > 0) {
                SgrmsOverflowMenu(
                    contentDescription = stringResource(R.string.notification_actions),
                    actions = listOf(
                        RowAction(stringResource(R.string.mark_all_as_read), Icons.Default.DoneAll) {
                            notificationViewModel.markAllNotificationsAsRead(studentId)
                        }
                    )
                )
            }
        }
    ) {
        items(items = shown, key = { it.notificationId.ifBlank { "${it.createdAt}-${it.title}" } }) { notification ->
            Box(ListItemPadding) {
                SgrmsListItemCard(
                    title = NotificationText.title(notification, strings),
                    subtitle = stringResource(R.string.format_two_lines, NotificationText.body(notification, strings), formatNotificationDate(notification.createdAt)),
                    subtitleMaxLines = 5,
                    leading = {
                        SgrmsIconTile(
                            icon = Icons.Default.Notifications,
                            tint = if (notification.isRead) MaterialTheme.colorScheme.onSurfaceVariant else com.wilanwell.sgrms.ui.theme.SgrmsColors.Orange,
                            container = if (notification.isRead) MaterialTheme.colorScheme.background else com.wilanwell.sgrms.ui.theme.SgrmsColors.OrangeContainer,
                            size = 44.dp
                        )
                    },
                    onClick = {
                        if (!notification.isRead) {
                            notificationViewModel.markNotificationAsRead(
                                studentId = studentId,
                                notificationId = notification.notificationId
                            )
                        }
                    }
                ) {
                    if (!notification.isRead) SgrmsStatusChip(stringResource(R.string.new_label), ChipTone.Warning)
                    SgrmsOverflowMenu(
                        contentDescription = stringResource(R.string.actions_for, NotificationText.title(notification, strings)),
                        actions = listOf(
                            RowAction(stringResource(R.string.delete), Icons.Default.Delete, destructive = true) {
                                notificationViewModel.deleteNotification(notificationId = notification.notificationId)
                            }
                        )
                    )
                }
            }
        }
    }
}

private fun formatNotificationDate(
    timestamp: Long
): String {
    return try {
        val formatter = SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        )

        formatter.format(Date(timestamp))
    } catch (exception: Exception) {
        "-"
    }
}