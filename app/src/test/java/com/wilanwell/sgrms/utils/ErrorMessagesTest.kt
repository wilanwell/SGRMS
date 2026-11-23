package com.wilanwell.sgrms.utils

import com.wilanwell.sgrms.R
import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorMessagesTest {

    @Test
    fun userMessageException_showsItsOwnMessage() {
        assertEquals(
            R.string.error_login_again,
            userMessageRes(UserMessageException(R.string.error_login_again), fallback = R.string.error_generic)
        )
    }

    @Test
    fun unknownError_usesTheFallback() {
        assertEquals(R.string.error_generic, userMessageRes(RuntimeException("boom"), fallback = R.string.error_generic))
    }

    @Test
    fun plainIllegalStateMessage_isNotShownToUsers() {
        assertEquals(
            R.string.error_generic,
            userMessageRes(IllegalStateException("PERMISSION_DENIED: raw SDK text"), fallback = R.string.error_generic)
        )
    }
}
