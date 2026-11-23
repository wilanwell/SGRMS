package com.wilanwell.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilanwell.sgrms.data.repository.AuthRepository
import com.wilanwell.sgrms.utils.isValidEmail
import com.wilanwell.sgrms.utils.toUserMessage
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PasswordResetState {
    data object Idle : PasswordResetState
    data object Sending : PasswordResetState
    data class Sent(val email: String) : PasswordResetState
    data class InvalidEmail(val message: String) : PasswordResetState
    data class Error(val message: String) : PasswordResetState
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val state: StateFlow<PasswordResetState> = _state.asStateFlow()

    fun sendResetEmail(rawEmail: String) {
        val email = rawEmail.trim()

        if (!isValidEmail(email)) {
            _state.value = PasswordResetState.InvalidEmail("Enter a valid email address")
            return
        }

        viewModelScope.launch {
            _state.value = PasswordResetState.Sending

            _state.value = try {
                authRepository.sendPasswordResetEmail(email)
                PasswordResetState.Sent(email)
            } catch (e: FirebaseAuthInvalidUserException) {
                // Same answer whether or not the account exists, so the dialog
                // cannot be used to find out which emails are registered.
                PasswordResetState.Sent(email)
            } catch (e: Exception) {
                PasswordResetState.Error(e.toUserMessage("Could not send the reset email. Please try again."))
            }
        }
    }

    fun reset() {
        _state.value = PasswordResetState.Idle
    }
}
