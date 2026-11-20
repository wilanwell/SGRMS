package com.example.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sgrms.data.session.SessionManager
import com.example.sgrms.data.session.SessionState
import com.example.sgrms.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    val session: StateFlow<SessionState> = sessionManager.state

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    init {
        if (sessionManager.state.value is SessionState.Loading) {
            viewModelScope.launch { sessionManager.restore() }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginUiState.update { it.copy(errorMessage = "Please enter your email and password.") }
            return
        }

        viewModelScope.launch {
            _loginUiState.update { it.copy(isLoading = true, errorMessage = "") }

            try {
                sessionManager.login(email = email.trim(), password = password)
                _loginUiState.value = LoginUiState()
            } catch (e: Exception) {
                _loginUiState.update {
                    it.copy(isLoading = false, errorMessage = e.toUserMessage("Login failed."))
                }
            }
        }
    }

    fun logout() {
        sessionManager.logout()
    }

    fun clearError() {
        _loginUiState.update { it.copy(errorMessage = "") }
    }
}
