package com.wilanwell.sgrms.viewmodel

import com.wilanwell.sgrms.utils.Strings
import com.wilanwell.sgrms.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilanwell.sgrms.data.model.ClassModel
import com.wilanwell.sgrms.data.repository.ClassFirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.wilanwell.sgrms.utils.toUserMessage

data class ClassFirebaseUiState(
    val classes: List<ClassModel> = emptyList(),
    val selectedClass: ClassModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

@HiltViewModel
class ClassFirebaseViewModel @Inject constructor(
    private val strings: Strings,
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
                    errorMessage = e.toUserMessage(strings, R.string.failed_to_load_classes)
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
                    errorMessage = e.toUserMessage(strings, R.string.failed_to_load_class)
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
                    successMessage = strings.get(R.string.class_added_successfully)
                )

                loadClasses()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage(strings, R.string.failed_to_add_class)
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
                    successMessage = strings.get(R.string.class_updated_successfully)
                )

                loadClasses()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage(strings, R.string.failed_to_update_class)
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
                    successMessage = strings.get(R.string.class_deleted_successfully)
                )

                loadClasses()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage(strings, R.string.failed_to_delete_class)
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