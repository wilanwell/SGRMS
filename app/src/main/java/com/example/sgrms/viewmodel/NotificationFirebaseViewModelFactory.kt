package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sgrms.data.repository.NotificationFirebaseRepository

class NotificationFirebaseViewModelFactory(
    private val notificationRepository: NotificationFirebaseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(NotificationFirebaseViewModel::class.java)) {
            return NotificationFirebaseViewModel(notificationRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}