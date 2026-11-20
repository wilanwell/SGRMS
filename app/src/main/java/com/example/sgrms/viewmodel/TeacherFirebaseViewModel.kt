package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgrms.data.model.TeacherModel
import com.example.sgrms.data.repository.TeacherFirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.sgrms.utils.toUserMessage

data class TeacherFirebaseUiState(
    val teachers: List<TeacherModel> = emptyList(),
    val selectedTeacher: TeacherModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

@HiltViewModel
class TeacherFirebaseViewModel @Inject constructor(
    private val teacherRepository: TeacherFirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherFirebaseUiState())
    val uiState: StateFlow<TeacherFirebaseUiState> = _uiState.asStateFlow()

    fun loadTeachers() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val teachers = teacherRepository.getAllTeachers()

                _uiState.value = _uiState.value.copy(
                    teachers = teachers,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to load teachers")
                )
            }
        }
    }

    fun loadTeacherById(
        teacherId: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val teacher = teacherRepository.getTeacherById(teacherId)

                _uiState.value = _uiState.value.copy(
                    selectedTeacher = teacher,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to load teacher")
                )
            }
        }
    }

    fun addTeacherWithAccount(
        staffId: String,
        teacherName: String,
        email: String,
        phoneNumber: String,
        department: String,
        position: String,
        password: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                teacherRepository.addTeacherWithAccount(
                    staffId = staffId,
                    teacherName = teacherName,
                    email = email,
                    phoneNumber = phoneNumber,
                    department = department,
                    position = position,
                    password = password
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Teacher added successfully"
                )

                loadTeachers()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to add teacher")
                )
            }
        }
    }

    fun updateTeacher(
        teacher: TeacherModel,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                teacherRepository.updateTeacher(teacher)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Teacher updated successfully"
                )

                loadTeachers()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to update teacher")
                )
            }
        }
    }

    fun deleteTeacher(
        teacherId: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                teacherRepository.deleteTeacher(teacherId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Teacher deleted successfully"
                )

                loadTeachers()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to delete teacher")
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