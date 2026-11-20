package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sgrms.data.repository.ClassFirebaseRepository

class ClassFirebaseViewModelFactory(
    private val classRepository: ClassFirebaseRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(ClassFirebaseViewModel::class.java)) {
            return ClassFirebaseViewModel(classRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}