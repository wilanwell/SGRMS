package com.example.sgrms.ui.grade

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sgrms.data.firebase.GradeFirebaseService
import com.example.sgrms.data.firebase.NotificationFirebaseService
import com.example.sgrms.data.firebase.StudentFirebaseService
import com.example.sgrms.data.firebase.SubjectFirebaseService
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.data.repository.GradeFirebaseRepository
import com.example.sgrms.data.repository.NotificationFirebaseRepository
import com.example.sgrms.data.repository.StudentFirebaseRepository
import com.example.sgrms.data.repository.SubjectFirebaseRepository
import com.example.sgrms.ui.components.SgrmsDropdownField
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsInfoRow
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.SuccessGreen
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.viewmodel.GradeBulkEntry
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.GradeFirebaseViewModelFactory
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModelFactory
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModelFactory
import com.example.sgrms.utils.GradeCalculator

@Composable
fun QuickAddGradeScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val studentFirebaseService = remember {
        StudentFirebaseService()
    }

    val studentFirebaseRepository = remember {
        StudentFirebaseRepository(studentFirebaseService)
    }

    val studentViewModel: StudentFirebaseViewModel = viewModel(
        factory = StudentFirebaseViewModelFactory(studentFirebaseRepository)
    )

    val subjectFirebaseService = remember {
        SubjectFirebaseService()
    }

    val subjectFirebaseRepository = remember {
        SubjectFirebaseRepository(subjectFirebaseService)
    }

    val subjectViewModel: SubjectFirebaseViewModel = viewModel(
        factory = SubjectFirebaseViewModelFactory(subjectFirebaseRepository)
    )

    val gradeFirebaseService = remember {
        GradeFirebaseService()
    }

    val gradeFirebaseRepository = remember {
        GradeFirebaseRepository(gradeFirebaseService)
    }

    val notificationFirebaseService = remember {
        NotificationFirebaseService()
    }

    val notificationFirebaseRepository = remember {
        NotificationFirebaseRepository(notificationFirebaseService)
    }

    val gradeViewModel: GradeFirebaseViewModel = viewModel(
        factory = GradeFirebaseViewModelFactory(
            gradeRepository = gradeFirebaseRepository,
            notificationRepository = notificationFirebaseRepository
        )
    )

    val studentUiState by studentViewModel.uiState.collectAsState()
    val subjectUiState by subjectViewModel.uiState.collectAsState()
    val gradeUiState by gradeViewModel.uiState.collectAsState()

    var selectedClassName by remember { mutableStateOf("") }
    var selectedSubjectName by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf("") }

    var classExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }

    var classError by remember { mutableStateOf(false) }
    var subjectError by remember { mutableStateOf(false) }
    var marksErrorMessage by remember { mutableStateOf("") }

    val marksMap = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(Unit) {
        studentViewModel.loadStudents()
        subjectViewModel.loadSubjects()
        gradeViewModel.loadGrades()
    }

    val students = studentUiState.students
    val subjects = subjectUiState.subjects

    val isLoading =
        studentUiState.isLoading ||
                subjectUiState.isLoading ||
                gradeUiState.isLoading

    val errorMessage = when {
        studentUiState.errorMessage.isNotBlank() -> studentUiState.errorMessage
        subjectUiState.errorMessage.isNotBlank() -> subjectUiState.errorMessage
        gradeUiState.errorMessage.isNotBlank() -> gradeUiState.errorMessage
        marksErrorMessage.isNotBlank() -> marksErrorMessage
        else -> ""
    }

    val classList = students
        .map { student ->
            student.className.ifBlank { "Not assigned" }
        }
        .distinct()
        .sorted()

    val classDropdownList = if (classList.isEmpty()) {
        listOf("No class available")
    } else {
        classList
    }

    val subjectList = subjects
        .map { subject ->
            subject.subjectName
        }
        .filter { subjectName ->
            subjectName.isNotBlank()
        }
        .distinct()
        .sorted()

    val subjectDropdownList = if (subjectList.isEmpty()) {
        listOf("No subject available")
    } else {
        subjectList
    }

    val classStudents = students
        .filter { student ->
            student.className.ifBlank { "Not assigned" } == selectedClassName
        }
        .sortedBy { student ->
            student.studentName
        }

    val canShowStudents =
        selectedClassName.isNotBlank() &&
                selectedClassName != "No class available" &&
                selectedSubjectId.isNotBlank() &&
                selectedSubjectName != "No subject available"

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Quick Add Grade",
                showBack = true,
                onBackClick = onBackClick,
                showIcon = false
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGrey)
                .navigationBarsPadding()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Quick Grade",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(30.dp)
                            )

                            Spacer(modifier = Modifier.size(10.dp))

                            Column {
                                Text(
                                    text = "Bulk Grade Entry",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Select class and subject, then enter marks for students",
                                    fontSize = 14.sp,
                                    color = TextGrey
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        SgrmsDropdownField(
                            value = selectedClassName.ifBlank { "Select class" },
                            label = "Class",
                            placeholder = "Select class",
                            expanded = classExpanded,
                            onExpandedChange = {
                                classExpanded = it
                            },
                            items = classDropdownList,
                            onItemSelected = { className ->
                                if (className != "No class available") {
                                    selectedClassName = className
                                    classError = false
                                    marksErrorMessage = ""
                                    marksMap.clear()
                                }

                                classExpanded = false
                            }
                        )

                        if (classError) {
                            Text(
                                text = "Class is required",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsDropdownField(
                            value = selectedSubjectName.ifBlank { "Select subject" },
                            label = "Subject",
                            placeholder = "Select subject",
                            expanded = subjectExpanded,
                            onExpandedChange = {
                                subjectExpanded = it
                            },
                            items = subjectDropdownList,
                            onItemSelected = { subjectName ->
                                if (subjectName != "No subject available") {
                                    val selectedSubject = subjects.find { subject ->
                                        subject.subjectName == subjectName
                                    }

                                    selectedSubjectName = subjectName
                                    selectedSubjectId = selectedSubject?.subjectId ?: ""
                                    subjectError = false
                                    marksErrorMessage = ""
                                }

                                subjectExpanded = false
                            }
                        )

                        if (subjectError) {
                            Text(
                                text = "Subject is required",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsInfoRow(
                            icon = Icons.Default.Person,
                            label = "Students Found",
                            value = if (selectedClassName.isBlank() || selectedClassName == "No class available") {
                                "-"
                            } else {
                                classStudents.size.toString()
                            }
                        )

                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Student Marks",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            when {
                isLoading -> {
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
                }

                !canShowStudents -> {
                    item {
                        SgrmsEmptyStateCard(
                            icon = Icons.Default.School,
                            title = "Select class and subject",
                            message = "Choose a class and subject before entering marks."
                        )
                    }
                }

                classStudents.isEmpty() -> {
                    item {
                        SgrmsEmptyStateCard(
                            icon = Icons.Default.Person,
                            title = "No students in this class",
                            message = "Add students to this class first before entering grades."
                        )
                    }
                }

                else -> {
                    items(
                        items = classStudents,
                        key = { student ->
                            student.studentId
                        }
                    ) { student ->
                        QuickGradeStudentCard(
                            student = student,
                            marks = marksMap[student.studentId] ?: "",
                            onMarksChange = { newMarks ->
                                if (
                                    newMarks.isEmpty() ||
                                    newMarks.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?$"))
                                ) {
                                    val marksValue = newMarks.toDoubleOrNull()

                                    if (marksValue == null || marksValue <= 100.0) {
                                        marksMap[student.studentId] = newMarks
                                        marksErrorMessage = ""
                                    }
                                }
                            }
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                classError = selectedClassName.isBlank() ||
                                        selectedClassName == "No class available"

                                subjectError = selectedSubjectId.isBlank() ||
                                        selectedSubjectName == "No subject available"

                                marksErrorMessage = ""

                                val isValid = !classError && !subjectError

                                if (isValid) {
                                    val gradeEntries = classStudents.mapNotNull { student ->
                                        val marksText = marksMap[student.studentId].orEmpty()
                                        val marks = marksText.toDoubleOrNull()

                                        if (marks == null) {
                                            null
                                        } else {
                                            GradeBulkEntry(
                                                studentId = student.studentId,
                                                subjectId = selectedSubjectId,
                                                marks = marks,
                                                gradeLetter = GradeCalculator.gradeLetter(marks),
                                                status = "Complete",
                                                remarks = "",
                                                createdByTeacherId = "",
                                                subjectName = selectedSubjectName
                                            )
                                        }
                                    }

                                    if (gradeEntries.isEmpty()) {
                                        marksErrorMessage = "Enter at least one valid mark before saving."
                                    } else {
                                        gradeViewModel.saveBulkGrades(
                                            gradeEntries = gradeEntries,
                                            onSuccess = onSaveClick
                                        )
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                disabledContainerColor = PrimaryBlue.copy(alpha = 0.6f)
                            )
                        ) {
                            if (gradeUiState.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save Grades",
                                    tint = Color.White
                                )

                                Spacer(modifier = Modifier.size(8.dp))

                                Text(
                                    text = "Save All Grades",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    item {
                        TextButton(
                            onClick = onBackClick,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cancel",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickGradeStudentCard(
    student: StudentModel,
    marks: String,
    onMarksChange: (String) -> Unit
) {
    val marksValue = marks.toDoubleOrNull()

    val gradeLetter = if (marksValue != null) {
        GradeCalculator.gradeLetter(marksValue)
    } else {
        "-"
    }

    val gradeColor = when (gradeLetter) {
        "A+", "A" -> SuccessGreen
        "B" -> PrimaryBlue
        "C" -> Color(0xFFFF9800)
        "D" -> Color(0xFFFF5722)
        "E" -> Color.Red
        else -> TextGrey
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        .size(46.dp)
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

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = student.studentName.ifBlank { "Unknown Student" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Code: ${student.studentCode.ifBlank { "-" }}",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Text(
                    text = gradeLetter,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = gradeColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = BackgroundGrey)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = marks,
                onValueChange = onMarksChange,
                label = {
                    Text("Marks")
                },
                placeholder = {
                    Text("0 - 100")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = "Marks"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    focusedLabelColor = PrimaryBlue,
                    cursorColor = PrimaryBlue
                )
            )
        }
    }
}