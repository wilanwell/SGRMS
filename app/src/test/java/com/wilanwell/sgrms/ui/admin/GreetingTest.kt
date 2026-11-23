package com.wilanwell.sgrms.ui.admin

import com.wilanwell.sgrms.R
import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {

    @Test
    fun boundaries() {
        assertEquals(R.string.good_evening, greetingFor(4))
        assertEquals(R.string.good_morning, greetingFor(5))
        assertEquals(R.string.good_morning, greetingFor(11))
        assertEquals(R.string.good_afternoon, greetingFor(12))
        assertEquals(R.string.good_afternoon, greetingFor(17))
        assertEquals(R.string.good_evening, greetingFor(18))
        assertEquals(R.string.good_evening, greetingFor(0))
    }
}
