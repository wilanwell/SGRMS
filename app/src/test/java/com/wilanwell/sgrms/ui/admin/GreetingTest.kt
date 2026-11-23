package com.wilanwell.sgrms.ui.admin

import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {

    @Test
    fun boundaries() {
        assertEquals("Good evening", greetingFor(4))
        assertEquals("Good morning", greetingFor(5))
        assertEquals("Good morning", greetingFor(11))
        assertEquals("Good afternoon", greetingFor(12))
        assertEquals("Good afternoon", greetingFor(17))
        assertEquals("Good evening", greetingFor(18))
        assertEquals("Good evening", greetingFor(0))
    }
}
