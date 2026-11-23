package com.wilanwell.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilanwell.sgrms.data.model.StudentModel
import com.wilanwell.sgrms.data.repository.ReportRepository
import com.wilanwell.sgrms.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportRemarksUiState(
    val remarks: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    val saved: Boolean = false
)

@HiltViewModel
class ReportRemarksViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportRemarksUiState())
    val uiState: StateFlow<ReportRemarksUiState> = _uiState.asStateFlow()

    fun loadRemarks(studentId: String) {
        if (studentId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }

            try {
                val remarks = reportRepository.getRemarks(studentId)
                _uiState.update { it.copy(remarks = remarks, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.toUserMessage("Failed to load report remarks."))
                }
            }
        }
    }

    fun saveRemarks(studentId: String, remarks: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = "", successMessage = "") }

            try {
                reportRepository.saveRemarks(studentId = studentId, remarks = remarks.trim())
                _uiState.update {
                    it.copy(
                        remarks = remarks.trim(),
                        isSaving = false,
                        saved = true,
                        successMessage = "Report remarks updated successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.toUserMessage("Failed to save report remarks."))
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = "", successMessage = "") }
    }

    suspend fun getClassTeacherName(student: StudentModel?): String =
        reportRepository.getClassTeacherName(student)
}
