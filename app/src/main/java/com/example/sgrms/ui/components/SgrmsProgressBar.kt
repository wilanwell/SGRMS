package com.example.sgrms.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.ui.theme.ColorPrimary

@Composable
fun SgrmsProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = {
            progress.coerceIn(0f, 1f)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
        color = ColorPrimary,
        trackColor = ColorBackground
    )
}