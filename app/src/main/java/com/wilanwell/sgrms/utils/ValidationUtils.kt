package com.wilanwell.sgrms.utils

/** Basic email shape check, also used by functions/src/index.ts. */
private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

fun isValidEmail(email: String): Boolean = EMAIL_PATTERN.matches(email.trim())
