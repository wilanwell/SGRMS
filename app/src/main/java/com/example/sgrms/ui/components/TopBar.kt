package com.example.sgrms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.theme.PrimaryBlue

@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    showIcon: Boolean = true,
    icon: ImageVector = Icons.Default.School,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PrimaryBlue,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlue)
                .windowInsetsPadding(WindowInsets.statusBars)
                .heightIn(min = 64.dp)
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = 6.dp,
                    bottom = 10.dp
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBack) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                } else if (showIcon) {
                    Spacer(modifier = Modifier.width(10.dp))

                    Icon(
                        imageVector = icon,
                        contentDescription = "$title Icon",
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(14.dp))
                } else {
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }
    }
}