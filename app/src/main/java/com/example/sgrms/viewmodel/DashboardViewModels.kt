package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgrms.data.repository.AdminSummary
import com.example.sgrms.data.repository.DashboardRepository
import com.example.sgrms.data.repository.TeacherDashboardSummary
import com.example.sgrms.data.repository.TeacherFirebaseRepository
import com.example.sgrms.data.repository.UserFirebaseRepository
import com.example.sgrms.data.model.UserModel
import com.example.sgrms.data.model.UserRole
import com.example.sgrms.data.model.userRole
import kotlinx.coroutines.async
import com.example.sgrms.data.session.SessionManager
import com.example.sgrms.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

data class AdminDashboardUiState(
    val summary: AdminSummary = AdminSummary(),
    val recentAccounts: List<UserModel> = emptyList(),
    val isLoading: Boolean = true,
    /** True once figures have loaded at least once (until then, show "–", not 0). */
    val loaded: Boolean = false,
    val errorMessage: String = ""
)

private const val RECENT_ACCOUNTS = 3

/** The [limit] newest teacher / student accounts, newest first. */
fun recentAccounts(users: List<UserModel>, limit: Int = RECENT_ACCOUNTS): List<UserModel> =
    users
        .filter { it.userRole != UserRole.ADMIN }
        .sortedByDescending { it.createdAt }
        .take(limit)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val userRepository: UserFirebaseRepository
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
                // The recent list is a nice-to-have: its failure never fails the dashboard.
                val recent = async {
                    try {
                        // +1 leaves room for the admin account, which recentAccounts() drops.
                        recentAccounts(userRepository.getNewestUsers(limit = RECENT_ACCOUNTS + 1L))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
                val summary = dashboardRepository.getAdminSummary()
                _uiState.value = AdminDashboardUiState(
                    summary = summary,
                    recentAccounts = recent.await(),
                    isLoading = false,
                    loaded = true
                )
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
    val teacherImageUrl: String = "",
    val teacherImageBase64: String = "",
    val summary: TeacherDashboardSummary = TeacherDashboardSummary(),
    val isLoading: Boolean = true,
    /** True once figures have loaded at least once (until then, show "–", not 0). */
    val loaded: Boolean = false,
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
                val teacher = sessionManager.currentTeacherId
                    .takeIf { it.isNotBlank() }
                    ?.let { teacherRepository.getTeacherById(it) }
                val teacherName = teacher?.teacherName
                    ?.takeIf { it.isNotBlank() }
                    ?: _uiState.value.teacherName

                val summary = dashboardRepository.getTeacherDashboardSummary()

                _uiState.value = TeacherDashboardUiState(
                    teacherName = teacherName,
                    teacherImageUrl = teacher?.profileImageUrl.orEmpty(),
                    teacherImageBase64 = teacher?.profileImageBase64.orEmpty(),
                    summary = summary,
                    isLoading = false,
                    loaded = true
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.toUserMessage("Failed to load dashboard data."))
                }
            }
        }
    }
}
