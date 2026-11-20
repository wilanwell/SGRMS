package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sgrms.data.repository.GradeFirebaseRepository
import com.example.sgrms.data.repository.NotificationFirebaseRepository

class GradeFirebaseViewModelFactory(
    private val gradeRepository: GradeFirebaseRepository,
    private val notificationRepository: NotificationFirebaseRepository? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(GradeFirebaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GradeFirebaseViewModel(gradeRepository, notificationRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}