package com.example.sgrms.ui.admin

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.sgrms.data.model.TeacherModel
import com.example.sgrms.ui.components.SgrmsActionButtonRow
import com.example.sgrms.ui.components.SgrmsDeleteDialog
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsFilterButton
import com.example.sgrms.ui.components.SgrmsInfoRow
import com.example.sgrms.ui.components.SgrmsSearchBar
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.viewmodel.TeacherFirebaseViewModel
import java.io.File
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun TeacherManagementScreen(
    onBackClick: () -> Unit = {},
    onAddTeacherClick: () -> Unit = {},
    onViewTeacherClick: (String) -> Unit = {},
    onEditTeacherClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    val teacherViewModel: TeacherFirebaseViewModel = hiltViewModel()

    val uiState by teacherViewModel.uiState.collectAsState()

    var searchText by remember { mutableStateOf("") }

    var selectedDepartment by remember { mutableStateOf("All Departments") }
    var departmentExpanded by remember { mutableStateOf(false) }

    var selectedTeacherToDelete by remember { mutableStateOf<TeacherModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        teacherViewModel.loadTeachers()
    }

    val teachers = uiState.teachers

    val departmentList = listOf("All Departments") + teachers
        .map { teacher ->
            teacher.department.ifBlank { "Not specified" }
        }
        .distinct()
        .sorted()

    val filteredTeachers = teachers.filter { teacher ->
        val teacherDepartment = teacher.department.ifBlank { "Not specified" }

        val matchesSearch =
            teacher.teacherName.contains(searchText, ignoreCase = true) ||
                    teacher.staffId.contains(searchText, ignoreCase = true) ||
                    teacher.email.contains(searchText, ignoreCase = true) ||
                    teacher.department.contains(searchText, ignoreCase = true) ||
                    teacher.position.contains(searchText, ignoreCase = true)

        val matchesDepartment =
            selectedDepartment == "All Departments" || teacherDepartment == selectedDepartment

        matchesSearch && matchesDepartment
    }

    if (showDeleteDialog && selectedTeacherToDelete != null) {
        SgrmsDeleteDialog(
            title = "Delete Teacher",
            message = "Are you sure you want to delete this teacher account? This will remove the teacher profile from Firestore.",
            onConfirmClick = {
                selectedTeacherToDelete?.let { teacher ->
                    teacherViewModel.deleteTeacher(
                        teacherId = teacher.teacherId
                    )
                }

                showDeleteDialog = false
                selectedTeacherToDelete = null
            },
            onDismissClick = {
                showDeleteDialog = false
                selectedTeacherToDelete = null
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Teacher Management",
                showBack = false
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTeacherClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Teacher"
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGrey)
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Teacher Accounts",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View and manage teacher profiles and Firebase login accounts",
                    fontSize = 14.sp,
                    color = TextGrey
                )
            }

            item {
                SgrmsSearchBar(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    label = "Search Teacher",
                    placeholder = "Search by name, staff ID, email, department, or position"
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SgrmsFilterButton(
                        selectedValue = selectedDepartment,
                        expanded = departmentExpanded,
                        onExpandedChange = {
                            departmentExpanded = it
                        },
                        items = departmentList,
                        onItemSelected = {
                            selectedDepartment = it
                        },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = {
                            exportTeacherListToCsv(
                                context = context,
                                teachers = filteredTeachers
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

            if (uiState.errorMessage.isNotBlank()) {
                item {
                    Text(
                        text = uiState.errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Text(
                    text = "Teacher List (${filteredTeachers.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.isLoading) {
                item {
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
            } else if (filteredTeachers.isEmpty()) {
                item {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.SupervisorAccount,
                        title = "No teacher found",
                        message = "Tap Add Teacher to create a teacher account"
                    )
                }
            } else {
                items(
                    items = filteredTeachers,
                    key = { teacher ->
                        teacher.teacherId
                    }
                ) { teacher ->
                    TeacherAccountCard(
                        teacher = teacher,
                        onViewClick = {
                            onViewTeacherClick(teacher.teacherId)
                        },
                        onEditClick = {
                            onEditTeacherClick(teacher.teacherId)
                        },
                        onDeleteClick = {
                            selectedTeacherToDelete = teacher
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TeacherAccountCard(
    teacher: TeacherModel,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupervisorAccount,
                        contentDescription = "Teacher",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = teacher.teacherName.ifBlank { "Unknown Teacher" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${teacher.staffId.ifBlank { "No Staff ID" }} • Teacher",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Text(
                    text = teacher.position.ifBlank { "Teacher" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = BackgroundGrey)

            Spacer(modifier = Modifier.height(12.dp))

            SgrmsInfoRow(
                icon = Icons.Default.Badge,
                label = "Staff ID",
                value = teacher.staffId.ifBlank { "Not specified" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SgrmsInfoRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = teacher.email.ifBlank { "Not specified" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SgrmsInfoRow(
                icon = Icons.Default.Badge,
                label = "Department",
                value = teacher.department.ifBlank { "Not specified" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SgrmsInfoRow(
                icon = Icons.Default.Badge,
                label = "Position",
                value = teacher.position.ifBlank { "Teacher" }
            )

            Spacer(modifier = Modifier.height(14.dp))

            SgrmsActionButtonRow(
                onViewClick = onViewClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

fun exportTeacherListToCsv(
    context: Context,
    teachers: List<TeacherModel>
) {
    val fileName = "teacher_list.csv"
    val file = File(context.cacheDir, fileName)

    val csvContent = buildString {
        appendLine("Teacher Name,Staff ID,Email,Department,Position")

        teachers.forEach { teacher ->
            val row = listOf(
                teacher.teacherName.ifBlank { "Unknown Teacher" },
                teacher.staffId.ifBlank { "Not specified" },
                teacher.email.ifBlank { "Not specified" },
                teacher.department.ifBlank { "Not specified" },
                teacher.position.ifBlank { "Teacher" }
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
        putExtra(Intent.EXTRA_SUBJECT, "Teacher List")
        putExtra(Intent.EXTRA_TITLE, "teacher_list.csv")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            "Export Teacher List"
        )
    )
}