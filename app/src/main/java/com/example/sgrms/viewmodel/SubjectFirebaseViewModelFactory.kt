package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sgrms.data.repository.SubjectFirebaseRepository

class SubjectFirebaseViewModelFactory(
    private val subjectRepository: SubjectFirebaseRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(SubjectFirebaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectFirebaseViewModel(subjectRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}