package com.wilanwell.sgrms.data.session

import com.wilanwell.sgrms.utils.UserMessageException
import com.wilanwell.sgrms.R
import com.wilanwell.sgrms.data.model.UserModel
import com.wilanwell.sgrms.data.model.UserRole
import com.wilanwell.sgrms.data.model.userRole
import com.wilanwell.sgrms.data.repository.AuthRepository
import com.wilanwell.sgrms.data.repository.DeviceTokenRepository
import com.wilanwell.sgrms.notifications.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SessionState {
    /** Restoring the Firebase session on app start. */
    data object Loading : SessionState

    data object LoggedOut : SessionState

    data class LoggedIn(
        val user: UserModel,
        val role: UserRole
    ) : SessionState
}

/**
 * Single source of truth for who is signed in.
 *
 * Screens and ViewModels read the current user, role, studentId and
 * teacherId from here instead of keeping their own copies or querying
 * FirebaseAuth / users/{uid} themselves.
 */
@Singleton
class SessionManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val deviceTokenRepository: DeviceTokenRepository,
    private val notificationScheduler: NotificationScheduler
) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    val currentUser: UserModel?
        get() = (state.value as? SessionState.LoggedIn)?.user

    val currentRole: UserRole?
        get() = (state.value as? SessionState.LoggedIn)?.role

    val currentUserId: String
        get() = currentUser?.userId.orEmpty()

    val currentStudentId: String
        get() = currentUser?.studentId.orEmpty()

    val currentTeacherId: String
        get() = currentUser?.teacherId.orEmpty()

    /** Restores a persisted Firebase session; call once on app start. */
    suspend fun restore() {
        if (!authRepository.isUserLoggedIn()) {
            _state.value = SessionState.LoggedOut
            return
        }

        _state.value = try {
            authRepository.getCurrentUserProfile()?.let(::loggedInOrOut)
                ?: signOutAndReturn()
        } catch (e: Exception) {
            // Offline or rules error: do not leave the user stuck on a loader.
            signOutAndReturn()
        }

        registerDeviceForPush()
    }

    suspend fun login(email: String, password: String): SessionState {
        val user = authRepository.loginUser(email = email, password = password)
        val state = loggedInOrOut(user)

        if (state is SessionState.LoggedOut) {
            throw UserMessageException(R.string.this_account_has_no_valid_role)
        }

        _state.value = state
        registerDeviceForPush()
        return state
    }

    /** Reloads users/{uid}, e.g. after the user edits their profile. */
    suspend fun refresh() {
        authRepository.getCurrentUserProfile()?.let { _state.value = loggedInOrOut(it) }
    }

    fun logout() {
        notificationScheduler.stop()
        _state.value = signOutAndReturn()
    }

    /** Students receive grade notifications on this phone. */
    private suspend fun registerDeviceForPush() {
        if (currentRole != UserRole.STUDENT) return

        // Spark plan: the phone checks for new notifications itself.
        notificationScheduler.startForStudent()

        try {
            deviceTokenRepository.registerCurrentDevice(currentStudentId)
        } catch (e: Exception) {
            // Push is optional: in-app notifications still work without it.
        }
    }

    private fun loggedInOrOut(user: UserModel): SessionState {
        val role = user.userRole ?: return signOutAndReturn()
        return SessionState.LoggedIn(user = user, role = role)
    }

    private fun signOutAndReturn(): SessionState {
        authRepository.logoutUser()
        return SessionState.LoggedOut
    }
}
