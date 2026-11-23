package com.wilanwell.sgrms.viewmodel

import com.wilanwell.sgrms.utils.Strings
import com.wilanwell.sgrms.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilanwell.sgrms.data.session.SessionManager
import com.wilanwell.sgrms.data.session.SessionState
import com.wilanwell.sgrms.utils.toUserMessage
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
    private val strings: Strings,
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
            _loginUiState.update { it.copy(errorMessage = strings.get(R.string.please_enter_your_email_and_password)) }
            return
        }

        viewModelScope.launch {
            _loginUiState.update { it.copy(isLoading = true, errorMessage = "") }

            try {
                sessionManager.login(email = email.trim(), password = password)
                _loginUiState.value = LoginUiState()
            } catch (e: Exception) {
                _loginUiState.update {
                    it.copy(isLoading = false, errorMessage = e.toUserMessage(strings, R.string.login_failed))
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
