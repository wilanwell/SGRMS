package com.wilanwell.sgrms.viewmodel

import com.wilanwell.sgrms.utils.GradeCalculator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilanwell.sgrms.data.model.GradeModel
import com.wilanwell.sgrms.data.model.NotificationModel
import com.wilanwell.sgrms.data.repository.GradeFirebaseRepository
import com.wilanwell.sgrms.data.repository.NotificationFirebaseRepository
import com.wilanwell.sgrms.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import com.wilanwell.sgrms.data.repository.PageCursor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.wilanwell.sgrms.utils.toUserMessage

data class GradeFirebaseUiState(
    val grades: List<GradeModel> = emptyList(),
    val selectedGrade: GradeModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    /** Paging (management list screens): more pages can be loaded. */
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false
)

data class GradeBulkEntry(
    val studentId: String,
    val subjectId: String,
    val marks: Double,
    val gradeLetter: String,
    val status: String,
    val remarks: String = "",
    val createdByTeacherId: String = "",
    val subjectName: String = ""
)

/**
 * The grade document a bulk entry saves. A new grade records [teacherId] as
 * its creator; an update keeps the original creator, creation time and the
 * teacher's remarks unless the entry brings new remarks.
 */
fun bulkGradeRecord(existing: GradeModel?, entry: GradeBulkEntry, teacherId: String, now: Long): GradeModel =
    existing?.copy(
        marks = entry.marks,
        gradeLetter = entry.gradeLetter,
        status = entry.status,
        remarks = entry.remarks.ifBlank { existing.remarks },
        createdByTeacherId = existing.createdByTeacherId.ifBlank { teacherId }
    ) ?: GradeModel(
        studentId = entry.studentId,
        subjectId = entry.subjectId,
        marks = entry.marks,
        gradeLetter = entry.gradeLetter,
        status = entry.status,
        remarks = entry.remarks,
        createdByTeacherId = teacherId,
        createdAt = now
    )

