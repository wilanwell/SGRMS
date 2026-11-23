package com.wilanwell.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilanwell.sgrms.data.model.StudentModel
import com.wilanwell.sgrms.data.repository.StudentFirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import com.wilanwell.sgrms.data.repository.PageCursor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.wilanwell.sgrms.utils.toUserMessage

data class StudentFirebaseUiState(
    val students: List<StudentModel> = emptyList(),
    val selectedStudent: StudentModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    /** Paging (management list screens): more pages can be loaded. */
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false
)

@HiltViewModel
class StudentFirebaseViewModel @Inject constructor(
    private val studentRepository: StudentFirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentFirebaseUiState())
    val uiState: StateFlow<StudentFirebaseUiState> = _uiState.asStateFlow()

    private var nextCursor: PageCursor? = null
    private var pageFilter: String? = null

    /** Loads the first page; [classId] narrows the query on the server. */
    fun loadFirstStudentPage(classId: String? = null) {
        pageFilter = classId
        nextCursor = null

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "", successMessage = "") }

            try {
                val page = studentRepository.getStudentsPage(classId = pageFilter, cursor = null)
                nextCursor = page.nextCursor
                _uiState.update { it.copy(students = page.items, hasMore = page.hasMore, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(students = emptyList(), hasMore = false, isLoading = false, errorMessage = e.toUserMessage("Failed to load students"))
                }
            }
        }
    }

    fun loadMoreStudents() {
        val cursor = nextCursor ?: return
        if (_uiState.value.isLoadingMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = "") }

            try {
                val page = studentRepository.getStudentsPage(classId = pageFilter, cursor = cursor)
                nextCursor = page.nextCursor
                _uiState.update {
                    it.copy(
                        students = (it.students + page.items).distinctBy { item -> item.studentId },
                        hasMore = page.hasMore,
                        isLoadingMore = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingMore = false, errorMessage = e.toUserMessage("Failed to load more students")) }
            }
        }
    }

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
                    students = _uiState.value.students.filterNot { it.studentId == studentId },
                    isLoading = false,
                    successMessage = "Student deleted successfully"
                )

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