package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgrms.data.model.SubjectModel
import com.example.sgrms.data.repository.SubjectFirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubjectFirebaseUiState(
    val subjects: List<SubjectModel> = emptyList(),
    val selectedSubject: SubjectModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

class SubjectFirebaseViewModel(
    private val subjectRepository: SubjectFirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectFirebaseUiState())
    val uiState: StateFlow<SubjectFirebaseUiState> = _uiState.asStateFlow()

    fun addSubject(
        subjectCode: String,
        subjectName: String,
        teacherId: String,
        teacherName: String,
        category: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                subjectRepository.addSubject(
                    subjectCode = subjectCode,
                    subjectName = subjectName,
                    teacherId = teacherId,
                    teacherName = teacherName,
                    category = category
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Subject added successfully"
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to add subject"
                )
            }
        }
    }

    fun loadSubjects() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val subjects = subjectRepository.getAllSubjects()

                _uiState.value = _uiState.value.copy(
                    subjects = subjects,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load subjects"
                )
            }
        }
    }

    fun loadSubjectById(subjectId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val subject = subjectRepository.getSubjectById(subjectId)

                _uiState.value = _uiState.value.copy(
                    selectedSubject = subject,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load subject"
                )
            }
        }
    }

    fun updateSubject(
        subject: SubjectModel,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                subjectRepository.updateSubject(subject)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Subject updated successfully"
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to update subject"
                )
            }
        }
    }

    fun deleteSubject(
        subjectId: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                subjectRepository.deleteSubject(subjectId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Subject deleted successfully"
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to delete subject"
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