package com.wilanwell.sgrms.ui.profile

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
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
import com.wilanwell.sgrms.ui.components.TopBar
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.ui.theme.ColorSurface
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.wilanwell.sgrms.viewmodel.ChangePasswordResult
import com.wilanwell.sgrms.viewmodel.ChangePasswordViewModel
import com.wilanwell.sgrms.ui.theme.SgrmsRadius

@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val currentPasswordIsIncorrectText = stringResource(R.string.current_password_is_incorrect)
    val currentPasswordIsRequiredText = stringResource(R.string.current_password_is_required)
    val newPasswordIsRequiredText = stringResource(R.string.new_password_is_required)
    val newPasswordMustBeDifferentText = stringResource(R.string.new_password_must_be_different)
    val passwordMustBeAtLeast6Text = stringResource(R.string.password_must_be_at_least_6_characters)
    val passwordUpdatedSuccessfullyText = stringResource(R.string.password_updated_successfully)
    val passwordsDoNotMatchText = stringResource(R.string.passwords_do_not_match)
    val pleaseConfirmYourNewPasswordText = stringResource(R.string.please_confirm_your_new_password)
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
            currentPasswordError = currentPasswordIsRequiredText
        }

        if (newPassword.isBlank()) {
            newPasswordError = newPasswordIsRequiredText
        } else if (newPassword.length < 6) {
            newPasswordError = passwordMustBeAtLeast6Text
        } else if (newPassword == currentPassword) {
            newPasswordError = newPasswordMustBeDifferentText
        }

        if (confirmPassword.isBlank()) {
            confirmPasswordError = pleaseConfirmYourNewPasswordText
        } else if (confirmPassword != newPassword) {
            confirmPasswordError = passwordsDoNotMatchText
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
                successMessage = passwordUpdatedSuccessfullyText
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                viewModel.reset()
                onSaveClick()
            }

            ChangePasswordResult.WrongCurrentPassword -> {
                showConfirmDialog = false
                currentPasswordError = currentPasswordIsIncorrectText
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
                    text = stringResource(R.string.confirm_password_change),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.are_you_sure_you_want_to_change)
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
                            text = stringResource(R.string.confirm),
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
                        text = stringResource(R.string.cancel),
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
            title = stringResource(R.string.change_password_title),
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
                shape = RoundedCornerShape(SgrmsRadius.Card),
                colors = CardDefaults.cardColors(containerColor = ColorSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(R.string.password_icon),
                            tint = ColorPrimary
                        )

                        Spacer(modifier = Modifier.padding(5.dp))

                        Column {
                            Text(
                                text = stringResource(R.string.update_password),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = stringResource(R.string.enter_your_current_and_new_password),
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
                        label = stringResource(R.string.current_password),
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
                        label = stringResource(R.string.new_password),
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
                        label = stringResource(R.string.confirm_password),
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
                                text = stringResource(R.string.cancel),
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
                            shape = RoundedCornerShape(SgrmsRadius.Field),
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
                                    text = stringResource(R.string.save),
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
                    contentDescription = stringResource(R.string.password_visibility)
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
        shape = RoundedCornerShape(SgrmsRadius.Field),
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