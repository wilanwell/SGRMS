package com.example.sgrms.ui.student

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.core.content.FileProvider
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.ui.components.SgrmsActionButtonRow
import com.example.sgrms.ui.theme.ColorSurface
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.SgrmsColors
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import java.io.File

@Composable
fun StudentCard(
    student: StudentModel,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val gender = student.gender.ifBlank { "Not specified" }
    val status = student.status.ifBlank { "Active" }

    val profileColor = if (gender == "Female") {
        Color(0xFFFF4081)
    } else {
        ColorPrimary
    }

    val statusColor = if (status == "Active") {
        SgrmsColors.Success
    } else {
        SgrmsColors.Error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onViewClick()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
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
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(profileColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Student Icon",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = student.studentName.ifBlank { "Unknown Student" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Code: ${student.studentCode.ifBlank { "-" }}",
                        fontSize = 13.sp,
                        color = ColorOnSurfaceVariant
                    )

                    Text(
                        text = "Class: ${student.className.ifBlank { "Not assigned" }}",
                        fontSize = 13.sp,
                        color = ColorOnSurfaceVariant
                    )

                    Text(
                        text = "Gender: $gender",
                        fontSize = 13.sp,
                        color = ColorOnSurfaceVariant
                    )

                    Text(
                        text = "Email: ${student.email.ifBlank { "Not specified" }}",
                        fontSize = 13.sp,
                        color = ColorOnSurfaceVariant
                    )

                    Text(
                        text = "Phone: ${student.phoneNumber.ifBlank { "Not specified" }}",
                        fontSize = 13.sp,
                        color = ColorOnSurfaceVariant
                    )
                }

                Text(
                    text = status,
                    fontSize = 12.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            statusColor.copy(alpha = 0.1f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
fun EmptyStudentCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "No Students",
                tint = ColorOnSurfaceVariant,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "No students found",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Add a new student to display records here.",
                fontSize = 13.sp,
                color = ColorOnSurfaceVariant
            )
        }
    }
}

@Composable
fun StudentFilterDialog(
    selectedClass: String,
    selectedGender: String,
    selectedStatus: String,
    selectedSort: String,
    classList: List<String>,
    genderList: List<String>,
    statusList: List<String>,
    sortList: List<String>,
    onClassSelected: (String) -> Unit,
    onGenderSelected: (String) -> Unit,
    onStatusSelected: (String) -> Unit,
    onSortSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onClearFilter: () -> Unit
) {
    var classExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Filter Students",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                StudentDialogDropdown(
                    label = "Class",
                    value = selectedClass,
                    expanded = classExpanded,
                    onExpandedChange = {
                        classExpanded = it
                    },
                    items = classList,
                    onItemSelected = { value ->
                        onClassSelected(value)
                        classExpanded = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                StudentDialogDropdown(
                    label = "Gender",
                    value = selectedGender,
                    expanded = genderExpanded,
                    onExpandedChange = {
                        genderExpanded = it
                    },
                    items = genderList,
                    onItemSelected = { value ->
                        onGenderSelected(value)
                        genderExpanded = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                StudentDialogDropdown(
                    label = "Status",
                    value = selectedStatus,
                    expanded = statusExpanded,
                    onExpandedChange = {
                        statusExpanded = it
                    },
                    items = statusList,
                    onItemSelected = { value ->
                        onStatusSelected(value)
                        statusExpanded = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                StudentDialogDropdown(
                    label = "Sort",
                    value = selectedSort,
                    expanded = sortExpanded,
                    onExpandedChange = {
                        sortExpanded = it
                    },
                    items = sortList,
                    onItemSelected = { value ->
                        onSortSelected(value)
                        sortExpanded = false
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary
                )
            ) {
                Text(
                    text = "Apply",
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onClearFilter
            ) {
                Text(
                    text = "Clear",
                    color = ColorPrimary
                )
            }
        }
    )
}

@Composable
fun StudentDialogDropdown(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    Box {
        OutlinedButton(
            onClick = {
                onExpandedChange(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ColorPrimary
            )
        ) {
            Text(
                text = "$label: $value",
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = label,
                tint = ColorPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
            }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(item)
                    },
                    onClick = {
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

fun exportStudentListToCsv(
    context: Context,
    students: List<StudentModel>
) {
    val fileName = "student_list.csv"
    val file = File(context.cacheDir, fileName)

    val csvContent = buildString {
        appendLine("Student Name,Student Code,Email,Phone Number,Class,Gender,Status")

        students.forEach { student ->
            val row = listOf(
                student.studentName.ifBlank { "Unknown Student" },
                student.studentCode.ifBlank { "-" },
                student.email.ifBlank { "Not specified" },
                student.phoneNumber.ifBlank { "Not specified" },
                student.className.ifBlank { "Not assigned" },
                student.gender.ifBlank { "Not specified" },
                student.status.ifBlank { "Active" }
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
        putExtra(Intent.EXTRA_SUBJECT, "Student List")
        putExtra(Intent.EXTRA_TITLE, "student_list.csv")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            "Export Student List"
        )
    )
}
