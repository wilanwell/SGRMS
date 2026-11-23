package com.wilanwell.sgrms.viewmodel

import com.wilanwell.sgrms.utils.Strings
import com.wilanwell.sgrms.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilanwell.sgrms.data.model.UserModel
import com.wilanwell.sgrms.data.repository.UserFirebaseRepository
import com.wilanwell.sgrms.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserManagementUiState(
    val users: List<UserModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val strings: Strings,
    private val userRepository: UserFirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }

            try {
                val users = userRepository.getAllUsers()
                _uiState.update { it.copy(users = users, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.toUserMessage(strings, R.string.failed_to_load_users))
                }
            }
        }
    }

    fun deleteAccount(user: UserModel) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = "") }

            try {
                userRepository.deleteAccount(user.userId)
                _uiState.update { state ->
                    state.copy(users = state.users.filterNot { it.userId == user.userId })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.toUserMessage(strings, R.string.failed_to_delete_user)) }
            }
        }
    }
}
