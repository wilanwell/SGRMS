package com.wilanwell.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilanwell.sgrms.data.model.NotificationModel
import com.wilanwell.sgrms.data.repository.NotificationFirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.wilanwell.sgrms.utils.toUserMessage

data class NotificationFirebaseUiState(
    val notifications: List<NotificationModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

@HiltViewModel
class NotificationFirebaseViewModel @Inject constructor(
    private val notificationRepository: NotificationFirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationFirebaseUiState())
    val uiState: StateFlow<NotificationFirebaseUiState> = _uiState.asStateFlow()

    fun loadNotificationsByStudentId(
        studentId: String
    ) {
        if (studentId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                notifications = emptyList(),
                isLoading = false,
                errorMessage = "Invalid student ID",
                successMessage = ""
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val notifications = notificationRepository.getNotificationsByStudentId(studentId)

                _uiState.value = _uiState.value.copy(
                    notifications = notifications,
                    isLoading = false,
                    errorMessage = "",
                    successMessage = ""
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.toUserMessage("Failed to load notifications"),
                    successMessage = ""
                )
            }
        }
    }

    fun markNotificationAsRead(
        studentId: String,
        notificationId: String
    ) {
        if (notificationId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Notification ID is missing",
                successMessage = ""
            )
            return
        }

        val previousNotifications = _uiState.value.notifications

        _uiState.value = _uiState.value.copy(
            notifications = previousNotifications.map { notification ->
                if (notification.notificationId == notificationId) {
                    notification.copy(isRead = true)
                } else {
                    notification
                }
            },
            errorMessage = "",
            successMessage = "Notification marked as read"
        )

        viewModelScope.launch {
            try {
                notificationRepository.markNotificationAsRead(notificationId)

                if (studentId.isNotBlank()) {
                    val refreshedNotifications =
                        notificationRepository.getNotificationsByStudentId(studentId)

                    _uiState.value = _uiState.value.copy(
                        notifications = refreshedNotifications,
                        errorMessage = "",
                        successMessage = "Notification marked as read"
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    notifications = previousNotifications,
                    errorMessage = exception.toUserMessage("Failed to mark notification as read"),
                    successMessage = ""
                )
            }
        }
    }

    fun markAllNotificationsAsRead(
        studentId: String
    ) {
        if (studentId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Invalid student ID",
                successMessage = ""
            )
            return
        }

        val previousNotifications = _uiState.value.notifications

        _uiState.value = _uiState.value.copy(
            notifications = previousNotifications.map { notification ->
                notification.copy(isRead = true)
            },
            errorMessage = "",
            successMessage = "All notifications marked as read"
        )

        viewModelScope.launch {
            try {
                notificationRepository.markAllNotificationsAsRead(studentId)

                val refreshedNotifications =
                    notificationRepository.getNotificationsByStudentId(studentId)

                _uiState.value = _uiState.value.copy(
                    notifications = refreshedNotifications,
                    errorMessage = "",
                    successMessage = "All notifications marked as read"
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    notifications = previousNotifications,
                    errorMessage = exception.toUserMessage("Failed to mark all notifications as read"),
                    successMessage = ""
                )
            }
        }
    }

    fun deleteNotification(
        notificationId: String
    ) {
        if (notificationId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Notification ID is missing",
                successMessage = ""
            )
            return
        }

        val previousNotifications = _uiState.value.notifications

        _uiState.value = _uiState.value.copy(
            notifications = previousNotifications.filter { notification ->
                notification.notificationId != notificationId
            },
            errorMessage = "",
            successMessage = "Notification deleted"
        )

        viewModelScope.launch {
            try {
                notificationRepository.deleteNotification(notificationId)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    notifications = previousNotifications,
                    errorMessage = exception.toUserMessage("Failed to delete notification"),
                    successMessage = ""
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = "",
            successMessage = ""
        )
    }
}