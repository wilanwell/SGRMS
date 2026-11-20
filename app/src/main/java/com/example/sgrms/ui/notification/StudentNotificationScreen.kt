package com.example.sgrms.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sgrms.data.firebase.NotificationFirebaseService
import com.example.sgrms.data.model.NotificationModel
import com.example.sgrms.data.repository.NotificationFirebaseRepository
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextDark
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.viewmodel.NotificationFirebaseViewModel
import com.example.sgrms.viewmodel.NotificationFirebaseViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudentNotificationScreen(
    studentId: String,
    onBackClick: () -> Unit = {}
) {
    val notificationFirebaseService = remember {
        NotificationFirebaseService()
    }

    val notificationFirebaseRepository = remember {
        NotificationFirebaseRepository(notificationFirebaseService)
    }

    val notificationViewModel: NotificationFirebaseViewModel = viewModel(
        factory = NotificationFirebaseViewModelFactory(notificationFirebaseRepository)
    )

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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "SGRMS",
                showBack = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGrey)
                .navigationBarsPadding()
                .padding(paddingValues)
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 18.dp,
                    bottom = 8.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Notifications",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Text(
                        text = if (unreadCount > 0) {
                            "$unreadCount unread notification(s)"
                        } else {
                            "You are all caught up"
                        },
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }

                if (unreadCount > 0) {
                    TextButton(
                        onClick = {
                            notificationViewModel.markAllNotificationsAsRead(studentId)
                        }
                    ) {
                        Text(
                            text = "Read all",
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (notificationUiState.errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                ErrorMessageCard(
                    message = notificationUiState.errorMessage
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            when {
                notificationUiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue
                        )
                    }
                }

                notifications.isEmpty() -> {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.Notifications,
                        title = "No notifications yet",
                        message = "Grade update notifications will appear here after your teacher updates your marks."
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(
                            items = notifications,
                            key = { index, notification ->
                                notification.notificationId.ifBlank {
                                    "${notification.studentId}-${notification.createdAt}-$index"
                                }
                            }
                        ) { _, notification ->
                            SimpleNotificationCard(
                                notification = notification,
                                onClick = {
                                    if (!notification.isRead) {
                                        notificationViewModel.markNotificationAsRead(
                                            studentId = studentId,
                                            notificationId = notification.notificationId
                                        )
                                    }
                                },
                                onDeleteClick = {
                                    notificationViewModel.deleteNotification(
                                        notificationId = notification.notificationId
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleNotificationCard(
    notification: NotificationModel,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateText = remember(notification.createdAt) {
        formatNotificationDate(notification.createdAt)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !notification.isRead) {
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) {
                1.dp
            } else {
                4.dp
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    top = 14.dp,
                    end = 8.dp,
                    bottom = 14.dp
                ),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (notification.isRead) {
                            Color(0xFFECEFF1)
                        } else {
                            PrimaryBlue.copy(alpha = 0.12f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification",
                    tint = if (notification.isRead) {
                        TextGrey
                    } else {
                        PrimaryBlue
                    },
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title.ifBlank {
                            "Grade Updated"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notification.message.ifBlank {
                        "Your subject grade has been updated."
                    },
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = TextGrey
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = dateText,
                    fontSize = 12.sp,
                    color = TextGrey
                )

                if (!notification.isRead) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap to mark as read",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Notification",
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorMessageCard(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Text(
            text = message,
            color = Color(0xFFC62828),
            fontSize = 14.sp,
            modifier = Modifier.padding(14.dp)
        )
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