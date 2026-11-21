package com.example.sgrms.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant

@Composable
fun SgrmsSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = ColorOnSurfaceVariant
            )
        }
    }
}