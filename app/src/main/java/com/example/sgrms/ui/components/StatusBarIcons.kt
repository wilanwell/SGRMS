package com.example.sgrms.ui.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Use dark status bar icons while this composable is shown, for screens
 * with a light background behind the status bar (most screens have the
 * blue TopBar there and keep the default light icons).
 */
@Composable
fun DarkStatusBarIcons() {
    val view = LocalView.current
    if (view.isInEditMode) return

    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars

        controller?.isAppearanceLightStatusBars = true

        onDispose {
            if (previous != null) controller.isAppearanceLightStatusBars = previous
        }
    }
}
