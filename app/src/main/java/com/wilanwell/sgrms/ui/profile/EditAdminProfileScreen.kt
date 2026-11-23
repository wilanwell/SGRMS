package com.wilanwell.sgrms.ui.profile

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilanwell.sgrms.ui.components.ProfileImagePicker
import com.wilanwell.sgrms.ui.components.SgrmsTextField
import com.wilanwell.sgrms.ui.components.TopBar
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.ui.theme.ColorSurface
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.wilanwell.sgrms.viewmodel.AdminProfileViewModel
import com.wilanwell.sgrms.data.repository.UserProfileUpdate
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.SgrmsRadius

@Composable
fun EditAdminProfileScreen(
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val enterAValidPhoneNumberText = stringResource(R.string.enter_a_valid_phone_number)
    val fullNameIsRequiredText = stringResource(R.string.full_name_is_required)
    val usernameIsRequiredText = stringResource(R.string.username_is_required)
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: AdminProfileViewModel = hiltViewModel()
    val uiState by viewModel.profileUiState.collectAsState()

    val profileImageUrl = uiState.profile?.profileImageUrl.orEmpty()
    val legacyImageBase64 = uiState.profile?.profileImageBase64.orEmpty()

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var profileNote by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Admin") }
    var status by remember { mutableStateOf("Active") }

    var fullNameError by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf("") }

    val isLoading = uiState.isLoading
    val isSaving = uiState.isSaving
    val isUploadingImage = uiState.isUploadingImage
    var generalError by remember { mutableStateOf("") }

    fun clearErrors() {
        fullNameError = ""
        usernameError = ""
        phoneNumberError = ""
        generalError = ""
    }

    fun isValidPhone(value: String): Boolean {
        if (value.isBlank()) return true

        return value.length in 7..15 &&
                value.all { character ->
                    character.isDigit() || character == '+' || character == '-' || character == ' '
                }
    }

    fun validateForm(): Boolean {
        clearErrors()

        if (fullName.isBlank()) {
            fullNameError = fullNameIsRequiredText
        }

        if (username.isBlank()) {
            usernameError = usernameIsRequiredText
        }

        if (!isValidPhone(phoneNumber)) {
            phoneNumberError = enterAValidPhoneNumberText
        }

        return fullNameError.isBlank() &&
                usernameError.isBlank() &&
                phoneNumberError.isBlank()
    }

    fun removeProfilePicture() {
        viewModel.removeImage()
    }

    var formFilled by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.profile) {
        val loaded = uiState.profile
        if (loaded != null && !formFilled) {
            fullName = loaded.fullName
            username = loaded.username.ifBlank { loaded.email }
            email = loaded.email
            phoneNumber = loaded.phoneNumber
            address = loaded.address
            profileNote = loaded.profileNote
            role = loaded.role.ifBlank { "Admin" }
            status = loaded.status.ifBlank { "Active" }
            formFilled = true
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotBlank()) generalError = uiState.errorMessage
    }

    LaunchedEffect(uiState.message) {
        if (uiState.message.isNotBlank()) {
            snackbarHostState.showSnackbar(message = uiState.message)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onSaveSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .navigationBarsPadding()
    ) {
        TopBar(
            title = stringResource(R.string.edit_admin_profile),
            showBack = true,
            onBackClick = onBackClick
        )

        SnackbarHost(hostState = snackbarHostState)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 32.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SgrmsRadius.Card),
                colors = CardDefaults.cardColors(containerColor = ColorSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.admin_information),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(R.string.update_account_contact_and_profile_picture),
                        fontSize = 14.sp,
                        color = ColorOnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = ColorPrimary)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.loading_admin_profile),
                            fontSize = 14.sp,
                            color = ColorOnSurfaceVariant
                        )
                    } else {
                        ProfileImagePicker(
                            imageUrl = profileImageUrl,
                            legacyImageBase64 = legacyImageBase64,
                            isUploading = isUploadingImage,
                            enabled = !isSaving,
                            onImageSelected = { imageUri ->
                                viewModel.onImageSelected(imageUri)
                            }
                        )

                        if (profileImageUrl.isNotBlank() || legacyImageBase64.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = {
                                    removeProfilePicture()
                                },
                                enabled = !isSaving &&
                                        !isUploadingImage,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(SgrmsRadius.Field),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ColorPrimary
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.remove_picture),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (generalError.isNotBlank()) {
                            Text(
                                text = generalError,
                                color = SgrmsColors.Error,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        SgrmsTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                fullNameError = ""
                                generalError = ""
                            },
                            label = stringResource(R.string.full_name),
                            leadingIcon = Icons.Default.Person,
                            isError = fullNameError.isNotBlank(),
                            errorMessage = fullNameError,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                usernameError = ""
                                generalError = ""
                            },
                            label = stringResource(R.string.username),
                            leadingIcon = Icons.Default.Badge,
                            isError = usernameError.isNotBlank(),
                            errorMessage = usernameError,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = email,
                            onValueChange = {},
                            label = stringResource(R.string.email),
                            leadingIcon = Icons.Default.Email,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = role,
                            onValueChange = {},
                            label = stringResource(R.string.role),
                            leadingIcon = Icons.Default.AdminPanelSettings,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = status,
                            onValueChange = {},
                            label = stringResource(R.string.status),
                            leadingIcon = Icons.Default.Badge,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        Text(
                            text = stringResource(R.string.contact_details),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                phoneNumberError = ""
                                generalError = ""
                            },
                            label = stringResource(R.string.phone_number),
                            leadingIcon = Icons.Default.Phone,
                            isError = phoneNumberError.isNotBlank(),
                            errorMessage = phoneNumberError,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = address,
                            onValueChange = {
                                address = it
                                clearErrors()
                            },
                            label = stringResource(R.string.address),
                            leadingIcon = Icons.Default.Home,
                            singleLine = false,
                            minLines = 2,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = profileNote,
                            onValueChange = {
                                profileNote = it
                                clearErrors()
                            },
                            label = stringResource(R.string.profile_note),
                            leadingIcon = Icons.Default.Note,
                            singleLine = false,
                            minLines = 3,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (!validateForm()) return@Button

                                viewModel.saveProfile(
                                    UserProfileUpdate(
                                        fullName = fullName.trim(),
                                        username = username.trim(),
                                        phoneNumber = phoneNumber.trim(),
                                        address = address.trim(),
                                        profileNote = profileNote.trim()
                                    )
                                )
                            },
                            enabled = !isSaving && !isLoading && !isUploadingImage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(SgrmsRadius.Field),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorPrimary
                            )
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.save_changes),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onBackClick,
                            enabled = !isSaving && !isUploadingImage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(SgrmsRadius.Field),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ColorPrimary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}