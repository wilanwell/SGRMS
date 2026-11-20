package com.example.sgrms.ui.profile

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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
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
import com.example.sgrms.data.model.TeacherModel
import com.example.sgrms.ui.components.ProfileImagePicker
import com.example.sgrms.ui.components.SgrmsTextField
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.utils.convertImageUriToBase64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EditTeacherProfileScreen(
    teacherId: Int = 0,
    firebaseTeacherId: String = "",
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var teacher by remember { mutableStateOf<TeacherModel?>(null) }
    var resolvedTeacherId by remember { mutableStateOf("") }

    var profileImageBase64 by remember { mutableStateOf("") }

    var phoneNumber by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var officeLocation by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var profileNote by remember { mutableStateOf("") }

    var phoneNumberError by remember { mutableStateOf("") }
    var departmentError by remember { mutableStateOf("") }
    var positionError by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf("") }

    fun clearErrors() {
        phoneNumberError = ""
        departmentError = ""
        positionError = ""
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

        if (!isValidPhone(phoneNumber)) {
            phoneNumberError = "Enter a valid phone number"
        }

        if (department.isBlank()) {
            departmentError = "Department is required"
        }

        if (position.isBlank()) {
            positionError = "Position is required"
        }

        return phoneNumberError.isBlank() &&
                departmentError.isBlank() &&
                positionError.isBlank()
    }

    fun removeProfilePicture() {
        if (resolvedTeacherId.isBlank()) {
            generalError = "Teacher profile not found."
            return
        }

        coroutineScope.launch {
            try {
                isUploadingImage = true
                generalError = ""

                firestore
                    .collection("teachers")
                    .document(resolvedTeacherId)
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

    LaunchedEffect(firebaseTeacherId, teacherId) {
        try {
            isLoading = true
            generalError = ""

            val firebaseUser = firebaseAuth.currentUser

            if (firebaseUser == null) {
                generalError = "User session not found. Please log in again."
                return@LaunchedEffect
            }

            val userDocument = firestore
                .collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val teacherDocumentId = firebaseTeacherId.ifBlank {
                userDocument.getString("teacherId").orEmpty()
            }.ifBlank {
                if (teacherId != 0) teacherId.toString() else ""
            }

            val loadedTeacher = if (teacherDocumentId.isNotBlank()) {
                val teacherDocument = firestore
                    .collection("teachers")
                    .document(teacherDocumentId)
                    .get()
                    .await()

                resolvedTeacherId = teacherDocument.id

                teacherDocument.toObject(TeacherModel::class.java)?.copy(
                    teacherId = teacherDocument.id
                )
            } else {
                val teacherQuery = firestore
                    .collection("teachers")
                    .whereEqualTo("userId", firebaseUser.uid)
                    .limit(1)
                    .get()
                    .await()

                val document = teacherQuery.documents.firstOrNull()
                resolvedTeacherId = document?.id.orEmpty()

                document?.toObject(TeacherModel::class.java)?.copy(
                    teacherId = document.id
                )
            }

            teacher = loadedTeacher

            if (loadedTeacher == null || resolvedTeacherId.isBlank()) {
                generalError = "Teacher profile not found."
                return@LaunchedEffect
            }

            profileImageBase64 = loadedTeacher.profileImageBase64
            phoneNumber = loadedTeacher.phoneNumber
            department = loadedTeacher.department
            position = loadedTeacher.position.ifBlank { "Teacher" }
            officeLocation = loadedTeacher.officeLocation
            qualification = loadedTeacher.qualification
            specialization = loadedTeacher.specialization
            address = loadedTeacher.address
            profileNote = loadedTeacher.profileNote
        } catch (e: Exception) {
            generalError = e.message ?: "Failed to load teacher profile."
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
            title = "Edit Teacher Profile",
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
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Teacher Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Update contact, professional details, and profile picture",
                        fontSize = 14.sp,
                        color = TextGrey
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = PrimaryBlue)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Loading teacher profile...",
                            fontSize = 14.sp,
                            color = TextGrey
                        )
                    } else {
                        ProfileImagePicker(
                            imageBase64 = profileImageBase64,
                            isUploading = isUploadingImage,
                            enabled = !isSaving,
                            onImageSelected = { imageUri ->
                                if (resolvedTeacherId.isBlank()) {
                                    generalError = "Teacher profile not found."
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
                                            .collection("teachers")
                                            .document(resolvedTeacherId)
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
                            value = teacher?.teacherName ?: "",
                            onValueChange = {},
                            label = "Teacher Name",
                            leadingIcon = Icons.Default.Person,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = teacher?.staffId ?: "",
                            onValueChange = {},
                            label = "Staff ID",
                            leadingIcon = Icons.Default.Badge,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = teacher?.email ?: "",
                            onValueChange = {},
                            label = "Email",
                            leadingIcon = Icons.Default.Badge,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        Text(
                            text = "Contact Details",
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
                            text = "Professional Details",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = department,
                            onValueChange = {
                                department = it
                                departmentError = ""
                                generalError = ""
                            },
                            label = "Department",
                            leadingIcon = Icons.Default.School,
                            isError = departmentError.isNotBlank(),
                            errorMessage = departmentError,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = position,
                            onValueChange = {
                                position = it
                                positionError = ""
                                generalError = ""
                            },
                            label = "Position",
                            leadingIcon = Icons.Default.Work,
                            isError = positionError.isNotBlank(),
                            errorMessage = positionError,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = officeLocation,
                            onValueChange = {
                                officeLocation = it
                                clearErrors()
                            },
                            label = "Office Location",
                            leadingIcon = Icons.Default.LocationOn,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = qualification,
                            onValueChange = {
                                qualification = it
                                clearErrors()
                            },
                            label = "Qualification",
                            leadingIcon = Icons.Default.Work,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = specialization,
                            onValueChange = {
                                specialization = it
                                clearErrors()
                            },
                            label = "Specialization",
                            leadingIcon = Icons.Default.MenuBook,
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
                                if (!validateForm()) return@Button

                                if (resolvedTeacherId.isBlank()) {
                                    generalError = "Teacher profile not found."
                                    return@Button
                                }

                                coroutineScope.launch {
                                    try {
                                        isSaving = true
                                        generalError = ""

                                        firestore
                                            .collection("teachers")
                                            .document(resolvedTeacherId)
                                            .update(
                                                mapOf(
                                                    "phoneNumber" to phoneNumber.trim(),
                                                    "department" to department.trim(),
                                                    "position" to position.trim(),
                                                    "officeLocation" to officeLocation.trim(),
                                                    "qualification" to qualification.trim(),
                                                    "specialization" to specialization.trim(),
                                                    "address" to address.trim(),
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