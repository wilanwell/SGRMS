package com.wilanwell.sgrms.viewmodel

import com.wilanwell.sgrms.utils.NotificationText
import com.wilanwell.sgrms.data.model.UserRole
import com.wilanwell.sgrms.utils.Strings
import com.wilanwell.sgrms.utils.UserMessageException
import com.wilanwell.sgrms.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilanwell.sgrms.data.model.GradeModel
import com.wilanwell.sgrms.data.model.NotificationModel
import com.wilanwell.sgrms.data.model.StudentModel
import com.wilanwell.sgrms.data.repository.GradeFirebaseRepository
import com.wilanwell.sgrms.data.repository.NotificationFirebaseRepository
import com.wilanwell.sgrms.data.repository.StudentFirebaseRepository
import com.wilanwell.sgrms.data.repository.SubjectFirebaseRepository
import com.wilanwell.sgrms.data.session.SessionManager
import com.wilanwell.sgrms.utils.GradeCalculator
import com.wilanwell.sgrms.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddStudentGradeSubjectItem(
    val subjectId: String,
    val subjectName: String,
    val subjectCode: String,
    val existingGrade: GradeModel? = null,
    val marksText: String = "",
    val remarks: String = "",
    val status: String = "Pending",
    val message: String = "",
    val isSaving: Boolean = false
) {
    val existingGradeId: String get() = existingGrade?.gradeId.orEmpty()
}

data class AddStudentGradeUiState(
    val student: StudentModel? = null,
    val subjectItems: List<AddStudentGradeSubjectItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String = ""
)

/** Enter or update one student's marks subject by subject. */
@HiltViewModel
class AddStudentGradeViewModel @Inject constructor(
    private val strings: Strings,
    private val studentRepository: StudentFirebaseRepository,
    private val subjectRepository: SubjectFirebaseRepository,
    private val gradeRepository: GradeFirebaseRepository,
    private val notificationRepository: NotificationFirebaseRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddStudentGradeUiState())
    val uiState: StateFlow<AddStudentGradeUiState> = _uiState.asStateFlow()

    fun load(studentId: String) {
        if (studentId.isBlank()) {
            _uiState.value = AddStudentGradeUiState(isLoading = false, errorMessage = strings.get(R.string.student_id_is_missing))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }

            try {
                val student = studentRepository.getStudentById(studentId)
                    ?: throw UserMessageException(R.string.student_not_found_2)

                val subjects = async { subjectRepository.getAllSubjects() }
                val grades = async { gradeRepository.getGradesByStudentId(studentId) }
                val gradesBySubject = grades.await().associateBy { it.subjectId }

                // Teachers grade only the subjects they teach (enforced by firestore.rules).
                val gradable = subjects.await().filter { subject ->
                    sessionManager.currentRole != UserRole.TEACHER ||
                        (sessionManager.currentTeacherId.isNotBlank() && subject.teacherId == sessionManager.currentTeacherId)
                }

                val items = gradable.map { subject ->
                    val grade = gradesBySubject[subject.subjectId]

                    AddStudentGradeSubjectItem(
                        subjectId = subject.subjectId,
                        subjectName = subject.subjectName,
                        subjectCode = subject.subjectCode,
                        existingGrade = grade,
                        marksText = grade?.marks?.takeIf { it > 0 }?.toString().orEmpty(),
                        remarks = grade?.remarks.orEmpty(),
                        status = grade?.status ?: strings.get(R.string.pending)
                    )
                }

                _uiState.value = AddStudentGradeUiState(
                    student = student,
                    subjectItems = items,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.toUserMessage(strings, R.string.failed_to_load_subjects))
                }
            }
        }
    }

    fun onMarksChange(index: Int, marksText: String) =
        updateItem(index) { it.copy(marksText = marksText, message = "") }

    fun onRemarksChange(index: Int, remarks: String) =
        updateItem(index) { it.copy(remarks = remarks, message = "") }

    fun saveGrade(index: Int) {
        val state = _uiState.value
        val student = state.student ?: return
        val item = state.subjectItems.getOrNull(index) ?: return

        val marks = item.marksText.trim().toDoubleOrNull()
        if (marks == null || !GradeCalculator.isValidMarks(marks)) {
            updateItem(index) { it.copy(message = strings.get(R.string.please_enter_marks_between_0_and)) }
            return
        }

        viewModelScope.launch {
            updateItem(index) { it.copy(isSaving = true, message = "") }

            try {
                val saved = gradeRepository.addGrade(
                    GradeModel(
                        gradeId = item.existingGradeId,
                        studentId = student.studentId,
                        subjectId = item.subjectId,
                        marks = marks,
                        gradeLetter = GradeCalculator.gradeLetter(marks),
                        status = "Complete",
                        remarks = item.remarks.trim(),
                        createdByTeacherId = sessionManager.currentTeacherId,
                        createdAt = item.existingGrade?.createdAt ?: System.currentTimeMillis()
                    )
                )

                // The student's phone picks this up in NotificationCheckWorker (no Cloud Functions on Spark).
                notificationRepository.addNotification(
                    NotificationModel(
                        studentId = student.studentId,
                        title = if (item.existingGrade == null) strings.get(R.string.new_grade) else strings.get(R.string.grade_updated),
                        message = strings.get(R.string.your_grade_is_now, item.subjectName, GradeCalculator.formatMarks(marks), saved.gradeLetter),
                        type = if (item.existingGrade == null) NotificationText.TYPE_GRADE_NEW else NotificationText.TYPE_GRADE_UPDATED,
                        relatedGradeId = saved.gradeId,
                        relatedSubjectId = item.subjectId,
                        subjectName = item.subjectName,
                        marks = marks,
                        gradeLetter = saved.gradeLetter,
                        isRead = false,
                        createdAt = System.currentTimeMillis()
                    )
                )

                updateItem(index) {
                    it.copy(
                        existingGrade = saved,
                        status = "Complete",
                        isSaving = false,
                        message = strings.get(R.string.grade_saved_successfully)
                    )
                }
            } catch (e: Exception) {
                updateItem(index) {
                    it.copy(isSaving = false, message = e.toUserMessage(strings, R.string.failed_to_save_grade))
                }
            }
        }
    }

    private fun updateItem(
        index: Int,
        transform: (AddStudentGradeSubjectItem) -> AddStudentGradeSubjectItem
    ) {
        _uiState.update { state ->
            if (index !in state.subjectItems.indices) return@update state

            state.copy(
                subjectItems = state.subjectItems.toMutableList().also { items ->
                    items[index] = transform(items[index])
                }
            )
        }
    }
}
