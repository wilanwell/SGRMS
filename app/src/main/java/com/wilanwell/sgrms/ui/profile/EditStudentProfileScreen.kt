package com.wilanwell.sgrms.ui.profile

import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Emergency
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
import java.util.Calendar
import java.util.Date
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.wilanwell.sgrms.viewmodel.StudentProfileViewModel
import com.wilanwell.sgrms.data.repository.StudentProfileUpdate
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.SgrmsRadius

@Composable
fun EditStudentProfileScreen(
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: StudentProfileViewModel = hiltViewModel()
    val uiState by viewModel.profileUiState.collectAsState()
    val student = uiState.profile


    val profileImageUrl = uiState.profile?.profileImageUrl.orEmpty()
    val legacyImageBase64 = uiState.profile?.profileImageBase64.orEmpty()

    var phoneNumber by remember {
        mutableStateOf("")
    }

    var dateOfBirth by remember {
        mutableStateOf("")
    }

    var address by remember {
        mutableStateOf("")
    }

    var guardianName by remember {
        mutableStateOf("")
    }

    var guardianPhone by remember {
        mutableStateOf("")
    }

    var emergencyContact by remember {
        mutableStateOf("")
    }

    var profileNote by remember {
        mutableStateOf("")
    }

    var phoneNumberError by remember {
        mutableStateOf("")
    }

    var guardianPhoneError by remember {
        mutableStateOf("")
    }

    var emergencyContactError by remember {
        mutableStateOf("")
    }

    val isLoading = uiState.isLoading

    val isSaving = uiState.isSaving

    val isUploadingImage = uiState.isUploadingImage

    var generalError by remember {
        mutableStateOf("")
    }

    fun clearErrors() {
        phoneNumberError = ""
        guardianPhoneError = ""
        emergencyContactError = ""
        generalError = ""
    }

    fun isValidPhone(
        value: String
    ): Boolean {
        if (value.isBlank()) {
            return true
        }

        return value.length in 7..15 &&
                value.all { character ->
                    character.isDigit() ||
                            character == '+' ||
                            character == '-' ||
                            character == ' '
                }
    }

    fun validateForm(): Boolean {
        clearErrors()

        if (!isValidPhone(phoneNumber)) {
            phoneNumberError = "Enter a valid phone number"
        }

        if (!isValidPhone(guardianPhone)) {
            guardianPhoneError = "Enter a valid guardian phone number"
        }

        if (!isValidPhone(emergencyContact)) {
            emergencyContactError = "Enter a valid emergency contact"
        }

        return phoneNumberError.isBlank() &&
                guardianPhoneError.isBlank() &&
                emergencyContactError.isBlank()
    }

    fun openDatePicker() {
        val calendar = Calendar.getInstance()

        val existingDate = parseStudentDateOfBirth(dateOfBirth)
        if (existingDate != null) {
            calendar.time = existingDate
        }

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                dateOfBirth = formatStudentDateOfBirth(
                    day = selectedDay,
                    month = selectedMonth,
                    year = selectedYear
                )

                clearErrors()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    fun removeProfilePicture() {
        viewModel.removeImage()
    }

    var formFilled by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.profile) {
        val loaded = uiState.profile
        if (loaded != null && !formFilled) {
            phoneNumber = loaded.phoneNumber
            dateOfBirth = loaded.dateOfBirth
            address = loaded.address
            guardianName = loaded.guardianName
            guardianPhone = loaded.guardianPhone
            emergencyContact = loaded.emergencyContact
            profileNote = loaded.profileNote
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
            title = "Edit Student Profile",
            showBack = true,
            onBackClick = onBackClick
        )

        SnackbarHost(
            hostState = snackbarHostState
        )

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
                colors = CardDefaults.cardColors(
                    containerColor = ColorSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Profile Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Update contact, guardian, and profile picture",
                        fontSize = 14.sp,
                        color = ColorOnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = ColorPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Loading student profile...",
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
                                    text = "Remove Picture",
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
                            value = student?.studentName ?: "",
                            onValueChange = {},
                            label = "Student Name",
                            leadingIcon = Icons.Default.Person,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = student?.studentCode ?: "",
                            onValueChange = {},
                            label = "Student ID",
                            leadingIcon = Icons.Default.Badge,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = student?.className ?: "",
                            onValueChange = {},
                            label = "Class",
                            leadingIcon = Icons.Default.Badge,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        Text(
                            text = "Personal Details",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // The whole field opens the calendar; no separate button.
                        Box(modifier = Modifier.fillMaxWidth()) {
                            SgrmsTextField(
                                value = dateOfBirth,
                                onValueChange = {},
                                label = "Date of Birth",
                                placeholder = "Tap to choose a date",
                                leadingIcon = Icons.Default.CalendarMonth,
                                readOnly = true
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(enabled = !isSaving) { openDatePicker() }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                phoneNumberError = ""
                                generalError = ""
                            },
                            label = "Phone Number",
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
                            label = "Address",
                            leadingIcon = Icons.Default.Home,
                            singleLine = false,
                            minLines = 2,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        Text(
                            text = "Guardian Details",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = guardianName,
                            onValueChange = {
                                guardianName = it
                                clearErrors()
                            },
                            label = "Guardian Name",
                            leadingIcon = Icons.Default.Person,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = guardianPhone,
                            onValueChange = {
                                guardianPhone = it
                                guardianPhoneError = ""
                                generalError = ""
                            },
                            label = "Guardian Phone",
                            leadingIcon = Icons.Default.Phone,
                            isError = guardianPhoneError.isNotBlank(),
                            errorMessage = guardianPhoneError,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = emergencyContact,
                            onValueChange = {
                                emergencyContact = it
                                emergencyContactError = ""
                                generalError = ""
                            },
                            label = "Emergency Contact",
                            leadingIcon = Icons.Default.Emergency,
                            isError = emergencyContactError.isNotBlank(),
                            errorMessage = emergencyContactError,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = profileNote,
                            onValueChange = {
                                profileNote = it
                                clearErrors()
                            },
                            label = "Profile Note",
                            leadingIcon = Icons.Default.Note,
                            singleLine = false,
                            minLines = 3,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (!validateForm()) {
                                    return@Button
                                }

                                viewModel.saveProfile(
                                    StudentProfileUpdate(
                                        phoneNumber = phoneNumber.trim(),
                                        dateOfBirth = dateOfBirth.trim(),
                                        address = address.trim(),
                                        guardianName = guardianName.trim(),
                                        guardianPhone = guardianPhone.trim(),
                                        emergencyContact = emergencyContact.trim(),
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
                                    text = "Save Changes",
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
                                text = "Cancel",
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
