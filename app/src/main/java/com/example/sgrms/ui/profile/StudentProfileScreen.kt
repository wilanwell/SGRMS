package com.example.sgrms.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.firebase.GradeFirebaseService
import com.example.sgrms.data.firebase.StudentFirebaseService
import com.example.sgrms.data.model.GradeModel
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.ui.components.ProfileImageView
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.sgrms.utils.GradeCalculator

@Composable
fun StudentProfileScreen(
    studentId: Int = 0,
    firebaseStudentId: String = "",
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val currentStudentId = firebaseStudentId.ifBlank {
        if (studentId != 0) {
            studentId.toString()
        } else {
            ""
        }
    }

    val studentFirebaseService = remember {
        StudentFirebaseService()
    }

    val gradeFirebaseService = remember {
        GradeFirebaseService()
    }

    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    var student by remember {
        mutableStateOf<StudentModel?>(null)
    }

    var grades by remember {
        mutableStateOf<List<GradeModel>>(emptyList())
    }

    var userFullName by remember {
        mutableStateOf("")
    }

    var userEmail by remember {
        mutableStateOf("")
    }

    var userRole by remember {
        mutableStateOf("Student")
    }

    var username by remember {
        mutableStateOf("")
    }

    LaunchedEffect(currentStudentId) {
        if (currentStudentId.isNotBlank()) {
            val loadedStudent = studentFirebaseService.getStudentById(currentStudentId)
            student = loadedStudent

            grades = gradeFirebaseService.getGradesByStudentId(currentStudentId)

            if (loadedStudent?.userId?.isNotBlank() == true) {
                val userDocument = firestore
                    .collection("users")
                    .document(loadedStudent.userId)
                    .get()
                    .await()

                userFullName = userDocument.getString("fullName")
                    ?: userDocument.getString("name")
                            ?: loadedStudent.studentName

                userEmail = userDocument.getString("email")
                    ?: loadedStudent.email

                userRole = userDocument.getString("role")
                    ?: "Student"

                username = userDocument.getString("username")
                    ?: userEmail.substringBefore("@", missingDelimiterValue = "-")
            } else {
                userFullName = loadedStudent?.studentName.orEmpty()
                userEmail = loadedStudent?.email.orEmpty()
                userRole = "Student"
                username = userEmail.substringBefore("@", missingDelimiterValue = "-")
            }
        }
    }

    val completedGrades = grades.filter { grade ->
        grade.status.equals("Complete", ignoreCase = true)
    }

    val totalSubjects = completedGrades.size

    val averageMarks = if (completedGrades.isNotEmpty()) {
        completedGrades.map { grade ->
            grade.marks
        }.average()
    } else {
        0.0
    }

    val overallGrade = if (completedGrades.isNotEmpty()) {
        GradeCalculator.gradeLetter(averageMarks)
    } else {
        "-"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrey)
    ) {
        TopBar(
            title = "Student Profile",
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
            ProfileImageView(
                imageBase64 = student?.profileImageBase64.orEmpty(),
                size = 90
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = student?.studentName ?: userFullName.ifBlank { "Student Account" },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "View student account information",
                fontSize = 14.sp,
                color = TextGrey
            )

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

                    StudentProfileInfoRow(
                        icon = Icons.Default.Person,
                        title = "Student Name",
                        value = student?.studentName ?: userFullName.ifBlank { "Not linked" }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.Badge,
                        title = "Student ID",
                        value = student?.studentCode ?: "Not linked"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.Person,
                        title = "Username",
                        value = username.ifBlank { "-" }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.Email,
                        title = "Email",
                        value = student?.email ?: userEmail.ifBlank { "-" }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.Badge,
                        title = "Role",
                        value = userRole.ifBlank { "Student" }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.School,
                        title = "Class",
                        value = student?.className ?: "Not linked"
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
                        text = "Personal Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    StudentProfileInfoRow(
                        icon = Icons.Default.Badge,
                        title = "Date of Birth",
                        value = student?.dateOfBirth ?: "-"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.Person,
                        title = "Gender",
                        value = student?.gender ?: "-"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.Badge,
                        title = "Phone Number",
                        value = student?.phoneNumber ?: "-"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.School,
                        title = "Address",
                        value = student?.address ?: "-"
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
                        text = "Guardian Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    StudentProfileInfoRow(
                        icon = Icons.Default.Person,
                        title = "Guardian Name",
                        value = student?.guardianName ?: "-"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.Badge,
                        title = "Guardian Phone",
                        value = student?.guardianPhone ?: "-"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.Badge,
                        title = "Emergency Contact",
                        value = student?.emergencyContact ?: "-"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.MenuBook,
                        title = "Profile Note",
                        value = student?.profileNote ?: "-"
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
                        text = "Academic Summary",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    StudentProfileInfoRow(
                        icon = Icons.Default.MenuBook,
                        title = "Total Subjects",
                        value = totalSubjects.toString()
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.TrendingUp,
                        title = "Average Marks",
                        value = "%.2f%%".format(averageMarks)
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BackgroundGrey
                    )

                    StudentProfileInfoRow(
                        icon = Icons.Default.Badge,
                        title = "Overall Grade",
                        value = overallGrade
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

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
        }
    }
}

@Composable
fun StudentProfileInfoRow(
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
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(
                text = title,
                fontSize = 13.sp,
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