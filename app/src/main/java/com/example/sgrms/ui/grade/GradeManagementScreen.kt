package com.example.sgrms.ui.grade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.firebase.GradeFirebaseService
import com.example.sgrms.data.firebase.StudentFirebaseService
import com.example.sgrms.data.firebase.SubjectFirebaseService
import com.example.sgrms.data.model.GradeModel
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.data.model.SubjectModel
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.SuccessGreen
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.ui.theme.WarningOrange
import kotlinx.coroutines.launch
import com.example.sgrms.utils.GradeCalculator

data class GradeDisplayItem(
    val grade: GradeModel,
    val studentName: String,
    val className: String,
    val subjectName: String,
    val subjectCode: String
)

@Composable
fun GradeManagementScreen(
    onBackClick: () -> Unit = {},
    onAddGradeClick: () -> Unit = {},
    onStudentDetailClick: (String) -> Unit = {},
    onEditGradeClick: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    val gradeFirebaseService = remember {
        GradeFirebaseService()
    }

    val studentFirebaseService = remember {
        StudentFirebaseService()
    }

    val subjectFirebaseService = remember {
        SubjectFirebaseService()
    }

    var grades by remember { mutableStateOf<List<GradeModel>>(emptyList()) }
    var students by remember { mutableStateOf<List<StudentModel>>(emptyList()) }
    var subjects by remember { mutableStateOf<List<SubjectModel>>(emptyList()) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var searchText by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("All Classes") }
    var selectedSubject by remember { mutableStateOf("All Subjects") }
    var selectedStatus by remember { mutableStateOf("All Status") }

    var classExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    var selectedGradeToDelete by remember { mutableStateOf<GradeModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun loadGradeData() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = ""

            try {
                grades = gradeFirebaseService.getAllGrades()
                students = studentFirebaseService.getAllStudents()
                subjects = subjectFirebaseService.getAllSubjects()
            } catch (exception: Exception) {
                errorMessage = exception.message ?: "Failed to load grade records."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadGradeData()
    }

    val gradeDisplayList = grades.map { grade ->
        val student = students.find { student ->
            student.studentId == grade.studentId
        }

        val subject = subjects.find { subject ->
            subject.subjectId == grade.subjectId
        }

        GradeDisplayItem(
            grade = grade,
            studentName = student?.studentName ?: "Unknown Student",
            className = student?.className ?: "Unknown Class",
            subjectName = subject?.subjectName ?: "Unknown Subject",
            subjectCode = subject?.subjectCode ?: "-"
        )
    }

    val classList = listOf("All Classes") + students
        .map { student ->
            student.className.ifBlank { "Unknown Class" }
        }
        .distinct()
        .sorted()

    val subjectList = listOf("All Subjects") + subjects
        .map { subject ->
            subject.subjectName.ifBlank { "Unknown Subject" }
        }
        .distinct()
        .sorted()

    val statusList = listOf(
        "All Status",
        "Complete",
        "Pending"
    )

    val filteredRecords = gradeDisplayList.filter { item ->
        val matchesSearch =
            item.studentName.contains(searchText, ignoreCase = true) ||
                    item.subjectName.contains(searchText, ignoreCase = true) ||
                    item.subjectCode.contains(searchText, ignoreCase = true) ||
                    item.className.contains(searchText, ignoreCase = true)

        val matchesClass =
            selectedClass == "All Classes" || item.className == selectedClass

        val matchesSubject =
            selectedSubject == "All Subjects" || item.subjectName == selectedSubject

        val matchesStatus =
            selectedStatus == "All Status" || item.grade.status == selectedStatus

        matchesSearch && matchesClass && matchesSubject && matchesStatus
    }

    if (showDeleteDialog && selectedGradeToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedGradeToDelete = null
            },
            title = {
                Text(
                    text = "Delete Grade",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this grade record? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val gradeToDelete = selectedGradeToDelete

                        if (gradeToDelete != null) {
                            coroutineScope.launch {
                                try {
                                    gradeFirebaseService.deleteGrade(
                                        gradeId = gradeToDelete.gradeId
                                    )

                                    loadGradeData()
                                } catch (exception: Exception) {
                                    errorMessage = exception.message ?: "Failed to delete grade."
                                }
                            }
                        }

                        showDeleteDialog = false
                        selectedGradeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text(
                        text = "Delete",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedGradeToDelete = null
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = PrimaryBlue
                    )
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Grade Manager",
                showBack = false,
                showIcon = true,
                icon = Icons.Default.Assessment
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGradeClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier.padding(end = 4.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 6.dp,
                    focusedElevation = 6.dp,
                    hoveredElevation = 6.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Grade",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGrey)
                .padding(paddingValues)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp
                )
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                label = {
                    Text("Search Grade")
                },
                placeholder = {
                    Text("Search by student, subject, or class")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextGrey
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    focusedLabelColor = PrimaryBlue,
                    cursorColor = PrimaryBlue
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box {
                    OutlinedButton(
                        onClick = {
                            classExpanded = true
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBlue
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Class Filter",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.size(6.dp))

                        Text(
                            text = selectedClass,
                            fontSize = 13.sp,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.size(4.dp))

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Class",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = classExpanded,
                        onDismissRequest = {
                            classExpanded = false
                        }
                    ) {
                        classList.forEach { className ->
                            DropdownMenuItem(
                                text = {
                                    Text(className)
                                },
                                onClick = {
                                    selectedClass = className
                                    classExpanded = false
                                }
                            )
                        }
                    }
                }

                Box {
                    OutlinedButton(
                        onClick = {
                            subjectExpanded = true
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBlue
                        )
                    ) {
                        Text(
                            text = selectedSubject,
                            fontSize = 13.sp,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.size(4.dp))

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Subject",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = {
                            subjectExpanded = false
                        }
                    ) {
                        subjectList.forEach { subjectName ->
                            DropdownMenuItem(
                                text = {
                                    Text(subjectName)
                                },
                                onClick = {
                                    selectedSubject = subjectName
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                Box {
                    OutlinedButton(
                        onClick = {
                            statusExpanded = true
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBlue
                        )
                    ) {
                        Text(
                            text = selectedStatus,
                            fontSize = 13.sp,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.size(4.dp))

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select Status",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = {
                            statusExpanded = false
                        }
                    ) {
                        statusList.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(status)
                                },
                                onClick = {
                                    selectedStatus = status
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Grade Records (${filteredRecords.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue
                        )
                    }
                }

                filteredRecords.isEmpty() -> {
                    GradeEmptyStateCard()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredRecords,
                            key = { item ->
                                item.grade.gradeId
                            }
                        ) { item ->
                            GradeCard(
                                item = item,
                                onViewClick = {
                                    onStudentDetailClick(item.grade.studentId)
                                },
                                onEditClick = {
                                    onEditGradeClick(item.grade.gradeId)
                                },
                                onDeleteClick = {
                                    selectedGradeToDelete = item.grade
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradeEmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Assessment,
                contentDescription = "No Grades",
                tint = TextGrey,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No grade record found",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tap the add button to create a grade record",
                fontSize = 14.sp,
                color = TextGrey
            )
        }
    }
}

@Composable
fun GradeCard(
    item: GradeDisplayItem,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val statusIcon: ImageVector =
        if (item.grade.status == "Complete") {
            Icons.Default.CheckCircle
        } else {
            Icons.Default.AccessTime
        }

    val statusColor: Color =
        if (item.grade.status == "Complete") {
            SuccessGreen
        } else {
            WarningOrange
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onViewClick()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Student",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.studentName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = item.className,
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = item.grade.gradeLetter.ifBlank {
                            GradeCalculator.gradeLetter(item.grade.marks)
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )

                    Text(
                        text = "%.2f%%".format(item.grade.marks),
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = BackgroundGrey)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Subject",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = item.subjectName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = item.subjectCode,
                        fontSize = 13.sp,
                        color = TextGrey,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = item.grade.status,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = item.grade.status.ifBlank { "Pending" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            if (item.grade.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Remarks: ${item.grade.remarks}",
                    fontSize = 14.sp,
                    color = TextGrey
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedIconButton(
                    onClick = onViewClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "View Grade",
                        tint = PrimaryBlue
                    )
                }

                OutlinedIconButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Grade",
                        tint = PrimaryBlue
                    )
                }

                OutlinedIconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Grade",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

