package com.wilanwell.sgrms.ui.admin

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilanwell.sgrms.data.model.StudentModel
import com.wilanwell.sgrms.ui.components.SgrmsEmptyStateCard
import com.wilanwell.sgrms.ui.components.SgrmsSearchBar
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import androidx.compose.material.icons.filled.Add
import com.wilanwell.sgrms.ui.theme.SgrmsRadius

@Composable
fun AddStudentToClassDialog(
    className: String,
    students: List<StudentModel>,
    onDismiss: () -> Unit,
    onStudentSelected: (StudentModel) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    val filteredStudents = students.filter { student ->
        student.studentName.contains(searchText, ignoreCase = true) ||
                student.studentCode.contains(searchText, ignoreCase = true) ||
                student.gender.contains(searchText, ignoreCase = true) ||
                student.status.contains(searchText, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = stringResource(R.string.add_student_title),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.choose_an_unassigned_student_to_add, className),
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
                    label = stringResource(R.string.search_student),
                    placeholder = stringResource(R.string.search_by_name_or_code)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredStudents.isEmpty()) {
                    SgrmsEmptyStateCard(
                        icon = Icons.Default.Group,
                        title = stringResource(R.string.no_available_student),
                        message = stringResource(R.string.no_unassigned_students_are_available)
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
                            AddStudentSelectionCard(
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
                    text = stringResource(R.string.close),
                    color = ColorPrimary
                )
            }
        }
    )
}

@Composable
fun AddStudentSelectionCard(
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
                    .clip(CircleShape)
                    .background(
                        if (student.gender == "Female") {
                            SgrmsColors.Pink
                        } else {
                            ColorPrimary
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.student),
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
                    text = stringResource(R.string.code, student.studentCode),
                    fontSize = 13.sp,
                    color = ColorOnSurfaceVariant
                )

                Text(
                    text = stringResource(R.string.current_class_value, student.className.ifBlank { stringResource(R.string.not_assigned) }),
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
                    contentDescription = stringResource(R.string.add_student_title),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun RemoveStudentFromClassDialog(
    student: StudentModel,
    onDismiss: () -> Unit,
    onConfirmRemove: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.remove_student),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.are_you_sure_you_want_to, student.studentName)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmRemove,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SgrmsColors.Error
                )
            ) {
                Text(
                    text = stringResource(R.string.remove),
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = ColorPrimary
                )
            }
        }
    )
}
