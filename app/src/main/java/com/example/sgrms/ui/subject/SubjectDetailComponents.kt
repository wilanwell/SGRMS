package com.example.sgrms.ui.subject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.ui.components.SgrmsEmptyStateCard
import com.example.sgrms.ui.components.SgrmsSearchBar
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import androidx.compose.material.icons.filled.Add
import com.example.sgrms.ui.theme.SgrmsRadius
import com.example.sgrms.ui.theme.SgrmsColors

@Composable
fun AddStudentToSubjectDialog(
    subjectName: String,
    students: List<StudentModel>,
    onDismiss: () -> Unit,
    onStudentSelected: (StudentModel) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    val filteredStudents = students.filter { student ->
        student.studentName.contains(searchText, ignoreCase = true) ||
                student.studentCode.contains(searchText, ignoreCase = true) ||
                student.className.contains(searchText, ignoreCase = true) ||
                student.gender.contains(searchText, ignoreCase = true) ||
                student.status.contains(searchText, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Add Student",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Choose a student to add into $subjectName",
                    fontSize = 13.sp,
                    color = ColorOnSurfaceVariant
                )
            }
        },
        text = {
            Column {
                SgrmsSearchBar(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    label = "Search Student",
                    placeholder = "Search by name, code, or class"
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredStudents.isEmpty()) {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.Group,
                        title = "No student found",
                        message = if (searchText.isBlank()) {
                            "All students are already added to this subject."
                        } else {
                            "No student matches your search."
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(360.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = filteredStudents,
                            key = { student ->
                                student.studentId
                            }
                        ) { student ->
                            AddSubjectStudentSelectionCard(
                                student = student,
                                onClick = {
                                    onStudentSelected(student)
                                }
                            )
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
                    color = ColorPrimary
                )
            }
        }
    )
}

@Composable
fun AddSubjectStudentSelectionCard(
    student: StudentModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SgrmsRadius.Field),
        colors = CardDefaults.cardColors(containerColor = ColorBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    .background(
                        color = if (student.gender == "Female") {
                            SgrmsColors.Pink
                        } else {
                            ColorPrimary
                        },
                        shape = CircleShape
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
                    text = student.studentName.ifBlank { "Unknown Student" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Code: ${student.studentCode.ifBlank { "-" }}",
                    fontSize = 13.sp,
                    color = ColorOnSurfaceVariant
                )

                Text(
                    text = "Class: ${student.className.ifBlank { "Not assigned" }}",
                    fontSize = 12.sp,
                    color = ColorOnSurfaceVariant
                )
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Student",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
