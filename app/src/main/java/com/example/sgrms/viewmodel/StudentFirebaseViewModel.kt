package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.data.repository.StudentFirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.sgrms.utils.toUserMessage

data class StudentFirebaseUiState(
    val students: List<StudentModel> = emptyList(),
    val selectedStudent: StudentModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

@HiltViewModel
class StudentFirebaseViewModel @Inject constructor(
    private val studentRepository: StudentFirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentFirebaseUiState())
    val uiState: StateFlow<StudentFirebaseUiState> = _uiState.asStateFlow()

    fun loadStudents() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val students = studentRepository.getAllStudents()

                _uiState.value = _uiState.value.copy(
                    students = students,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to load students")
                )
            }
        }
    }

    fun loadStudentById(
        studentId: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val student = studentRepository.getStudentById(studentId)

                _uiState.value = _uiState.value.copy(
                    selectedStudent = student,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to load student")
                )
            }
        }
    }

    fun addStudentWithAccount(
        studentCode: String,
        studentName: String,
        email: String,
        phoneNumber: String,
        classId: String,
        className: String,
        gender: String,
        status: String,
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

                studentRepository.addStudentWithAccount(
                    studentCode = studentCode,
                    studentName = studentName,
                    email = email,
                    phoneNumber = phoneNumber,
                    classId = classId,
                    className = className,
                    gender = gender,
                    status = status,
                    password = password
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Student added successfully"
                )

                loadStudents()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to add student")
                )
            }
        }
    }

    fun updateStudent(
        student: StudentModel,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                studentRepository.updateStudent(student)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Student updated successfully"
                )

                loadStudents()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to update student")
                )
            }
        }
    }

    fun deleteStudent(
        studentId: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                studentRepository.deleteStudent(studentId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Student deleted successfully"
                )

                loadStudents()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to delete student")
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