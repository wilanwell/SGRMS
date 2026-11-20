package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgrms.data.model.ClassModel
import com.example.sgrms.data.repository.ClassFirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.sgrms.utils.toUserMessage

data class ClassFirebaseUiState(
    val classes: List<ClassModel> = emptyList(),
    val selectedClass: ClassModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

@HiltViewModel
class ClassFirebaseViewModel @Inject constructor(
    private val classRepository: ClassFirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassFirebaseUiState())
    val uiState: StateFlow<ClassFirebaseUiState> = _uiState.asStateFlow()

    fun loadClasses() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val classes = classRepository.getAllClasses()

                _uiState.value = _uiState.value.copy(
                    classes = classes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to load classes")
                )
            }
        }
    }

    fun loadClassById(
        classId: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val schoolClass = classRepository.getClassById(classId)

                _uiState.value = _uiState.value.copy(
                    selectedClass = schoolClass,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to load class")
                )
            }
        }
    }

    fun addClass(
        className: String,
        academicYear: String,
        classTeacherId: String,
        classTeacherName: String,
        description: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                classRepository.addClass(
                    className = className,
                    academicYear = academicYear,
                    classTeacherId = classTeacherId,
                    classTeacherName = classTeacherName,
                    description = description
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Class added successfully"
                )

                loadClasses()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to add class")
                )
            }
        }
    }

    fun updateClass(
        schoolClass: ClassModel,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                classRepository.updateClass(schoolClass)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Class updated successfully"
                )

                loadClasses()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to update class")
                )
            }
        }
    }

    fun deleteClass(
        classId: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                classRepository.deleteClass(classId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Class deleted successfully"
                )

                loadClasses()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to delete class")
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