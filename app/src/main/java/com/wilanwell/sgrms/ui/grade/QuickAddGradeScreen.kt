package com.wilanwell.sgrms.ui.grade

import com.wilanwell.sgrms.ui.components.LocalSessionAccess
import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import com.wilanwell.sgrms.utils.isComplete
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilanwell.sgrms.ui.components.SgrmsDropdownField
import com.wilanwell.sgrms.ui.components.SgrmsEmptyStateCard
import com.wilanwell.sgrms.ui.components.SgrmsInfoRow
import com.wilanwell.sgrms.ui.components.TopBar
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.ui.theme.ColorSurface
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import com.wilanwell.sgrms.viewmodel.GradeBulkEntry
import com.wilanwell.sgrms.viewmodel.GradeFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel
import com.wilanwell.sgrms.utils.GradeCalculator
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.SgrmsRadius

@Composable
fun QuickAddGradeScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val noMarksChangedEnterOrChangeText = stringResource(R.string.no_marks_changed_enter_or_change)
    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()

    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()

    val gradeViewModel: GradeFirebaseViewModel = hiltViewModel()

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
    // Teachers grade only the subjects they teach (enforced by firestore.rules).
    val access = LocalSessionAccess.current
    val subjects = subjectUiState.subjects.filter { access.canWriteSubject(it.teacherId) }

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
            student.className.ifBlank { stringResource(R.string.not_assigned) }
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
            student.className.ifBlank { stringResource(R.string.not_assigned) } == selectedClassName
        }
        .sortedBy { student ->
            student.studentName
        }

    fun existingGradeFor(studentId: String) = gradeUiState.grades.firstOrNull { grade ->
        grade.studentId == studentId && grade.subjectId == selectedSubjectId
    }

    // Show the marks already saved for this class and subject, so the teacher
    // sees them and only changes what is new.
    LaunchedEffect(selectedClassName, selectedSubjectId, gradeUiState.grades) {
        if (selectedSubjectId.isBlank()) return@LaunchedEffect
        classStudents.forEach { student ->
            val existing = existingGradeFor(student.studentId)
            if (student.studentId !in marksMap && existing != null && existing.isComplete) {
                marksMap[student.studentId] = GradeCalculator.formatMarks(existing.marks)
            }
        }
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
                title = stringResource(R.string.quick_add_grade),
                showBack = true,
                onBackClick = onBackClick,
                showIcon = false
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorBackground)
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
                    shape = RoundedCornerShape(SgrmsRadius.Card),
                    colors = CardDefaults.cardColors(containerColor = ColorSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = stringResource(R.string.quick_grade),
                                tint = ColorPrimary,
                                modifier = Modifier.size(30.dp)
                            )

                            Spacer(modifier = Modifier.size(10.dp))

                            Column {
                                Text(
                                    text = stringResource(R.string.bulk_grade_entry),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = stringResource(R.string.select_class_and_subject_then_enter),
                                    fontSize = 14.sp,
                                    color = ColorOnSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        SgrmsDropdownField(
                            value = selectedClassName.ifBlank { stringResource(R.string.select_class) },
                            label = stringResource(R.string.class_label),
                            placeholder = stringResource(R.string.select_class),
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
                                text = stringResource(R.string.class_is_required),
                                color = SgrmsColors.Error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsDropdownField(
                            value = selectedSubjectName.ifBlank { stringResource(R.string.select_subject) },
                            label = stringResource(R.string.subject),
                            placeholder = stringResource(R.string.select_subject),
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
                                    // Marks typed for the previous subject must not carry over.
                                    marksMap.clear()
                                }

                                subjectExpanded = false
                            }
                        )

                        if (subjectError) {
                            Text(
                                text = stringResource(R.string.subject_is_required),
                                color = SgrmsColors.Error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SgrmsInfoRow(
                            icon = Icons.Default.Person,
                            label = stringResource(R.string.students_found),
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
                                color = SgrmsColors.Error,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.student_marks),
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
                                color = ColorPrimary
                            )
                        }
                    }
                }

                !canShowStudents -> {
                    item {
                        SgrmsEmptyStateCard(
                            icon = Icons.Default.School,
                            title = stringResource(R.string.select_class_and_subject),
                            message = stringResource(R.string.choose_a_class_and_subject_before)
                        )
                    }
                }

                classStudents.isEmpty() -> {
                    item {
                        SgrmsEmptyStateCard(
                            icon = Icons.Default.Person,
                            title = stringResource(R.string.no_students_in_this_class),
                            message = stringResource(R.string.add_students_to_this_class_first)
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

                    // Repeat the marks message next to the button, where the teacher is looking.
                    if (marksErrorMessage.isNotBlank()) {
                        item {
                            Text(
                                text = marksErrorMessage,
                                color = SgrmsColors.Error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
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
                                        val existing = existingGradeFor(student.studentId)
                                        val unchanged = existing != null && existing.isComplete && existing.marks == marks

                                        // Empty, or the same mark that is already saved: nothing to save.
                                        if (marks == null || unchanged) {
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
                                        marksErrorMessage = noMarksChangedEnterOrChangeText
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
                            shape = RoundedCornerShape(SgrmsRadius.Field),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorPrimary,
                                disabledContainerColor = ColorPrimary.copy(alpha = 0.6f)
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
                                    contentDescription = stringResource(R.string.save_grades),
                                    tint = Color.White
                                )

                                Spacer(modifier = Modifier.size(8.dp))

                                Text(
                                    text = stringResource(R.string.save_all_grades),
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
                                text = stringResource(R.string.cancel),
                                color = ColorPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
