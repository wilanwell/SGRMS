package com.wilanwell.sgrms.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import com.wilanwell.sgrms.data.model.UserRole

/**
 * Who is signed in, for screens that hide actions the user may not take.
 * The real check is in firestore.rules; this only keeps the UI honest.
 */
data class SessionAccess(
    val role: UserRole? = null,
    val teacherId: String = ""
) {
    val isAdmin: Boolean get() = role == UserRole.ADMIN

    /** Same rule as isMyTeacherId in firestore.rules: admins manage every subject, a teacher only their own. */
    fun canWriteSubject(subjectTeacherId: String): Boolean =
        isAdmin || (role == UserRole.TEACHER && teacherId.isNotBlank() && subjectTeacherId == teacherId)
}

/** Provided once by AppNavigation from the current session. */
val LocalSessionAccess = staticCompositionLocalOf { SessionAccess() }
