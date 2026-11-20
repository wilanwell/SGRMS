package com.example.sgrms.ui.student

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sgrms.data.firebase.StudentFirebaseService
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.data.repository.StudentFirebaseRepository
import com.example.sgrms.ui.components.SgrmsActionButtonRow
import com.example.sgrms.ui.components.SgrmsSearchBar
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.SuccessGreen
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModelFactory
import java.io.File

@Composable
fun StudentManagementScreen(
    onBackClick: () -> Unit = {},
    onAddStudentClick: () -> Unit = {},
    onStudentDetailClick: (String) -> Unit = {},
    onEditStudentClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    val studentFirebaseService = remember {
        StudentFirebaseService()
    }

    val studentFirebaseRepository = remember {
        StudentFirebaseRepository(studentFirebaseService)
    }

    val studentViewModel: StudentFirebaseViewModel = viewModel(
        factory = StudentFirebaseViewModelFactory(studentFirebaseRepository)
    )

    val uiState by studentViewModel.uiState.collectAsState()

    var searchText by remember { mutableStateOf("") }

    var selectedClass by remember { mutableStateOf("All Classes") }
    var selectedGender by remember { mutableStateOf("All Gender") }
    var selectedStatus by remember { mutableStateOf("All Status") }
    var selectedSort by remember { mutableStateOf("Name") }

    var showFilterDialog by remember { mutableStateOf(false) }

    var selectedStudentToDelete by remember { mutableStateOf<StudentModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        studentViewModel.loadStudents()
    }

    val students = uiState.students

    val classList = listOf("All Classes") + students
        .map { student ->
            student.className.ifBlank { "Not assigned" }
        }
        .distinct()
        .sorted()

    val genderList = listOf(
        "All Gender",
        "Male",
        "Female"
    )

    val statusList = listOf(
        "All Status",
        "Active",
        "Inactive"
    )

    val sortList = listOf(
        "Name",
        "Code",
        "Class",
        "Gender",
        "Status"
    )

    val filteredStudents = students.filter { student ->
        val studentClass = student.className.ifBlank { "Not assigned" }
        val studentGender = student.gender.ifBlank { "Not specified" }
        val studentStatus = student.status.ifBlank { "Active" }

        val matchesSearch =
            student.studentName.contains(searchText, ignoreCase = true) ||
                    student.studentCode.contains(searchText, ignoreCase = true) ||
                    student.email.contains(searchText, ignoreCase = true) ||
                    student.phoneNumber.contains(searchText, ignoreCase = true) ||
                    studentClass.contains(searchText, ignoreCase = true) ||
                    studentGender.contains(searchText, ignoreCase = true) ||
                    studentStatus.contains(searchText, ignoreCase = true)

        val matchesClass =
            selectedClass == "All Classes" || studentClass == selectedClass

        val matchesGender =
            selectedGender == "All Gender" || studentGender == selectedGender

        val matchesStatus =
            selectedStatus == "All Status" || studentStatus == selectedStatus

        matchesSearch && matchesClass && matchesGender && matchesStatus
    }.let { list ->
        when (selectedSort) {
            "Code" -> list.sortedBy { student ->
                student.studentCode.lowercase()
            }

            "Class" -> list.sortedBy { student ->
                student.className.lowercase()
            }

            "Gender" -> list.sortedBy { student ->
                student.gender.lowercase()
            }

            "Status" -> list.sortedBy { student ->
                student.status.lowercase()
            }

            else -> list.sortedBy { student ->
                student.studentName.lowercase()
            }
        }
    }

    if (showDeleteDialog && selectedStudentToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedStudentToDelete = null
            },
            title = {
                Text(
                    text = "Delete Student",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${selectedStudentToDelete?.studentName}? This student profile will be removed from Firestore."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedStudentToDelete?.let { student ->
                            studentViewModel.deleteStudent(
                                studentId = student.studentId,
                                onSuccess = {
                                    studentViewModel.loadStudents()
                                }
                            )
                        }

                        showDeleteDialog = false
                        selectedStudentToDelete = null
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
                        selectedStudentToDelete = null
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

    if (showFilterDialog) {
        StudentFilterDialog(
            selectedClass = selectedClass,
            selectedGender = selectedGender,
            selectedStatus = selectedStatus,
            selectedSort = selectedSort,
            classList = classList,
            genderList = genderList,
            statusList = statusList,
            sortList = sortList,
            onClassSelected = {
                selectedClass = it
            },
            onGenderSelected = {
                selectedGender = it
            },
            onStatusSelected = {
                selectedStatus = it
            },
            onSortSelected = {
                selectedSort = it
            },
            onDismiss = {
                showFilterDialog = false
            },
            onClearFilter = {
                selectedClass = "All Classes"
                selectedGender = "All Gender"
                selectedStatus = "All Status"
                selectedSort = "Name"
                showFilterDialog = false
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Students",
                showBack = false
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddStudentClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Student"
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGrey)
                .padding(paddingValues)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            SgrmsSearchBar(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                label = "Search Student",
                placeholder = "Search by name, code, email, phone, class, gender, or status"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Student List",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Total Students: ${students.size}",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            showFilterDialog = true
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBlue
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter Students",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Filter",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            exportStudentListToCsv(
                                context = context,
                                students = filteredStudents
                            )
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBlue
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Export",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${filteredStudents.size} shown",
                fontSize = 13.sp,
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
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

                uiState.errorMessage.isNotBlank() -> {
                    Text(
                        text = uiState.errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                filteredStudents.isEmpty() -> {
                    EmptyStudentCard()
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(
                            items = filteredStudents,
                            key = { student ->
                                student.studentId
                            }
                        ) { student ->
                            StudentCard(
                                student = student,
                                onViewClick = {
                                    onStudentDetailClick(student.studentId)
                                },
                                onEditClick = {
                                    onEditStudentClick(student.studentId)
                                },
                                onDeleteClick = {
                                    selectedStudentToDelete = student
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
fun StudentCard(
    student: StudentModel,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val gender = student.gender.ifBlank { "Not specified" }
    val status = student.status.ifBlank { "Active" }

    val profileColor = if (gender == "Female") {
        Color(0xFFFF4081)
    } else {
        PrimaryBlue
    }

    val statusColor = if (status == "Active") {
        SuccessGreen
    } else {
        Color.Red
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
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(profileColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Student Icon",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = student.studentName.ifBlank { "Unknown Student" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Code: ${student.studentCode.ifBlank { "-" }}",
                        fontSize = 13.sp,
                        color = TextGrey
                    )

                    Text(
                        text = "Class: ${student.className.ifBlank { "Not assigned" }}",
                        fontSize = 13.sp,
                        color = TextGrey
                    )

                    Text(
                        text = "Gender: $gender",
                        fontSize = 13.sp,
                        color = TextGrey
                    )

                    Text(
                        text = "Email: ${student.email.ifBlank { "Not specified" }}",
                        fontSize = 13.sp,
                        color = TextGrey
                    )

                    Text(
                        text = "Phone: ${student.phoneNumber.ifBlank { "Not specified" }}",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Text(
                    text = status,
                    fontSize = 12.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            statusColor.copy(alpha = 0.1f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SgrmsActionButtonRow(
                onViewClick = onViewClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EmptyStudentCard() {
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
                imageVector = Icons.Default.Person,
                contentDescription = "No Students",
                tint = TextGrey,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "No students found",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Add a new student to display records here.",
                fontSize = 13.sp,
                color = TextGrey
            )
        }
    }
}

@Composable
fun StudentFilterDialog(
    selectedClass: String,
    selectedGender: String,
    selectedStatus: String,
    selectedSort: String,
    classList: List<String>,
    genderList: List<String>,
    statusList: List<String>,
    sortList: List<String>,
    onClassSelected: (String) -> Unit,
    onGenderSelected: (String) -> Unit,
    onStatusSelected: (String) -> Unit,
    onSortSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onClearFilter: () -> Unit
) {
    var classExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Filter Students",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                StudentDialogDropdown(
                    label = "Class",
                    value = selectedClass,
                    expanded = classExpanded,
                    onExpandedChange = {
                        classExpanded = it
                    },
                    items = classList,
                    onItemSelected = { value ->
                        onClassSelected(value)
                        classExpanded = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                StudentDialogDropdown(
                    label = "Gender",
                    value = selectedGender,
                    expanded = genderExpanded,
                    onExpandedChange = {
                        genderExpanded = it
                    },
                    items = genderList,
                    onItemSelected = { value ->
                        onGenderSelected(value)
                        genderExpanded = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                StudentDialogDropdown(
                    label = "Status",
                    value = selectedStatus,
                    expanded = statusExpanded,
                    onExpandedChange = {
                        statusExpanded = it
                    },
                    items = statusList,
                    onItemSelected = { value ->
                        onStatusSelected(value)
                        statusExpanded = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                StudentDialogDropdown(
                    label = "Sort",
                    value = selectedSort,
                    expanded = sortExpanded,
                    onExpandedChange = {
                        sortExpanded = it
                    },
                    items = sortList,
                    onItemSelected = { value ->
                        onSortSelected(value)
                        sortExpanded = false
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                )
            ) {
                Text(
                    text = "Apply",
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onClearFilter
            ) {
                Text(
                    text = "Clear",
                    color = PrimaryBlue
                )
            }
        }
    )
}

@Composable
fun StudentDialogDropdown(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    Box {
        OutlinedButton(
            onClick = {
                onExpandedChange(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryBlue
            )
        ) {
            Text(
                text = "$label: $value",
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = label,
                tint = PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(item)
                    },
                    onClick = {
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

fun exportStudentListToCsv(
    context: Context,
    students: List<StudentModel>
) {
    val fileName = "student_list.csv"
    val file = File(context.cacheDir, fileName)

    val csvContent = buildString {
        appendLine("Student Name,Student Code,Email,Phone Number,Class,Gender,Status")

        students.forEach { student ->
            val row = listOf(
                student.studentName.ifBlank { "Unknown Student" },
                student.studentCode.ifBlank { "-" },
                student.email.ifBlank { "Not specified" },
                student.phoneNumber.ifBlank { "Not specified" },
                student.className.ifBlank { "Not assigned" },
                student.gender.ifBlank { "Not specified" },
                student.status.ifBlank { "Active" }
            )

            appendLine(
                row.joinToString(",") { value ->
                    "\"${value.replace("\"", "\"\"")}\""
                }
            )
        }
    }

    file.writeText(csvContent)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Student List")
        putExtra(Intent.EXTRA_TITLE, "student_list.csv")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            "Export Student List"
        )
    )
}