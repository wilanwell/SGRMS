package com.wilanwell.sgrms.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Runs [onReturn] each time this screen becomes visible again, for example
 * after the user comes back from an add / edit screen, so it never shows
 * stale data. It does not run when the screen is first shown (the
 * ViewModel already loads then).
 */
@Composable
fun OnReturnToScreen(onReturn: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val latest by rememberUpdatedState(onReturn)

    DisposableEffect(lifecycleOwner) {
        var firstResume = true
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (firstResume) firstResume = false else latest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
