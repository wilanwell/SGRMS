package com.example.sgrms.ui.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sgrms.data.firebase.ClassFirebaseService
import com.example.sgrms.data.model.ClassModel
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.data.repository.ClassFirebaseRepository
import com.example.sgrms.ui.components.SgrmsActionButtonRow
import com.example.sgrms.ui.components.SgrmsDashboardSummaryCard
import com.example.sgrms.ui.components.SgrmsDeleteDialog
import com.example.sgrms.ui.components.SgrmsDropdownField
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsInfoRow
import com.example.sgrms.ui.components.SgrmsSearchBar
import com.example.sgrms.ui.components.SgrmsSummaryCardStyle
import com.example.sgrms.ui.components.SgrmsTextField
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.SuccessGreen
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.viewmodel.ClassFirebaseViewModel
import com.example.sgrms.viewmodel.ClassFirebaseViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class ClassTeacherItem(
    val teacherId: String = "",
    val teacherName: String = ""
)

@Composable
fun ClassManagementScreen(
    onBackClick: () -> Unit = {},
    onViewClassClick: (String) -> Unit = {},
    onAddStudentClick: () -> Unit = {}
) {
    val classFirebaseService = remember {
        ClassFirebaseService()
    }

    val classFirebaseRepository = remember {
        ClassFirebaseRepository(classFirebaseService)
    }

    val classViewModel: ClassFirebaseViewModel = viewModel(
        factory = ClassFirebaseViewModelFactory(classFirebaseRepository)
    )

    val uiState by classViewModel.uiState.collectAsState()

    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    var teachers by remember { mutableStateOf<List<ClassTeacherItem>>(emptyList()) }
    var students by remember { mutableStateOf<List<StudentModel>>(emptyList()) }

    var searchText by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedClassToEdit by remember { mutableStateOf<ClassModel?>(null) }
    var selectedClassToDelete by remember { mutableStateOf<ClassModel?>(null) }
    var selectedClassToView by remember { mutableStateOf<ClassModel?>(null) }

    LaunchedEffect(Unit) {
        classViewModel.loadClasses()

        try {
            val teacherSnapshot = firestore
                .collection("teachers")
                .get()
                .await()

            teachers = teacherSnapshot.documents.mapNotNull { document ->
                val teacherName = document.getString("teacherName")
                    ?: document.getString("fullName")
                    ?: document.getString("name")
                    ?: ""

                if (teacherName.isNotBlank()) {
                    ClassTeacherItem(
                        teacherId = document.id,
                        teacherName = teacherName
                    )
                } else {
                    null
                }
            }.sortedBy { teacher ->
                teacher.teacherName
            }
        } catch (_: Exception) {
            teachers = emptyList()
        }

        try {
            val studentSnapshot = firestore
                .collection("students")
                .get()
                .await()

            students = studentSnapshot.toObjects(StudentModel::class.java)
        } catch (_: Exception) {
            students = emptyList()
        }
    }

    val classes = uiState.classes

    val filteredClasses = classes.filter { schoolClass ->
        schoolClass.className.contains(searchText, ignoreCase = true) ||
                schoolClass.academicYear.contains(searchText, ignoreCase = true) ||
                schoolClass.description.contains(searchText, ignoreCase = true) ||
                schoolClass.classTeacherName.contains(searchText, ignoreCase = true)
    }

    if (showAddDialog) {
        AddClassDialog(
            teachers = teachers,
            onDismiss = {
                showAddDialog = false
            },
            onSave = { className, academicYear, description, classTeacherId, classTeacherName ->
                classViewModel.addClass(
                    className = className.trim(),
                    academicYear = academicYear.trim(),
                    classTeacherId = classTeacherId,
                    classTeacherName = classTeacherName,
                    description = description.trim(),
                    onSuccess = {
                        showAddDialog = false
                    }
                )
            }
        )
    }

    if (showEditDialog && selectedClassToEdit != null) {
        EditClassDialog(
            schoolClass = selectedClassToEdit!!,
            teachers = teachers,
            onDismiss = {
                showEditDialog = false
                selectedClassToEdit = null
            },
            onSave = { updatedClass ->
                classViewModel.updateClass(
                    schoolClass = updatedClass,
                    onSuccess = {
                        showEditDialog = false
                        selectedClassToEdit = null
                    }
                )
            }
        )
    }

    if (showDeleteDialog && selectedClassToDelete != null) {
        SgrmsDeleteDialog(
            title = "Delete Class",
            message = "Are you sure you want to delete this class? This action cannot be undone.",
            onConfirmClick = {
                selectedClassToDelete?.let { schoolClass ->
                    classViewModel.deleteClass(
                        classId = schoolClass.classId,
                        onSuccess = {
                            showDeleteDialog = false
                            selectedClassToDelete = null
                        }
                    )
                }
            },
            onDismissClick = {
                showDeleteDialog = false
                selectedClassToDelete = null
            }
        )
    }

    selectedClassToView?.let { schoolClass ->
        val studentsInClass = students.filter { student ->
            student.classId == schoolClass.classId ||
                    student.className == schoolClass.className
        }

        ClassStudentsDialog(
            schoolClass = schoolClass,
            students = studentsInClass,
            onDismiss = {
                selectedClassToView = null
            },
            onAddStudentClick = {
                selectedClassToView = null
                onAddStudentClick()
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Class Management",
                showBack = false,
                showIcon = true
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Class"
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGrey)
                .padding(paddingValues)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SgrmsDashboardSummaryCard(
                        title = "Classes",
                        value = classes.size.toString(),
                        icon = Icons.Default.School,
                        modifier = Modifier.weight(1f),
                        style = SgrmsSummaryCardStyle.Primary,
                        height = 108.dp
                    )

                    SgrmsDashboardSummaryCard(
                        title = "Teachers",
                        value = teachers.size.toString(),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f),
                        style = SgrmsSummaryCardStyle.Primary,
                        height = 108.dp
                    )
                }
            }

            item {
                SgrmsSearchBar(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    label = "Search Class",
                    placeholder = "Search by class name or academic year"
                )
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
                    text = "Class List (${filteredClasses.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (filteredClasses.isEmpty()) {
                item {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.School,
                        title = "No class found",
                        message = "Tap Add Class to create a class record"
                    )
                }
            } else {
                items(
                    items = filteredClasses,
                    key = { schoolClass ->
                        schoolClass.classId
                    }
                ) { schoolClass ->
                    ClassCard(
                        schoolClass = schoolClass,
                        classTeacherName = schoolClass.classTeacherName.ifBlank {
                            "Not assigned"
                        },
                        onViewClick = {
                            onViewClassClick(schoolClass.classId)
                        },
                        onEditClick = {
                            selectedClassToEdit = schoolClass
                            showEditDialog = true
                        },
                        onDeleteClick = {
                            selectedClassToDelete = schoolClass
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddClassDialog(
    teachers: List<ClassTeacherItem>,
    onDismiss: () -> Unit,
    onSave: (
        className: String,
        academicYear: String,
        description: String,
        classTeacherId: String,
        classTeacherName: String
    ) -> Unit
) {
    var className by remember { mutableStateOf("") }
    var academicYear by remember { mutableStateOf("2024/2025") }
    var description by remember { mutableStateOf("") }

    var selectedTeacherName by remember { mutableStateOf("") }
    var selectedTeacherId by remember { mutableStateOf("") }
    var teacherExpanded by remember { mutableStateOf(false) }

    var classNameError by remember { mutableStateOf(false) }
    var academicYearError by remember { mutableStateOf(false) }

    val teacherItems = listOf("Not assigned") + teachers.map { teacher ->
        teacher.teacherName
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Class",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                SgrmsTextField(
                    value = className,
                    onValueChange = {
                        className = it
                        classNameError = false
                    },
                    label = "Class Name",
                    placeholder = "Example: 5 Amanah",
                    icon = Icons.Default.School,
                    isError = classNameError,
                    errorMessage = "Class name is required"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SgrmsTextField(
                    value = academicYear,
                    onValueChange = {
                        academicYear = it
                        academicYearError = false
                    },
                    label = "Academic Year",
                    placeholder = "Example: 2024/2025",
                    icon = Icons.Default.Badge,
                    isError = academicYearError,
                    errorMessage = "Academic year is required"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SgrmsDropdownField(
                    value = selectedTeacherName.ifBlank { "Not assigned" },
                    label = "Class Teacher",
                    placeholder = "Select class teacher",
                    expanded = teacherExpanded,
                    onExpandedChange = {
                        teacherExpanded = it
                    },
                    items = teacherItems,
                    onItemSelected = { selectedName ->
                        selectedTeacherName = selectedName

                        if (selectedName == "Not assigned") {
                            selectedTeacherId = ""
                        } else {
                            selectedTeacherId = teachers.find { teacher ->
                                teacher.teacherName == selectedName
                            }?.teacherId.orEmpty()
                        }

                        teacherExpanded = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SgrmsTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = "Description",
                    placeholder = "Optional class description",
                    icon = Icons.Default.MenuBook,
                    singleLine = false,
                    modifier = Modifier.height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    classNameError = className.isBlank()
                    academicYearError = academicYear.isBlank()

                    val isValid = !classNameError && !academicYearError

                    if (isValid) {
                        onSave(
                            className,
                            academicYear,
                            description,
                            selectedTeacherId,
                            selectedTeacherName.ifBlank { "Not assigned" }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                )
            ) {
                Text(
                    text = "Save",
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel",
                    color = PrimaryBlue
                )
            }
        }
    )
}

@Composable
fun EditClassDialog(
    schoolClass: ClassModel,
    teachers: List<ClassTeacherItem>,
    onDismiss: () -> Unit,
    onSave: (ClassModel) -> Unit
) {
    var className by remember { mutableStateOf(schoolClass.className) }
    var academicYear by remember { mutableStateOf(schoolClass.academicYear) }
    var description by remember { mutableStateOf(schoolClass.description) }

    var selectedTeacherName by remember {
        mutableStateOf(schoolClass.classTeacherName.ifBlank { "Not assigned" })
    }

    var selectedTeacherId by remember {
        mutableStateOf(schoolClass.classTeacherId)
    }

    var teacherExpanded by remember { mutableStateOf(false) }

    var classNameError by remember { mutableStateOf(false) }
    var academicYearError by remember { mutableStateOf(false) }

    val teacherItems = listOf("Not assigned") + teachers.map { teacher ->
        teacher.teacherName
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Class",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                SgrmsTextField(
                    value = className,
                    onValueChange = {
                        className = it
                        classNameError = false
                    },
                    label = "Class Name",
                    placeholder = "Example: 5 Amanah",
                    icon = Icons.Default.School,
                    isError = classNameError,
                    errorMessage = "Class name is required"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SgrmsTextField(
                    value = academicYear,
                    onValueChange = {
                        academicYear = it
                        academicYearError = false
                    },
                    label = "Academic Year",
                    placeholder = "Example: 2024/2025",
                    icon = Icons.Default.Badge,
                    isError = academicYearError,
                    errorMessage = "Academic year is required"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SgrmsDropdownField(
                    value = selectedTeacherName.ifBlank { "Not assigned" },
                    label = "Class Teacher",
                    placeholder = "Select class teacher",
                    expanded = teacherExpanded,
                    onExpandedChange = {
                        teacherExpanded = it
                    },
                    items = teacherItems,
                    onItemSelected = { selectedName ->
                        selectedTeacherName = selectedName

                        if (selectedName == "Not assigned") {
                            selectedTeacherId = ""
                        } else {
                            selectedTeacherId = teachers.find { teacher ->
                                teacher.teacherName == selectedName
                            }?.teacherId.orEmpty()
                        }

                        teacherExpanded = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SgrmsTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    label = "Description",
                    placeholder = "Optional class description",
                    icon = Icons.Default.MenuBook,
                    singleLine = false,
                    modifier = Modifier.height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    classNameError = className.isBlank()
                    academicYearError = academicYear.isBlank()

                    val isValid = !classNameError && !academicYearError

                    if (isValid) {
                        val updatedClass = schoolClass.copy(
                            className = className.trim(),
                            academicYear = academicYear.trim(),
                            description = description.trim(),
                            classTeacherId = selectedTeacherId,
                            classTeacherName = selectedTeacherName.ifBlank {
                                "Not assigned"
                            }
                        )

                        onSave(updatedClass)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                )
            ) {
                Text(
                    text = "Save Changes",
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel",
                    color = PrimaryBlue
                )
            }
        }
    )
}

@Composable
fun ClassCard(
    schoolClass: ClassModel,
    classTeacherName: String,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        imageVector = Icons.Default.School,
                        contentDescription = "Class",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = schoolClass.className,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Academic Year: ${schoolClass.academicYear}",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = BackgroundGrey)

            Spacer(modifier = Modifier.height(12.dp))

            SgrmsInfoRow(
                icon = Icons.Default.Person,
                label = "Class Teacher",
                value = classTeacherName
            )

            if (schoolClass.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                SgrmsInfoRow(
                    icon = Icons.Default.MenuBook,
                    label = "Description",
                    value = schoolClass.description
                )
            }

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

@Composable
fun ClassStudentsDialog(
    schoolClass: ClassModel,
    students: List<StudentModel>,
    onDismiss: () -> Unit,
    onAddStudentClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = schoolClass.className,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Students in this class (${students.size})",
                    fontSize = 13.sp,
                    color = TextGrey
                )
            }
        },
        text = {
            Column {
                Button(
                    onClick = onAddStudentClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Student",
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Add Student in This Class",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (students.isEmpty()) {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.Group,
                        title = "No student in this class",
                        message = "Tap the button above to add a student."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = students,
                            key = { student ->
                                student.studentId
                            }
                        ) { student ->
                            StudentInClassCard(student = student)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Close",
                    color = PrimaryBlue
                )
            }
        }
    )
}

@Composable
fun StudentInClassCard(
    student: StudentModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = BackgroundGrey
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (student.gender == "Female") {
                            Color(0xFFFF4081)
                        } else {
                            PrimaryBlue
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Student",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.size(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = student.studentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = student.studentCode,
                    fontSize = 13.sp,
                    color = TextGrey
                )
            }

            Text(
                text = student.status,
                fontSize = 12.sp,
                color = if (student.status == "Active") {
                    SuccessGreen
                } else {
                    Color.Red
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}