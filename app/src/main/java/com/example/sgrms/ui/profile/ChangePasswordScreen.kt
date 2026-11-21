package com.example.sgrms.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.ui.theme.ColorSurface
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.SgrmsColors
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.example.sgrms.viewmodel.ChangePasswordResult
import com.example.sgrms.viewmodel.ChangePasswordViewModel

@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val viewModel: ChangePasswordViewModel = hiltViewModel()
    val result by viewModel.result.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var currentPasswordError by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    var successMessage by remember { mutableStateOf("") }
    var generalErrorMessage by remember { mutableStateOf("") }

    var showConfirmDialog by remember { mutableStateOf(false) }
    val isLoading = result is ChangePasswordResult.Loading

    fun clearMessages() {
        successMessage = ""
        generalErrorMessage = ""
    }

    fun validateInput(): Boolean {
        currentPasswordError = ""
        newPasswordError = ""
        confirmPasswordError = ""
        clearMessages()


        if (currentPassword.isBlank()) {
            currentPasswordError = "Current password is required"
        }

        if (newPassword.isBlank()) {
            newPasswordError = "New password is required"
        } else if (newPassword.length < 6) {
            newPasswordError = "Password must be at least 6 characters"
        } else if (newPassword == currentPassword) {
            newPasswordError = "New password must be different"
        }

        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Please confirm your new password"
        } else if (confirmPassword != newPassword) {
            confirmPasswordError = "Passwords do not match"
        }

        return currentPasswordError.isBlank() &&
                newPasswordError.isBlank() &&
                confirmPasswordError.isBlank() &&
                generalErrorMessage.isBlank()
    }

    fun updatePasswordWithFirebase() {
        viewModel.changePassword(
            currentPassword = currentPassword,
            newPassword = newPassword.trim()
        )
    }

    LaunchedEffect(result) {
        when (val state = result) {
            ChangePasswordResult.Success -> {
                showConfirmDialog = false
                successMessage = "Password updated successfully"
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                viewModel.reset()
                onSaveClick()
            }

            ChangePasswordResult.WrongCurrentPassword -> {
                showConfirmDialog = false
                currentPasswordError = "Current password is incorrect"
                viewModel.reset()
            }

            is ChangePasswordResult.Error -> {
                showConfirmDialog = false
                generalErrorMessage = state.message
                viewModel.reset()
            }

            else -> Unit
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isLoading) {
                    showConfirmDialog = false
                }
            },
            title = {
                Text(
                    text = "Confirm Password Change",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to change your password?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        updatePasswordWithFirebase()
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Confirm",
                            color = Color.White
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                    },
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Cancel",
                        color = ColorPrimary
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .navigationBarsPadding()
    ) {
        TopBar(
            title = "Change Password",
            showBack = true,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ColorSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = ColorPrimary
                        )

                        Spacer(modifier = Modifier.padding(5.dp))

                        Column {
                            Text(
                                text = "Update Password",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Enter your current and new password",
                                fontSize = 14.sp,
                                color = ColorOnSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    PasswordInputField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            currentPasswordError = ""
                            clearMessages()
                        },
                        label = "Current Password",
                        visible = currentVisible,
                        onVisibilityClick = {
                            currentVisible = !currentVisible
                        },
                        isError = currentPasswordError.isNotBlank(),
                        errorMessage = currentPasswordError,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PasswordInputField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            newPasswordError = ""
                            clearMessages()
                        },
                        label = "New Password",
                        visible = newVisible,
                        onVisibilityClick = {
                            newVisible = !newVisible
                        },
                        isError = newPasswordError.isNotBlank(),
                        errorMessage = newPasswordError,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PasswordInputField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError = ""
                            clearMessages()
                        },
                        label = "Confirm Password",
                        visible = confirmVisible,
                        onVisibilityClick = {
                            confirmVisible = !confirmVisible
                        },
                        isError = confirmPasswordError.isNotBlank(),
                        errorMessage = confirmPasswordError,
                        enabled = !isLoading
                    )

                    if (generalErrorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = generalErrorMessage,
                            color = SgrmsColors.Error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (successMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = successMessage,
                            color = SgrmsColors.Success,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onBackClick,
                            enabled = !isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                color = ColorPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                if (validateInput()) {
                                    showConfirmDialog = true
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorPrimary
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Save",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

@Composable
fun PasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onVisibilityClick: () -> Unit,
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = {
            Text(label)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = label
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onVisibilityClick,
                enabled = enabled
            ) {
                Icon(
                    imageVector = if (visible) {
                        Icons.Default.Visibility
                    } else {
                        Icons.Default.VisibilityOff
                    },
                    contentDescription = "Password Visibility"
                )
            }
        },
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        singleLine = true,
        isError = isError,
        supportingText = {
            if (isError && errorMessage.isNotBlank()) {
                Text(text = errorMessage)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ColorPrimary,
            focusedLabelColor = ColorPrimary,
            cursorColor = ColorPrimary,
            errorBorderColor = SgrmsColors.Error,
            errorLabelColor = SgrmsColors.Error,
            errorCursorColor = SgrmsColors.Error
        )
    )
}