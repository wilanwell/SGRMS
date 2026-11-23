package com.wilanwell.sgrms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wilanwell.sgrms.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.wilanwell.sgrms.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChangePasswordResult {
    data object Idle : ChangePasswordResult
    data object Loading : ChangePasswordResult
    data object Success : ChangePasswordResult
    data object WrongCurrentPassword : ChangePasswordResult
    data class Error(val message: String) : ChangePasswordResult
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _result = MutableStateFlow<ChangePasswordResult>(ChangePasswordResult.Idle)
    val result: StateFlow<ChangePasswordResult> = _result.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _result.value = ChangePasswordResult.Loading

            _result.value = try {
                authRepository.changePassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
                ChangePasswordResult.Success
            } catch (e: FirebaseAuthWeakPasswordException) {
                // Subclass of InvalidCredentials, so it must be caught first.
                ChangePasswordResult.Error(e.toUserMessage())
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                ChangePasswordResult.WrongCurrentPassword
            } catch (e: Exception) {
                ChangePasswordResult.Error(e.toUserMessage("Failed to update password."))
            }
        }
    }

    fun reset() {
        _result.value = ChangePasswordResult.Idle
    }
}
