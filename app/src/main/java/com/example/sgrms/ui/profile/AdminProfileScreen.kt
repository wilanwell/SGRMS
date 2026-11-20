package com.example.sgrms.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.components.ProfileImageView
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun AdminProfileScreen(
    userId: Int = 0,
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var profileImageBase64 by remember { mutableStateOf("") }

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var profileNote by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Admin") }
    var status by remember { mutableStateOf("Active") }

    var userCount by remember { mutableIntStateOf(0) }
    var teacherCount by remember { mutableIntStateOf(0) }
    var studentCount by remember { mutableIntStateOf(0) }
    var classCount by remember { mutableIntStateOf(0) }
    var subjectCount by remember { mutableIntStateOf(0) }

    var adminCount by remember { mutableIntStateOf(0) }
    var teacherAccountCount by remember { mutableIntStateOf(0) }
    var studentAccountCount by remember { mutableIntStateOf(0) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var profileFound by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        try {
            isLoading = true
            errorMessage = ""

            val firebaseUser = firebaseAuth.currentUser

            if (firebaseUser == null) {
                profileFound = false
                errorMessage = "User session not found. Please log in again."
                return@LaunchedEffect
            }

            val currentUserId = firebaseUser.uid

            val userDocument = firestore
                .collection("users")
                .document(currentUserId)
                .get()
                .await()

            profileFound = userDocument.exists()

            if (profileFound) {
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
            } else {
                fullName = firebaseUser.displayName ?: "Admin Account"
                username = firebaseUser.email.orEmpty()
                email = firebaseUser.email.orEmpty()
                phoneNumber = ""
                address = ""
                profileNote = ""
                profileImageBase64 = ""
                role = "Admin"
                status = "Active"
            }

            val usersSnapshot = firestore.collection("users").get().await()
            val teachersSnapshot = firestore.collection("teachers").get().await()
            val studentsSnapshot = firestore.collection("students").get().await()
            val classesSnapshot = firestore.collection("classes").get().await()
            val subjectsSnapshot = firestore.collection("subjects").get().await()

            userCount = usersSnapshot.size()
            teacherCount = teachersSnapshot.size()
            studentCount = studentsSnapshot.size()
            classCount = classesSnapshot.size()
            subjectCount = subjectsSnapshot.size()

            adminCount = usersSnapshot.documents.count { document ->
                document.getString("role") == "Admin"
            }

            teacherAccountCount = usersSnapshot.documents.count { document ->
                document.getString("role") == "Teacher"
            }

            studentAccountCount = usersSnapshot.documents.count { document ->
                document.getString("role") == "Student"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load admin profile"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrey)
    ) {
        TopBar(
            title = "Admin Profile",
            showBack = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Spacer(modifier = Modifier.height(24.dp))

                CircularProgressIndicator(
                    color = PrimaryBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Loading admin profile...",
                    fontSize = 14.sp,
                    color = TextGrey
                )
            } else if (!profileFound && errorMessage.isNotBlank()) {
                SgrmsEmptyStateCard(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Admin profile not found",
                    message = errorMessage
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Logout",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                ProfileImageView(
                    imageBase64 = profileImageBase64,
                    size = 90
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = fullName.ifBlank { "Admin Account" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${username.ifBlank { "-" }} • ${role.ifBlank { "Admin" }}",
                    fontSize = 14.sp,
                    color = TextGrey
                )

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "Account Information",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AdminProfileInfoRow(
                            icon = Icons.Default.Person,
                            title = "Full Name",
                            value = fullName.ifBlank { "-" }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        AdminProfileInfoRow(
                            icon = Icons.Default.Badge,
                            title = "Username",
                            value = username.ifBlank { "-" }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        AdminProfileInfoRow(
                            icon = Icons.Default.Email,
                            title = "Email",
                            value = email.ifBlank { "-" }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        AdminProfileInfoRow(
                            icon = Icons.Default.AdminPanelSettings,
                            title = "Role",
                            value = role.ifBlank { "Admin" }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        AdminProfileInfoRow(
                            icon = Icons.Default.Badge,
                            title = "Status",
                            value = status.ifBlank { "Active" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "Additional Information",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AdminProfileInfoRow(
                            icon = Icons.Default.Phone,
                            title = "Phone Number",
                            value = phoneNumber.ifBlank { "-" }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        AdminProfileInfoRow(
                            icon = Icons.Default.Home,
                            title = "Address",
                            value = address.ifBlank { "-" }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BackgroundGrey
                        )

                        AdminProfileInfoRow(
                            icon = Icons.Default.Note,
                            title = "Profile Note",
                            value = profileNote.ifBlank { "-" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "System Summary",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AdminProfileSummaryBox(
                                title = "Users",
                                value = userCount.toString(),
                                icon = Icons.Default.ManageAccounts,
                                modifier = Modifier.weight(1f)
                            )

                            AdminProfileSummaryBox(
                                title = "Teachers",
                                value = teacherCount.toString(),
                                icon = Icons.Default.SupervisorAccount,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AdminProfileSummaryBox(
                                title = "Students",
                                value = studentCount.toString(),
                                icon = Icons.Default.Group,
                                modifier = Modifier.weight(1f)
                            )

                            AdminProfileSummaryBox(
                                title = "Classes",
                                value = classCount.toString(),
                                icon = Icons.Default.School,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AdminProfileSummaryBox(
                            title = "Subjects",
                            value = subjectCount.toString(),
                            icon = Icons.Default.MenuBook,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AdminProfileInfoRow(
                            icon = Icons.Default.ManageAccounts,
                            title = "Account Breakdown",
                            value = "$adminCount Admin • $teacherAccountCount Teacher • $studentAccountCount Student"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedButton(
                    onClick = onEditProfileClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Edit Profile",
                        tint = PrimaryBlue
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Edit Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onChangePasswordClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Change Password",
                        tint = PrimaryBlue
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Change Password",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Logout",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AdminProfileSummaryBox(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryBlue.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryBlue.copy(alpha = 0.12f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )

                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = TextGrey
                )
            }
        }
    }
}

@Composable
fun AdminProfileInfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = PrimaryBlue,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.size(14.dp))

        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                color = TextGrey
            )

            Text(
                text = value.ifBlank { "-" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}