package com.wilanwell.sgrms.utils

import android.util.Log
import androidx.annotation.StringRes
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.wilanwell.sgrms.R

/**
 * Turns an exception into a message that is safe and useful to show to the
 * user, instead of raw SDK text such as
 * "PERMISSION_DENIED: Missing or insufficient permissions."
 */
fun Throwable.toUserMessage(strings: Strings, @StringRes fallback: Int = R.string.error_generic): String {
    val error = if (this is IllegalStateException && this !is UserMessageException && cause != null) cause!! else this

    // The user sees a friendly message; logcat keeps the real cause
    // (e.g. FAILED_PRECONDITION "The query requires an index").
    Log.w("SGRMS", strings.get(fallback), this)

    return strings.get(userMessageRes(error, fallback))
}

/** The string resource to show for [error]; [fallback] when nothing more specific fits. */
@StringRes
fun userMessageRes(error: Throwable, @StringRes fallback: Int): Int = when (error) {
    // Messages we throw ourselves are written for users.
    is UserMessageException -> error.messageRes

    is FirebaseNetworkException -> R.string.error_no_internet
    is FirebaseTooManyRequestsException -> R.string.error_too_many_attempts
    is FirebaseAuthWeakPasswordException -> R.string.error_weak_password
    is FirebaseAuthUserCollisionException -> R.string.error_email_in_use
    is FirebaseAuthInvalidUserException -> R.string.error_account_missing
    is FirebaseAuthInvalidCredentialsException -> R.string.error_wrong_credentials
    is FirebaseAuthRecentLoginRequiredException -> R.string.error_login_again

    is FirebaseFirestoreException -> when (error.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> R.string.error_permission_denied
        FirebaseFirestoreException.Code.UNAVAILABLE -> R.string.error_service_unavailable
        FirebaseFirestoreException.Code.NOT_FOUND -> R.string.error_record_missing
        else -> fallback
    }

    else -> fallback
}
