package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgrms.data.repository.AdminSummary
import com.example.sgrms.data.repository.DashboardRepository
import com.example.sgrms.data.repository.TeacherDashboardSummary
import com.example.sgrms.data.repository.TeacherFirebaseRepository
import com.example.sgrms.data.session.SessionManager
import com.example.sgrms.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardUiState(
    val summary: AdminSummary = AdminSummary(),
    val isLoading: Boolean = true,
    val errorMessage: String = ""
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }

            try {
                val summary = dashboardRepository.getAdminSummary()
                _uiState.value = AdminDashboardUiState(summary = summary, isLoading = false)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.toUserMessage("Failed to load dashboard data."))
                }
            }
        }
    }
}

data class TeacherDashboardUiState(
    val teacherName: String = "Teacher",
    val summary: TeacherDashboardSummary = TeacherDashboardSummary(),
    val isLoading: Boolean = true,
    val errorMessage: String = ""
)

@HiltViewModel
class TeacherDashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val teacherRepository: TeacherFirebaseRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TeacherDashboardUiState(
            teacherName = sessionManager.currentUser?.fullName?.ifBlank { null } ?: "Teacher"
        )
    )
    val uiState: StateFlow<TeacherDashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }

            try {
                val teacherName = sessionManager.currentTeacherId
                    .takeIf { it.isNotBlank() }
                    ?.let { teacherRepository.getTeacherById(it)?.teacherName }
                    ?.takeIf { it.isNotBlank() }
                    ?: _uiState.value.teacherName

                val summary = dashboardRepository.getTeacherDashboardSummary()

                _uiState.value = TeacherDashboardUiState(
                    teacherName = teacherName,
                    summary = summary,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.toUserMessage("Failed to load dashboard data."))
                }
            }
        }
    }
}
