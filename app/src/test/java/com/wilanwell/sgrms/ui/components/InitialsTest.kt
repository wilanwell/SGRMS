package com.wilanwell.sgrms.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class InitialsTest {

    @Test
    fun firstAndLastMeaningfulWords() {
        assertEquals("AA", initialsOf("Adam bin Anuar"))
        assertEquals("SA", initialsOf("System Administrator"))
    }

    @Test
    fun skipsMalayPatronymicWordsAndUsesFirstTwoNames() {
        assertEquals("SH", initialsOf("Sarah Hadizah binti Abdullah"))
        assertEquals("RK", initialsOf("Ravi a/l Kumar"))
    }

    @Test
    fun singleWordAndBlank() {
        assertEquals("A", initialsOf("arman"))
        assertEquals("?", initialsOf("  "))
    }

    @Test
    fun ignoresTitles() {
        assertEquals("AA", initialsOf("Mr. Abu bin Ali"))
    }
}
