package com.example.sgrms.utils

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Turns an exception into a message that is safe and useful to show to the
 * user, instead of raw SDK text such as
 * "PERMISSION_DENIED: Missing or insufficient permissions."
 */
fun Throwable.toUserMessage(fallback: String = "Something went wrong. Please try again."): String {
    val error = if (this is IllegalStateException && cause != null) cause!! else this

    // The user sees a friendly message; logcat keeps the real cause
    // (e.g. FAILED_PRECONDITION "The query requires an index").
    Log.w("SGRMS", fallback, this)

    return when (error) {
        is FirebaseNetworkException ->
            "No internet connection. Please check your network and try again."

        is FirebaseTooManyRequestsException ->
            "Too many attempts. Please wait a moment and try again."

        is FirebaseAuthWeakPasswordException ->
            "Password is too weak. Use at least 6 characters."

        is FirebaseAuthUserCollisionException ->
            "This email is already used by another account."

        is FirebaseAuthInvalidUserException ->
            "This account does not exist or has been disabled."

        is FirebaseAuthInvalidCredentialsException ->
            "Email or password is incorrect."

        is FirebaseAuthRecentLoginRequiredException ->
            "Please log in again to continue."

        is FirebaseFirestoreException -> when (error.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "You do not have permission to do this."
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                "Service is unavailable. Please check your connection."
            FirebaseFirestoreException.Code.NOT_FOUND ->
                "The record no longer exists."
            else -> fallback
        }

        // Messages we throw ourselves are written for users.
        is IllegalStateException, is IllegalArgumentException ->
            error.message?.takeIf { it.isNotBlank() } ?: fallback

        else -> fallback
    }
}
