package com.example.sgrms.utils

/** Same rule as the Cloud Functions (functions/src/index.ts EMAIL_PATTERN). */
private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

fun isValidEmail(email: String): Boolean = EMAIL_PATTERN.matches(email.trim())
