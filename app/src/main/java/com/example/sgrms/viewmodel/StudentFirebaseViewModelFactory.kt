package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sgrms.data.repository.StudentFirebaseRepository

class StudentFirebaseViewModelFactory(
    private val studentRepository: StudentFirebaseRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(StudentFirebaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentFirebaseViewModel(studentRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}