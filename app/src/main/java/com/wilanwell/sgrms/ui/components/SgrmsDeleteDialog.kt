package com.wilanwell.sgrms.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.SgrmsColors

@Composable
fun SgrmsDeleteDialog(
    title: String,
    message: String,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    confirmText: String = "Delete",
    dismissText: String = "Cancel"
) {
    AlertDialog(
        onDismissRequest = onDismissClick,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            Button(
                onClick = onConfirmClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SgrmsColors.Error
                )
            ) {
                Text(
                    text = confirmText,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissClick
            ) {
                Text(
                    text = dismissText,
                    color = ColorPrimary
                )
            }
        }
    )
}