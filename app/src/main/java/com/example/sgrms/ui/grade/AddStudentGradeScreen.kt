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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.firebase.GradeFirebaseService
import com.example.sgrms.data.firebase.StudentFirebaseService
import com.example.sgrms.data.firebase.SubjectFirebaseService
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.SuccessGreen
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.ui.theme.WarningOrange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.sgrms.ui.theme.markColor
import com.example.sgrms.utils.GradeCalculator

data class AddStudentGradeSubjectItem(
    val subjectId: String,
    val subjectName: String,
    val subjectCode: String,
    val existingGradeId: String = "",
    val marksText: String = "",
    val remarks: String = "",
    val status: String = "Pending",
    val message: String = "",
    val isSaving: Boolean = false
)

@Composable
fun AddStudentGradeScreen(
    studentId: String = "",
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    val firestore = remember {
        FirebaseFirestore.getInstance()
    }

    val studentFirebaseService = remember {
        StudentFirebaseService()
    }

    val subjectFirebaseService = remember {
        SubjectFirebaseService()
    }

    val gradeFirebaseService = remember {
        GradeFirebaseService()
    }

    var student by remember {
        mutableStateOf<StudentModel?>(null)
    }

    val subjectItems = remember {
        mutableStateListOf<AddStudentGradeSubjectItem>()
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    LaunchedEffect(studentId) {
        try {
            isLoading = true
            errorMessage = ""

            if (studentId.isBlank()) {
                errorMessage = "Student ID is missing."
                return@LaunchedEffect
            }

            val loadedStudent = studentFirebaseService.getStudentById(studentId)

            if (loadedStudent == null) {
                errorMessage = "Student not found."
                return@LaunchedEffect
            }

            student = loadedStudent

            val subjects = subjectFirebaseService.getAllSubjects()
            val existingGrades = gradeFirebaseService.getGradesByStudentId(studentId)

            subjectItems.clear()

            subjects.forEach { subject ->
                val existingGrade = existingGrades.find { grade ->
                    grade.subjectId == subject.subjectId
                }

                subjectItems.add(
                    AddStudentGradeSubjectItem(
                        subjectId = subject.subjectId,
                        subjectName = subject.subjectName,
                        subjectCode = subject.subjectCode,
                        existingGradeId = existingGrade?.gradeId ?: "",
                        marksText = if (existingGrade != null && existingGrade.marks > 0) {
                            existingGrade.marks.toString()
                        } else {
                            ""
                        },
                        remarks = existingGrade?.remarks ?: "",
                        status = existingGrade?.status ?: "Pending"
                    )
                )
            }
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "Failed to load subjects."
        } finally {
            isLoading = false
        }
    }

    fun updateSubjectItem(
        index: Int,
        updatedItem: AddStudentGradeSubjectItem
    ) {
        if (index in subjectItems.indices) {
            subjectItems[index] = updatedItem
        }
    }

    fun saveGradeForSubject(
        index: Int,
        item: AddStudentGradeSubjectItem
    ) {
        val currentStudent = student

        if (currentStudent == null) {
            updateSubjectItem(
                index = index,
                updatedItem = item.copy(
                    message = "Student not found."
                )
            )
            return
        }

        val marks = item.marksText.toDoubleOrNull()

        if (marks == null || marks < 0 || marks > 100) {
            updateSubjectItem(
                index = index,
                updatedItem = item.copy(
                    message = "Please enter marks between 0 and 100."
                )
            )
            return
        }

        coroutineScope.launch {
            try {
                updateSubjectItem(
                    index = index,
                    updatedItem = item.copy(
                        isSaving = true,
                        message = ""
                    )
                )

                val gradeDocument = if (item.existingGradeId.isNotBlank()) {
                    firestore
                        .collection("grades")
                        .document(item.existingGradeId)
                } else {
                    firestore
                        .collection("grades")
                        .document()
                }

                val gradeId = gradeDocument.id
                val gradeLetter = GradeCalculator.gradeLetter(marks)
                val now = System.currentTimeMillis()

                val gradeData = mapOf(
                    "gradeId" to gradeId,
                    "studentId" to currentStudent.studentId,
                    "studentName" to currentStudent.studentName,
                    "studentCode" to currentStudent.studentCode,
                    "classId" to currentStudent.classId,
                    "className" to currentStudent.className,
                    "subjectId" to item.subjectId,
                    "subjectName" to item.subjectName,
                    "subjectCode" to item.subjectCode,
                    "marks" to marks,
                    "gradeLetter" to gradeLetter,
                    "remarks" to item.remarks.trim(),
                    "status" to "Complete",
                    "updatedAt" to now,
                    "createdAt" to now
                )

                gradeDocument
                    .set(gradeData)
                    .await()

                val notificationDocument = firestore
                    .collection("notifications")
                    .document()

                notificationDocument
                    .set(
                        mapOf(
                            "notificationId" to notificationDocument.id,
                            "studentId" to currentStudent.studentId,
                            "title" to "Grade Updated",
                            "message" to "Your ${item.subjectName} grade has been updated.",
                            "type" to "GRADE_UPDATED",
                            "relatedGradeId" to gradeId,
                            "relatedSubjectId" to item.subjectId,
                            "isRead" to false,
                            "read" to false,
                            "createdAt" to now
                        )
                    )
                    .await()

                updateSubjectItem(
                    index = index,
                    updatedItem = item.copy(
                        existingGradeId = gradeId,
                        status = "Complete",
                        isSaving = false,
                        message = "Grade saved successfully."
                    )
                )
            } catch (exception: Exception) {
                updateSubjectItem(
                    index = index,
                    updatedItem = item.copy(
                        isSaving = false,
                        message = exception.message ?: "Failed to save grade."
                    )
                )
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopBar(
                title = "Add Student Grade",
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
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                errorMessage.isNotBlank() -> {
                    item {
                        SgrmsEmptyStateCard(
                            icon = Icons.Default.Person,
                            title = "Unable to load",
                            message = errorMessage
                        )
                    }
                }

                subjectItems.isEmpty() -> {
                    item {
                        SgrmsEmptyStateCard(
                            icon = Icons.Default.MenuBook,
                            title = "No subjects found",
                            message = "There are no subjects available for this student."
                        )
                    }
                }

                else -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = PrimaryBlue
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 3.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                Text(
                                    text = student?.studentName ?: "Unknown Student",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = student?.className ?: "Unknown Class",
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Subjects",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Enter marks for the subjects below.",
                            fontSize = 14.sp,
                            color = TextGrey
                        )
                    }

                    itemsIndexed(
                        items = subjectItems,
                        key = { index, item ->
                            item.existingGradeId.ifBlank {
                                "${item.subjectId}-$index"
                            }
                        }
                    ) { index, item ->
                        AddStudentGradeSubjectCard(
                            item = item,
                            onMarksChange = { newMarks ->
                                updateSubjectItem(
                                    index = index,
                                    updatedItem = item.copy(
                                        marksText = newMarks,
                                        message = ""
                                    )
                                )
                            },
                            onRemarksChange = { newRemarks ->
                                updateSubjectItem(
                                    index = index,
                                    updatedItem = item.copy(
                                        remarks = newRemarks,
                                        message = ""
                                    )
                                )
                            },
                            onSaveClick = {
                                saveGradeForSubject(
                                    index = index,
                                    item = item
                                )
                            }
                        )
                    }

                    item {
                        Button(
                            onClick = onSaveSuccess,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue
                            )
                        ) {
                            Text(
                                text = "Done",
                                color = Color.White,
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

@Composable
private fun AddStudentGradeSubjectCard(
    item: AddStudentGradeSubjectItem,
    onMarksChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    val marks = item.marksText.toDoubleOrNull()
    val gradeLetter = if (marks != null) {
        GradeCalculator.gradeLetter(marks)
    } else {
        "-"
    }

    val gradeColor = if (marks == null) TextGrey else markColor(marks)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = PrimaryBlue.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Subject",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(27.dp)
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.subjectName.ifBlank { "Unknown Subject" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = item.subjectCode.ifBlank { "-" },
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Text(
                    text = gradeLetter,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = gradeColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = item.marksText,
                onValueChange = { value ->
                    val filtered = value.filter { character ->
                        character.isDigit() || character == '.'
                    }

                    onMarksChange(filtered)
                },
                label = {
                    Text("Marks")
                },
                placeholder = {
                    Text("0 - 100")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Marks"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !item.isSaving,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    focusedLabelColor = PrimaryBlue,
                    cursorColor = PrimaryBlue
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = item.remarks,
                onValueChange = onRemarksChange,
                label = {
                    Text("Remarks")
                },
                placeholder = {
                    Text("Optional remarks")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = !item.isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    focusedLabelColor = PrimaryBlue,
                    cursorColor = PrimaryBlue
                )
            )

            if (item.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = item.message,
                    color = if (item.message.contains("success", ignoreCase = true)) {
                        SuccessGreen
                    } else {
                        Color.Red
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSaveClick,
                enabled = !item.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                )
            ) {
                if (item.isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = if (item.existingGradeId.isBlank()) {
                            "Add Grade"
                        } else {
                            "Update Grade"
                        },
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

