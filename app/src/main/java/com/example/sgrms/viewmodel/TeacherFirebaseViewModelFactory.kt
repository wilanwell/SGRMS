package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sgrms.data.repository.TeacherFirebaseRepository

class TeacherFirebaseViewModelFactory(
    private val teacherRepository: TeacherFirebaseRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(TeacherFirebaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeacherFirebaseViewModel(teacherRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}