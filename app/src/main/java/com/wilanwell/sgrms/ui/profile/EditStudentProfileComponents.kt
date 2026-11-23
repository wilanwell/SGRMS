package com.wilanwell.sgrms.ui.profile

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun formatStudentDateOfBirth(
    day: Int,
    month: Int,
    year: Int
): String {
    return String.format(
        Locale.getDefault(),
        "%02d/%02d/%04d",
        day,
        month + 1,
        year
    )
}

internal fun parseStudentDateOfBirth(
    value: String
): Date? {
    return try {
        if (value.isBlank()) {
            null
        } else {
            val formatter = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )

            formatter.isLenient = false
            formatter.parse(value)
        }
    } catch (exception: Exception) {
        null
    }
}
