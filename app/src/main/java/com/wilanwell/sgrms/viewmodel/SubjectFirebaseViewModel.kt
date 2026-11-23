package com.wilanwell.sgrms.viewmodel

import com.wilanwell.sgrms.utils.Strings
import com.wilanwell.sgrms.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilanwell.sgrms.data.model.SubjectModel
import com.wilanwell.sgrms.data.repository.SubjectFirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.wilanwell.sgrms.utils.toUserMessage

data class SubjectFirebaseUiState(
    val subjects: List<SubjectModel> = emptyList(),
    val selectedSubject: SubjectModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

@HiltViewModel
class SubjectFirebaseViewModel @Inject constructor(
    private val strings: Strings,
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
                    successMessage = strings.get(R.string.subject_added_successfully)
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage(strings, R.string.failed_to_add_subject)
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
                    errorMessage = e.toUserMessage(strings, R.string.failed_to_load_subjects)
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
                    errorMessage = e.toUserMessage(strings, R.string.failed_to_load_subject)
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
                    successMessage = strings.get(R.string.subject_updated_successfully)
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage(strings, R.string.failed_to_update_subject)
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
                    successMessage = strings.get(R.string.subject_deleted_successfully)
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage(strings, R.string.failed_to_delete_subject)
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