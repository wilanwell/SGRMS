package com.wilanwell.sgrms.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.wilanwell.sgrms.utils.AndroidStrings
import com.wilanwell.sgrms.utils.Strings

/** [Strings] for composables that call plain Kotlin text builders (e.g. NotificationText). */
@Composable
fun rememberStrings(): Strings {
    val context = LocalContext.current
    return remember(context) { AndroidStrings(context) }
}
