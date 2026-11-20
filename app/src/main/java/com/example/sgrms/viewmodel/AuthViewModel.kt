package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgrms.data.model.UserModel
import com.example.sgrms.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: UserModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isLoggedIn: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    fun loginUser(
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            try {
                _authUiState.value = _authUiState.value.copy(
                    isLoading = true,
                    errorMessage = ""
                )

                val user = authRepository.loginUser(
                    email = email,
                    password = password
                )

                _authUiState.value = AuthUiState(
                    currentUser = user,
                    isLoading = false,
                    errorMessage = "",
                    isLoggedIn = true
                )
            } catch (e: Exception) {
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Login failed",
                    isLoggedIn = false
                )
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                _authUiState.value = _authUiState.value.copy(
                    isLoading = true,
                    errorMessage = ""
                )

                val user = authRepository.getCurrentUserProfile()

                _authUiState.value = AuthUiState(
                    currentUser = user,
                    isLoading = false,
                    errorMessage = "",
                    isLoggedIn = user != null
                )
            } catch (e: Exception) {
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load user",
                    isLoggedIn = false
                )
            }
        }
    }

    fun logoutUser() {
        authRepository.logoutUser()

        _authUiState.value = AuthUiState(
            currentUser = null,
            isLoading = false,
            errorMessage = "",
            isLoggedIn = false
        )
    }

    fun clearError() {
        _authUiState.value = _authUiState.value.copy(
            errorMessage = ""
        )
    }
}