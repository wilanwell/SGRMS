package com.wilanwell.sgrms.ui.grade

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilanwell.sgrms.ui.components.SgrmsEmptyStateCard
import com.wilanwell.sgrms.ui.components.TopBar
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.ui.theme.ColorSurface
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import com.wilanwell.sgrms.ui.theme.markColor
import com.wilanwell.sgrms.utils.GradeCalculator
import com.wilanwell.sgrms.viewmodel.AddStudentGradeSubjectItem
import com.wilanwell.sgrms.viewmodel.AddStudentGradeViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.wilanwell.sgrms.ui.theme.SgrmsRadius
import com.wilanwell.sgrms.ui.components.sgrmsFieldColors

@Composable
fun AddStudentGradeScreen(
    studentId: String = "",
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    val viewModel: AddStudentGradeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val student = uiState.student
    val subjectItems = uiState.subjectItems
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    LaunchedEffect(studentId) {
        viewModel.load(studentId)
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
                .background(ColorBackground)
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
                                color = ColorPrimary
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
                            shape = RoundedCornerShape(SgrmsRadius.Card),
                            colors = CardDefaults.cardColors(
                                containerColor = ColorPrimary
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            color = ColorOnSurfaceVariant
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
                                viewModel.onMarksChange(index, newMarks)
                            },
                            onRemarksChange = { newRemarks ->
                                viewModel.onRemarksChange(index, newRemarks)
                            },
                            onSaveClick = {
                                viewModel.saveGrade(index)
                            }
                        )
                    }

                    item {
                        Button(
                            onClick = onSaveSuccess,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(SgrmsRadius.Field),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorPrimary
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

    val gradeColor = if (marks == null) ColorOnSurfaceVariant else markColor(marks)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SgrmsRadius.Card),
        colors = CardDefaults.cardColors(
            containerColor = ColorSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            color = ColorPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(SgrmsRadius.Field)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Subject",
                        tint = ColorPrimary,
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
                        color = ColorOnSurfaceVariant
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
                shape = RoundedCornerShape(SgrmsRadius.Field),
                colors = sgrmsFieldColors()
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
                shape = RoundedCornerShape(SgrmsRadius.Field),
                colors = sgrmsFieldColors()
            )

            if (item.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = item.message,
                    color = if (item.message.contains("success", ignoreCase = true)) {
                        SgrmsColors.Success
                    } else {
                        SgrmsColors.Error
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
                shape = RoundedCornerShape(SgrmsRadius.Field),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary
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

