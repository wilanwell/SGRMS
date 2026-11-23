package com.wilanwell.sgrms.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.wilanwell.sgrms.data.model.ClassModel
import com.wilanwell.sgrms.data.model.StudentModel
import com.wilanwell.sgrms.ui.components.SgrmsDropdownField
import com.wilanwell.sgrms.ui.components.SgrmsEmptyStateCard
import com.wilanwell.sgrms.ui.components.SgrmsTextField
import com.wilanwell.sgrms.ui.theme.ColorBackground
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import com.wilanwell.sgrms.ui.theme.SgrmsRadius

data class ClassTeacherItem(
    val teacherId: String = "",
    val teacherName: String = ""
)

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
                    containerColor = ColorPrimary
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
                    color = ColorPrimary
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
                    containerColor = ColorPrimary
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
                    color = ColorPrimary
                )
            }
        }
    )
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
                    color = ColorOnSurfaceVariant
                )
            }
        },
        text = {
            Column {
                Button(
                    onClick = onAddStudentClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary
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
                    color = ColorPrimary
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
        shape = RoundedCornerShape(SgrmsRadius.Field),
        colors = CardDefaults.cardColors(
            containerColor = ColorBackground
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
                            SgrmsColors.Pink
                        } else {
                            ColorPrimary
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
                    color = ColorOnSurfaceVariant
                )
            }

            Text(
                text = student.status,
                fontSize = 12.sp,
                color = if (student.status == "Active") {
                    SgrmsColors.Success
                } else {
                    SgrmsColors.Error
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}