@HiltViewModel
class GradeFirebaseViewModel @Inject constructor(
    private val gradeRepository: GradeFirebaseRepository,
    private val notificationRepository: NotificationFirebaseRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradeFirebaseUiState())
    val uiState: StateFlow<GradeFirebaseUiState> = _uiState.asStateFlow()

    private var nextCursor: PageCursor? = null
    private var pageFilter: String? = null

    /** Loads the first page; [subjectId] narrows the query on the server. */
    fun loadFirstGradePage(subjectId: String? = null) {
        pageFilter = subjectId
        nextCursor = null

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "", successMessage = "") }

            try {
                val page = gradeRepository.getGradesPage(subjectId = pageFilter, cursor = null)
                nextCursor = page.nextCursor
                _uiState.update { it.copy(grades = page.items, hasMore = page.hasMore, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(grades = emptyList(), hasMore = false, isLoading = false, errorMessage = e.toUserMessage("Failed to load grades"))
                }
            }
        }
    }

    fun loadMoreGrades() {
        val cursor = nextCursor ?: return
        if (_uiState.value.isLoadingMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = "") }

            try {
                val page = gradeRepository.getGradesPage(subjectId = pageFilter, cursor = cursor)
                nextCursor = page.nextCursor
                _uiState.update {
                    it.copy(
                        grades = (it.grades + page.items).distinctBy { item -> item.gradeId },
                        hasMore = page.hasMore,
                        isLoadingMore = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingMore = false, errorMessage = e.toUserMessage("Failed to load more grades")) }
            }
        }
    }

    fun loadGrades() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val grades = gradeRepository.getAllGrades()

                _uiState.value = _uiState.value.copy(
                    grades = grades,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to load grades")
                )
            }
        }
    }

    fun loadGradeById(
        gradeId: String
    ) {
        if (gradeId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                selectedGrade = null,
                errorMessage = "Invalid grade ID"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val grade = gradeRepository.getGradeById(gradeId)

                _uiState.value = _uiState.value.copy(
                    selectedGrade = grade,
                    isLoading = false,
                    errorMessage = if (grade == null) {
                        "Grade not found"
                    } else {
                        ""
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to load grade")
                )
            }
        }
    }

    fun loadGradesByStudentId(
        studentId: String
    ) {
        if (studentId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                grades = emptyList(),
                errorMessage = "Invalid student ID"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val grades = gradeRepository.getGradesByStudentId(studentId)

                _uiState.value = _uiState.value.copy(
                    grades = grades,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to load student grades")
                )
            }
        }
    }

    fun loadGradesBySubjectId(
        subjectId: String
    ) {
        if (subjectId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                grades = emptyList(),
                errorMessage = "Invalid subject ID"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val grades = gradeRepository.getGradesBySubjectId(subjectId)

                _uiState.value = _uiState.value.copy(
                    grades = grades,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to load subject grades")
                )
            }
        }
    }

    fun addGrade(
        grade: GradeModel,
        onSuccess: () -> Unit = {}
    ) {
        if (grade.studentId.isBlank() || grade.subjectId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Student and subject are required"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                gradeRepository.addGrade(grade)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Grade added successfully"
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to add grade")
                )
            }
        }
    }

    fun saveBulkGrades(
        gradeEntries: List<GradeBulkEntry>,
        onSuccess: () -> Unit = {}
    ) {
        if (gradeEntries.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No grades to save"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                gradeEntries.forEach { entry ->
                    if (entry.studentId.isNotBlank() && entry.subjectId.isNotBlank()) {
                        val existingGrade = gradeRepository.getGradeByStudentAndSubject(
                            studentId = entry.studentId,
                            subjectId = entry.subjectId
                        )

                        val record = bulkGradeRecord(
                            existing = existingGrade,
                            entry = entry,
                            teacherId = entry.createdByTeacherId.ifBlank { sessionManager.currentTeacherId },
                            now = System.currentTimeMillis()
                        )

                        // New and changed grades both notify the student.
                        val saved = if (existingGrade == null) {
                            gradeRepository.addGrade(record)
                        } else {
                            gradeRepository.updateGrade(record)
                            record
                        }

                        createGradeUpdatedNotification(
                            grade = saved,
                            subjectName = entry.subjectName,
                            isNew = existingGrade == null
                        )
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Grades saved successfully"
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to save grades")
                )
            }
        }
    }

    fun addOrUpdateGradeByStudentAndSubject(
        studentId: String,
        subjectId: String,
        marks: Double,
        gradeLetter: String,
        status: String,
        remarks: String,
        createdByTeacherId: String = "",
        onSuccess: () -> Unit = {}
    ) {
        if (studentId.isBlank() || subjectId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Student and subject are required"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                val existingGrade = gradeRepository.getGradeByStudentAndSubject(
                    studentId = studentId,
                    subjectId = subjectId
                )

                if (existingGrade == null) {
                    val newGrade = GradeModel(
                        studentId = studentId,
                        subjectId = subjectId,
                        marks = marks,
                        gradeLetter = gradeLetter,
                        status = status,
                        remarks = remarks,
                        createdByTeacherId = createdByTeacherId,
                        createdAt = System.currentTimeMillis()
                    )

                    gradeRepository.addGrade(newGrade)
                } else {
                    val updatedGrade = existingGrade.copy(
                        marks = marks,
                        gradeLetter = gradeLetter,
                        status = status,
                        remarks = remarks,
                        createdByTeacherId = createdByTeacherId.ifBlank {
                            existingGrade.createdByTeacherId
                        }
                    )

                    gradeRepository.updateGrade(updatedGrade)

                    createGradeUpdatedNotification(
                        grade = updatedGrade,
                        subjectName = ""
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Grade saved successfully"
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to save grade")
                )
            }
        }
    }

    fun updateGrade(
        grade: GradeModel,
        subjectName: String = "",
        onSuccess: () -> Unit = {}
    ) {
        if (grade.studentId.isBlank() || grade.subjectId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Student and subject are required"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                gradeRepository.updateGrade(grade)

                createGradeUpdatedNotification(
                    grade = grade,
                    subjectName = subjectName
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Grade updated successfully"
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to update grade")
                )
            }
        }
    }

    fun deleteGrade(
        gradeId: String,
        onSuccess: () -> Unit = {}
    ) {
        if (gradeId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Invalid grade ID"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = "",
                    successMessage = ""
                )

                gradeRepository.deleteGrade(gradeId)

                _uiState.value = _uiState.value.copy(
                    grades = _uiState.value.grades.filterNot { it.gradeId == gradeId },
                    isLoading = false,
                    successMessage = "Grade deleted successfully"
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.toUserMessage("Failed to delete grade")
                )
            }
        }
    }

    private suspend fun createGradeUpdatedNotification(
        grade: GradeModel,
        subjectName: String = "",
        isNew: Boolean = false
    ) {
        val repository = notificationRepository ?: return

        val displaySubjectName = subjectName.ifBlank {
            "your subject"
        }

        repository.addNotification(
            NotificationModel(
                studentId = grade.studentId,
                title = if (isNew) "New grade" else "Grade updated",
                message = "Your $displaySubjectName grade is now ${GradeCalculator.formatMarks(grade.marks)} (${grade.gradeLetter}).",
                type = "GRADE_UPDATED",
                relatedGradeId = grade.gradeId,
                relatedSubjectId = grade.subjectId,
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = "",
            successMessage = ""
        )
    }
}