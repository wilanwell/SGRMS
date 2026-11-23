package com.wilanwell.sgrms.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun validEmails() {
        assertTrue(isValidEmail("admin@sgrms.edu.my"))
        assertTrue(isValidEmail("  guru.ali@sekolah.com  "))
    }

    @Test
    fun invalidEmails() {
        assertFalse(isValidEmail(""))
        assertFalse(isValidEmail("admin"))
        assertFalse(isValidEmail("admin@"))
        assertFalse(isValidEmail("admin@sgrms"))
        assertFalse(isValidEmail("ad min@sgrms.com"))
    }
}
