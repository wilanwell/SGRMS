package com.wilanwell.sgrms.ui.login

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import com.wilanwell.sgrms.viewmodel.ForgotPasswordViewModel
import com.wilanwell.sgrms.viewmodel.PasswordResetState
import com.wilanwell.sgrms.ui.theme.SgrmsRadius
import com.wilanwell.sgrms.ui.components.sgrmsFieldColors
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Sends a Firebase password reset link to the entered email.
 * [initialEmail] is pre-filled from the login form.
 */
@Composable
fun ForgotPasswordDialog(
    initialEmail: String,
    onDismiss: () -> Unit
) {
    val viewModel: ForgotPasswordViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    var email by remember { mutableStateOf(initialEmail.trim()) }
    val isSending = state is PasswordResetState.Sending

    fun close() {
        viewModel.reset()
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = { if (!isSending) close() },
        title = {
            Text(text = stringResource(R.string.reset_password), fontWeight = FontWeight.Bold)
        },
        text = {
            when (val current = state) {
                is PasswordResetState.Sent -> Text(
                    text = stringResource(R.string.password_reset_link_sent, current.email),
                    fontSize = 14.sp,
                    color = SgrmsColors.Success
                )

                else -> Column {
                    Text(
                        text = stringResource(R.string.enter_the_email_you_use_to),
                        fontSize = 14.sp,
                        color = ColorOnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val errorText = when (current) {
                        is PasswordResetState.InvalidEmail -> current.message
                        is PasswordResetState.Error -> current.message
                        else -> null
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (state !is PasswordResetState.Idle) viewModel.reset()
                        },
                        label = { Text(stringResource(R.string.email)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        enabled = !isSending,
                        isError = errorText != null,
                        supportingText = errorText?.let { message ->
                            { Text(text = message, color = SgrmsColors.Error) }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { viewModel.sendResetEmail(email) }
                        ),
                        shape = RoundedCornerShape(SgrmsRadius.Field),
                        colors = sgrmsFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (state is PasswordResetState.Sent) {
                TextButton(onClick = { close() }) {
                    Text(text = stringResource(R.string.ok), color = ColorPrimary, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { viewModel.sendResetEmail(email) },
                    enabled = !isSending,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                    shape = RoundedCornerShape(SgrmsRadius.Field)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(text = stringResource(R.string.send_link))
                    }
                }
            }
        },
        dismissButton = {
            if (state !is PasswordResetState.Sent) {
                TextButton(onClick = { close() }, enabled = !isSending) {
                    Text(text = stringResource(R.string.cancel), color = ColorOnSurfaceVariant)
                }
            }
        }
    )
}
