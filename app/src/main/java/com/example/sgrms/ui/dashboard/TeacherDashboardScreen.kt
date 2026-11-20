package com.example.sgrms.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.sgrms.data.model.GradeModel
import com.example.sgrms.data.model.SubjectModel
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ClassAverageItem(
    val subjectName: String,
    val progress: Float,
    val percentage: String
)

@Composable
fun TeacherDashboardScreen(
    teacherId: Int = 0,
    firebaseTeacherId: String = "",
    onStudentClick: () -> Unit = {},
    onSubjectClick: () -> Unit = {},
    onGradeClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var teacherName by remember { mutableStateOf("Teacher") }

    var studentCount by remember { mutableIntStateOf(0) }
    var subjectCount by remember { mutableIntStateOf(0) }
    var gradeCount by remember { mutableIntStateOf(0) }
    var reportCount by remember { mutableIntStateOf(0) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    val subjects = remember { mutableStateListOf<SubjectModel>() }
    val grades = remember { mutableStateListOf<GradeModel>() }

    LaunchedEffect(teacherId, firebaseTeacherId) {
        try {
            isLoading = true
            errorMessage = ""

            val currentUserId = firebaseAuth.currentUser?.uid.orEmpty()

            val userDocument = if (currentUserId.isNotBlank()) {
                firestore
                    .collection("users")
                    .document(currentUserId)
                    .get()
                    .await()
            } else {
                null
            }

            val resolvedTeacherId = firebaseTeacherId.ifBlank {
                userDocument?.getString("teacherId").orEmpty()
            }

            teacherName = if (resolvedTeacherId.isNotBlank()) {
                val teacherDocument = firestore
                    .collection("teachers")
                    .document(resolvedTeacherId)
                    .get()
                    .await()

                teacherDocument.getString("teacherName")
                    ?: userDocument?.getString("fullName")
                    ?: "Teacher"
            } else {
                userDocument?.getString("fullName") ?: "Teacher"
            }

            val studentsSnapshot = firestore
                .collection("students")
                .get()
                .await()

            val subjectsSnapshot = firestore
                .collection("subjects")
                .get()
                .await()

            val gradesSnapshot = firestore
                .collection("grades")
                .get()
                .await()

            val reportsSnapshot = firestore
                .collection("reports")
                .get()
                .await()

            studentCount = studentsSnapshot.size()
            subjectCount = subjectsSnapshot.size()
            gradeCount = gradesSnapshot.size()
            reportCount = reportsSnapshot.size()

            subjects.clear()
            subjects.addAll(
                subjectsSnapshot.documents.mapNotNull { document ->
                    document.toObject(SubjectModel::class.java)?.copy(
                        subjectId = document.id
                    )
                }
            )

            grades.clear()
            grades.addAll(
                gradesSnapshot.documents.mapNotNull { document ->
                    document.toObject(GradeModel::class.java)?.copy(
                        gradeId = document.id
                    )
                }
            )
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load dashboard data"
        } finally {
            isLoading = false
        }
    }

    val classAverageList = subjects.map { subject ->
        val subjectGrades = grades.filter { grade ->
            grade.subjectId == subject.subjectId
        }

        val averageMarks = if (subjectGrades.isNotEmpty()) {
            subjectGrades.map { grade ->
                grade.marks
            }.average()
        } else {
            0.0
        }

        ClassAverageItem(
            subjectName = subject.subjectName,
            progress = (averageMarks / 100.0)
                .toFloat()
                .coerceIn(0f, 1f),
            percentage = "%.1f%%".format(averageMarks)
        )
    }.sortedByDescending { item ->
        item.progress
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrey)
    ) {
        TopBar(
            title = "SGRMS",
            showBack = false,
            showIcon = true,
            actions = {
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 18.dp,
                    bottom = 8.dp
                )
        ) {
            Text(
                text = "Welcome, ${teacherName.ifBlank { "Teacher" }}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Overview of student grades and academic records",
                fontSize = 14.sp,
                color = TextGrey
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = PrimaryBlue,
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Loading dashboard data...",
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }
            }

            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DashboardSummaryCard(
                    title = "Students",
                    value = studentCount.toString(),
                    icon = Icons.Default.Group,
                    modifier = Modifier.weight(1f),
                    onClick = onStudentClick
                )

                DashboardSummaryCard(
                    title = "Subjects",
                    value = subjectCount.toString(),
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.weight(1f),
                    onClick = onSubjectClick
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DashboardSummaryCard(
                    title = "Grades",
                    value = gradeCount.toString(),
                    icon = Icons.Default.Assessment,
                    modifier = Modifier.weight(1f),
                    onClick = onGradeClick
                )

                DashboardSummaryCard(
                    title = "Reports",
                    value = reportCount.toString(),
                    icon = Icons.Default.Summarize,
                    modifier = Modifier.weight(1f),
                    onClick = onReportClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Class Averages",
                            tint = PrimaryBlue
                        )

                        Spacer(modifier = Modifier.size(10.dp))

                        Text(
                            text = "Class Averages",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (classAverageList.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "No Average Data",
                                tint = TextGrey,
                                modifier = Modifier.size(42.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "No class average available",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Add subjects and grades to show average performance",
                                fontSize = 14.sp,
                                color = TextGrey
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                bottom = 8.dp
                            )
                        ) {
                            items(classAverageList) { item ->
                                SubjectAverageItem(
                                    subjectName = item.subjectName,
                                    progress = item.progress,
                                    percentage = item.percentage
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardSummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = value,
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SubjectAverageItem(
    subjectName: String,
    progress: Float,
    percentage: String
) {
    Column(
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = subjectName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = percentage,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = {
                progress
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = PrimaryBlue,
            trackColor = BackgroundGrey
        )
    }
}