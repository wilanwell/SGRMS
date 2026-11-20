package com.example.sgrms.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sgrms.data.firebase.GradeFirebaseService
import com.example.sgrms.data.firebase.StudentFirebaseService
import com.example.sgrms.data.firebase.SubjectFirebaseService
import com.example.sgrms.data.repository.GradeFirebaseRepository
import com.example.sgrms.data.repository.StudentFirebaseRepository
import com.example.sgrms.data.repository.SubjectFirebaseRepository
import com.example.sgrms.ui.components.SgrmsButtonRow
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsFormCard
import com.example.sgrms.ui.components.SgrmsTextField
import com.example.sgrms.ui.components.TopBar
import com.example.sgrms.ui.grade.calculateGradeLetter
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.viewmodel.GradeFirebaseViewModel
import com.example.sgrms.viewmodel.GradeFirebaseViewModelFactory
import com.example.sgrms.viewmodel.StudentFirebaseViewModel
import com.example.sgrms.viewmodel.StudentFirebaseViewModelFactory
import com.example.sgrms.viewmodel.SubjectFirebaseViewModel
import com.example.sgrms.viewmodel.SubjectFirebaseViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun UpdateReportScreen(
    reportId: String = "",
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }

    val studentFirebaseService = remember { StudentFirebaseService() }
    val studentFirebaseRepository = remember {
        StudentFirebaseRepository(studentFirebaseService)
    }
    val studentViewModel: StudentFirebaseViewModel = viewModel(
        factory = StudentFirebaseViewModelFactory(studentFirebaseRepository)
    )

    val gradeFirebaseService = remember { GradeFirebaseService() }
    val gradeFirebaseRepository = remember {
        GradeFirebaseRepository(gradeFirebaseService)
    }
    val gradeViewModel: GradeFirebaseViewModel = viewModel(
        factory = GradeFirebaseViewModelFactory(gradeFirebaseRepository)
    )

    val subjectFirebaseService = remember { SubjectFirebaseService() }
    val subjectFirebaseRepository = remember {
        SubjectFirebaseRepository(subjectFirebaseService)
    }
    val subjectViewModel: SubjectFirebaseViewModel = viewModel(
        factory = SubjectFirebaseViewModelFactory(subjectFirebaseRepository)
    )

    val studentUiState by studentViewModel.uiState.collectAsState()
    val gradeUiState by gradeViewModel.uiState.collectAsState()
    val subjectUiState by subjectViewModel.uiState.collectAsState()

    var remarks by remember { mutableStateOf("") }
    var remarksError by remember { mutableStateOf(false) }
    var localMessage by remember { mutableStateOf("") }
    var localErrorMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(reportId) {
        if (reportId.isNotBlank()) {
            studentViewModel.loadStudentById(reportId)
            gradeViewModel.loadGradesByStudentId(reportId)
            subjectViewModel.loadSubjects()

            try {
                val remarkDocument = firestore
                    .collection("reportRemarks")
                    .document(reportId)
                    .get()
                    .await()

                remarks = remarkDocument.getString("remarks").orEmpty()
            } catch (e: Exception) {
                localErrorMessage = e.message ?: "Failed to load report remarks"
            }
        }
    }

    val student = studentUiState.selectedStudent
    val allGrades = gradeUiState.grades

    val completedGrades = allGrades.filter { grade ->
        grade.status.equals("Complete", ignoreCase = true)
    }

    val totalMarks = completedGrades.sumOf { grade ->
        grade.marks
    }

    val averageMarks = if (completedGrades.isNotEmpty()) {
        totalMarks / completedGrades.size
    } else {
        0.0
    }

    val overallGrade = if (completedGrades.isNotEmpty()) {
        calculateGradeLetter(averageMarks.toString())
    } else {
        "-"
    }

    val isLoading =
        studentUiState.isLoading ||
                gradeUiState.isLoading ||
                subjectUiState.isLoading ||
                isSaving

    val errorMessage = when {
        localErrorMessage.isNotBlank() -> localErrorMessage
        studentUiState.errorMessage.isNotBlank() -> studentUiState.errorMessage
        gradeUiState.errorMessage.isNotBlank() -> gradeUiState.errorMessage
        subjectUiState.errorMessage.isNotBlank() -> subjectUiState.errorMessage
        else -> ""
    }

    fun clearMessages() {
        remarksError = false
        localMessage = ""
        localErrorMessage = ""
        studentViewModel.clearMessage()
        gradeViewModel.clearMessage()
        subjectViewModel.clearMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGrey)
            .navigationBarsPadding()
    ) {
        TopBar(
            title = "Update Report",
            showBack = true,
            onBackClick = onBackClick,
            showIcon = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 32.dp
                )
        ) {
            when {
                isLoading && student == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue
                        )
                    }
                }

                reportId.isBlank() || student == null -> {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.Summarize,
                        title = "Report not found",
                        message = if (errorMessage.isNotBlank()) {
                            errorMessage
                        } else {
                            "The selected report could not be loaded."
                        }
                    )
                }

                else -> {
                    SgrmsFormCard(
                        title = "Report Information",
                        subtitle = "Update additional report remarks",
                        icon = Icons.Default.Edit
                    ) {
                        SgrmsTextField(
                            value = student.studentName.ifBlank { "Unknown Student" },
                            onValueChange = {},
                            label = "Student Name",
                            placeholder = "Student name",
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = student.className.ifBlank { "Not assigned" },
                            onValueChange = {},
                            label = "Class",
                            placeholder = "Class",
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = completedGrades.size.toString(),
                            onValueChange = {},
                            label = "Completed Subjects",
                            placeholder = "Completed subjects",
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = "%.2f".format(totalMarks),
                            onValueChange = {},
                            label = "Total Marks",
                            placeholder = "Total marks",
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = "%.2f%%".format(averageMarks),
                            onValueChange = {},
                            label = "Average Marks",
                            placeholder = "Average marks",
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = overallGrade,
                            onValueChange = {},
                            label = "Overall Grade",
                            placeholder = "Overall grade",
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SgrmsTextField(
                            value = remarks,
                            onValueChange = {
                                remarks = it
                                clearMessages()
                            },
                            label = "Additional Remarks",
                            placeholder = "Enter additional report remarks",
                            singleLine = false,
                            modifier = Modifier.height(130.dp),
                            isError = remarksError,
                            errorMessage = "Remarks are required"
                        )

                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (localMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = localMessage,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        SgrmsButtonRow(
                            cancelText = "Cancel",
                            confirmText = if (isSaving) {
                                "Saving..."
                            } else {
                                "Save"
                            },
                            onCancelClick = onBackClick,
                            onConfirmClick = {
                                remarksError = remarks.isBlank()

                                if (!remarksError) {
                                    coroutineScope.launch {
                                        try {
                                            isSaving = true
                                            localErrorMessage = ""
                                            localMessage = ""

                                            val remarkData = hashMapOf(
                                                "studentId" to reportId,
                                                "remarks" to remarks.trim(),
                                                "updatedAt" to System.currentTimeMillis()
                                            )

                                            firestore
                                                .collection("reportRemarks")
                                                .document(reportId)
                                                .set(remarkData)
                                                .await()

                                            isSaving = false
                                            localMessage = "Report remarks updated successfully"
                                            onSaveClick()
                                        } catch (e: Exception) {
                                            isSaving = false
                                            localErrorMessage =
                                                e.message ?: "Failed to update report remarks"
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}