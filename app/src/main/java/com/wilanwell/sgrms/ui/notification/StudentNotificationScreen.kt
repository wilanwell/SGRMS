package com.wilanwell.sgrms.ui.notification

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
                it.title.contains(searchText, ignoreCase = true) ||
                it.message.contains(searchText, ignoreCase = true)
        }
        .sortedByDescending { it.createdAt }

    SgrmsListScreen(
        title = "Notifications",
        subtitle = if (unreadCount == 0) "You're all caught up" else "$unreadCount unread",
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = "Search notifications",
        isLoading = notificationUiState.isLoading && notifications.isEmpty(),
        errorMessage = notificationUiState.errorMessage,
        isEmpty = shown.isEmpty(),
        empty = EmptyContent("No notifications", "Grade updates from your teachers will show up here.", Icons.Default.Notifications),
        chipGroups = listOf(ChipGroup(listOf("All", "Unread"), selectedFilter) { selectedFilter = it }),
        headerActions = {
            if (unreadCount > 0) {
                SgrmsOverflowMenu(
                    contentDescription = "Notification actions",
                    actions = listOf(
                        RowAction("Mark all as read", Icons.Default.DoneAll) {
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
                    title = notification.title.ifBlank { "Notification" },
                    subtitle = "${notification.message}\n${formatNotificationDate(notification.createdAt)}",
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
                    if (!notification.isRead) SgrmsStatusChip("New", ChipTone.Warning)
                    SgrmsOverflowMenu(
                        contentDescription = "Actions for ${notification.title}",
                        actions = listOf(
                            RowAction("Delete", Icons.Default.Delete, destructive = true) {
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