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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun EditAdminProfileScreen(
    userId: Int = 0,
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var resolvedUserId by remember { mutableStateOf("") }

    var profileImageBase64 by remember { mutableStateOf("") }

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

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
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
            fullNameError = "Full name is required"
        }

        if (username.isBlank()) {
            usernameError = "Username is required"
        }

        if (!isValidPhone(phoneNumber)) {
            phoneNumberError = "Enter a valid phone number"
        }

        return fullNameError.isBlank() &&
                usernameError.isBlank() &&
                phoneNumberError.isBlank()
    }

    fun removeProfilePicture() {
        if (resolvedUserId.isBlank()) {
            generalError = "User profile not found."
            return
        }

        coroutineScope.launch {
            try {
                isUploadingImage = true
                generalError = ""

                firestore
                    .collection("users")
                    .document(resolvedUserId)
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

    LaunchedEffect(userId) {
        try {
            isLoading = true
            generalError = ""

            val firebaseUser = firebaseAuth.currentUser

            if (firebaseUser == null) {
                generalError = "User session not found. Please log in again."
                return@LaunchedEffect
            }

            resolvedUserId = firebaseUser.uid

            val userDocument = firestore
                .collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (!userDocument.exists()) {
                fullName = firebaseUser.displayName ?: "Admin Account"
                username = firebaseUser.email.orEmpty()
                email = firebaseUser.email.orEmpty()
                phoneNumber = ""
                address = ""
                profileNote = ""
                profileImageBase64 = ""
                role = "Admin"
                status = "Active"
                return@LaunchedEffect
            }

            fullName = userDocument.getString("fullName")
                ?: firebaseUser.displayName
                        ?: "Admin Account"

            username = userDocument.getString("username")
                ?: userDocument.getString("staffId")
                        ?: firebaseUser.email.orEmpty()

            email = userDocument.getString("email")
                ?: firebaseUser.email.orEmpty()

            phoneNumber = userDocument.getString("phoneNumber").orEmpty()
            address = userDocument.getString("address").orEmpty()
            profileNote = userDocument.getString("profileNote").orEmpty()
            profileImageBase64 = userDocument.getString("profileImageBase64").orEmpty()
            role = userDocument.getString("role") ?: "Admin"
            status = userDocument.getString("status") ?: "Active"
        } catch (e: Exception) {
            generalError = e.message ?: "Failed to load admin profile."
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
            title = "Edit Admin Profile",
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
                        text = "Admin Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Update account, contact, and profile picture",
                        fontSize = 14.sp,
                        color = TextGrey
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = PrimaryBlue)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Loading admin profile...",
                            fontSize = 14.sp,
                            color = TextGrey
                        )
                    } else {
                        ProfileImagePicker(
                            imageBase64 = profileImageBase64,
                            isUploading = isUploadingImage,
                            enabled = !isSaving,
                            onImageSelected = { imageUri ->
                                if (resolvedUserId.isBlank()) {
                                    generalError = "User profile not found."
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
                                            .collection("users")
                                            .document(resolvedUserId)
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
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                fullNameError = ""
                                generalError = ""
                            },
                            label = "Full Name",
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
                            label = "Username",
                            leadingIcon = Icons.Default.Badge,
                            isError = usernameError.isNotBlank(),
                            errorMessage = usernameError,
                            enabled = !isSaving
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = email,
                            onValueChange = {},
                            label = "Email",
                            leadingIcon = Icons.Default.Email,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = role,
                            onValueChange = {},
                            label = "Role",
                            leadingIcon = Icons.Default.AdminPanelSettings,
                            enabled = false
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsTextField(
                            value = status,
                            onValueChange = {},
                            label = "Status",
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

                                if (resolvedUserId.isBlank()) {
                                    generalError = "User profile not found."
                                    return@Button
                                }

                                coroutineScope.launch {
                                    try {
                                        isSaving = true
                                        generalError = ""

                                        firestore
                                            .collection("users")
                                            .document(resolvedUserId)
                                            .update(
                                                mapOf(
                                                    "fullName" to fullName.trim(),
                                                    "username" to username.trim(),
                                                    "phoneNumber" to phoneNumber.trim(),
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