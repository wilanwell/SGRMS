package com.example.sgrms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.PrimaryBlue
import androidx.compose.ui.graphics.Color

@Composable
fun SgrmsScreenLayout(
    title: String,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    showIcon: Boolean = true,
    icon: ImageVector? = null,
    floatingActionIcon: ImageVector? = null,
    onFloatingActionClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (icon != null) {
                TopBar(
                    title = title,
                    showBack = showBack,
                    onBackClick = onBackClick,
                    showIcon = showIcon,
                    icon = icon
                )
            } else {
                TopBar(
                    title = title,
                    showBack = showBack,
                    onBackClick = onBackClick,
                    showIcon = showIcon
                )
            }
        },
        floatingActionButton = {
            if (floatingActionIcon != null && onFloatingActionClick != null) {
                FloatingActionButton(
                    onClick = onFloatingActionClick,
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = floatingActionIcon,
                        contentDescription = title
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGrey)
                .navigationBarsPadding()
                .padding(paddingValues)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            content = content
        )
    }
}