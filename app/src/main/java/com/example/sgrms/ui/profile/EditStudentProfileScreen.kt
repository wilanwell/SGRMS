package com.example.sgrms.ui.profile

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.firebase.StudentFirebaseService
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.ui.components.ProfileImagePicker
import com.example.sgrms.ui.components.SgrmsTextField
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.utils.convertImageUriToBase64
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun EditStudentProfileScreen(
    studentId: Int = 0,
    firebaseStudentId: String = "",
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val studentFirebaseService = remember {
        StudentFirebaseService()
    }

    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    val currentStudentId = firebaseStudentId.ifBlank {
        if (studentId != 0) {
            studentId.toString()
        } else {
            ""
        }
    }

    var student by remember {
        mutableStateOf<StudentModel?>(null)
    }

    var profileImageBase64 by remember {
        mutableStateOf("")
    }

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

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    var isUploadingImage by remember {
        mutableStateOf(false)
    }

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
        if (currentStudentId.isBlank()) {
            generalError = "Student profile not found."
            return
        }

        coroutineScope.launch {
            try {
                isUploadingImage = true
                generalError = ""

                firestore
                    .collection("students")
                    .document(currentStudentId)
                    .update(
                        mapOf(
                            "profileImageBase64" to "",
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()

                profileImageBase64 = ""

                snackbarHostState.showSnackbar(
                    message = "Profile picture removed successfully"
                )
            } catch (e: Exception) {
                generalError = e.message ?: "Failed to remove profile picture."
            } finally {
                isUploadingImage = false
            }
        }
    }

    LaunchedEffect(currentStudentId) {
        try {
            isLoading = true
            generalError = ""

            if (currentStudentId.isBlank()) {
                generalError = "Student profile not found."
                return@LaunchedEffect
            }

            val loadedStudent = studentFirebaseService.getStudentById(currentStudentId)
            student = loadedStudent

            if (loadedStudent == null) {
                generalError = "Student profile not found."
                return@LaunchedEffect
            }

            profileImageBase64 = loadedStudent.profileImageBase64
            phoneNumber = loadedStudent.phoneNumber
            dateOfBirth = loadedStudent.dateOfBirth
            address = loadedStudent.address
            guardianName = loadedStudent.guardianName
            guardianPhone = loadedStudent.guardianPhone
            emergencyContact = loadedStudent.emergencyContact
            profileNote = loadedStudent.profileNote
        } catch (e: Exception) {
            generalError = e.message ?: "Failed to load student profile."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrey)
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
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardWhite
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 3.dp
                )
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
                        color = TextGrey
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = PrimaryBlue
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Loading student profile...",
                            fontSize = 14.sp,
                            color = TextGrey
                        )
                    } else {
                        ProfileImagePicker(
                            imageBase64 = profileImageBase64,
                            isUploading = isUploadingImage,
                            enabled = !isSaving,
                            onImageSelected = { imageUri ->
                                if (currentStudentId.isBlank()) {
                                    generalError = "Student profile not found."
                                    return@ProfileImagePicker
                                }

                                coroutineScope.launch {
                                    try {
                                        isUploadingImage = true
                                        generalError = ""

                                        val convertedImageBase64 = convertImageUriToBase64(
                                            context = context,
                                            imageUri = imageUri
                                        )

                                        firestore
                                            .collection("students")
                                            .document(currentStudentId)
                                            .update(
                                                mapOf(
                                                    "profileImageBase64" to convertedImageBase64,
                                                    "updatedAt" to System.currentTimeMillis()
                                                )
                                            )
                                            .await()

                                        profileImageBase64 = convertedImageBase64

                                        snackbarHostState.showSnackbar(
                                            message = "Profile picture updated successfully"
                                        )
                                    } catch (e: Exception) {
                                        generalError = e.message ?: "Failed to save profile picture."
                                    } finally {
                                        isUploadingImage = false
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                removeProfilePicture()
                            },
                            enabled = profileImageBase64.isNotBlank() &&
                                    !isSaving &&
                                    !isUploadingImage,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryBlue
                            )
                        ) {
                            Text(
                                text = "Remove Picture",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (generalError.isNotBlank()) {
                            Text(
                                text = generalError,
                                color = Color.Red,
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

                        SgrmsTextField(
                            value = dateOfBirth.ifBlank {
                                "Select date of birth"
                            },
                            onValueChange = {},
                            label = "Date of Birth",
                            leadingIcon = Icons.Default.Badge,
                            readOnly = true,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                openDatePicker()
                            },
                            enabled = !isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryBlue
                            )
                        ) {
                            Text(
                                text = "Choose from Calendar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
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

                                if (currentStudentId.isBlank()) {
                                    generalError = "Student profile not found."
                                    return@Button
                                }

                                coroutineScope.launch {
                                    try {
                                        isSaving = true
                                        generalError = ""

                                        firestore
                                            .collection("students")
                                            .document(currentStudentId)
                                            .update(
                                                mapOf(
                                                    "phoneNumber" to phoneNumber.trim(),
                                                    "dateOfBirth" to dateOfBirth.trim(),
                                                    "address" to address.trim(),
                                                    "guardianName" to guardianName.trim(),
                                                    "guardianPhone" to guardianPhone.trim(),
                                                    "emergencyContact" to emergencyContact.trim(),
                                                    "profileNote" to profileNote.trim(),
                                                    "updatedAt" to System.currentTimeMillis()
                                                )
                                            )
                                            .await()

                                        snackbarHostState.showSnackbar(
                                            message = "Profile updated successfully"
                                        )

                                        onSaveSuccess()
                                    } catch (e: Exception) {
                                        generalError = e.message ?: "Failed to update profile."
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = !isSaving && !isLoading && !isUploadingImage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue
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
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryBlue
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

private fun formatStudentDateOfBirth(
    day: Int,
    month: Int,
    year: Int
): String {
    return String.format(
        Locale.getDefault(),
        "%02d/%02d/%04d",
        day,
        month + 1,
        year
    )
}

private fun parseStudentDateOfBirth(
    value: String
): Date? {
    return try {
        if (value.isBlank()) {
            null
        } else {
            val formatter = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )

            formatter.isLenient = false
            formatter.parse(value)
        }
    } catch (exception: Exception) {
        null
    }
}