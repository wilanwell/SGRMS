package com.example.sgrms.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.SuccessGreen
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.ui.theme.WarningOrange
import com.example.sgrms.utils.ReportPdfData
import com.example.sgrms.utils.ReportPdfGradeItem
import java.util.Date
import com.example.sgrms.ui.theme.markColor

data class ReportDisplayItem(
    val report: ReportPdfData,
    val student: StudentModel,
    val gradeItems: List<ReportPdfGradeItem>,
    val studentName: String,
    val studentCode: String,
    val className: String,
    val status: String
)

@Composable
fun ReportSummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )

            Column {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ReportCard(
    item: ReportDisplayItem,
    onViewClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val statusIcon = if (item.status == "Generated") {
        Icons.Default.CheckCircle
    } else {
        Icons.Default.Warning
    }

    val statusColor = if (item.status == "Generated") {
        SuccessGreen
    } else {
        WarningOrange
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onViewClick()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        imageVector = Icons.Default.Description,
                        contentDescription = "Report",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(29.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.studentName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${item.studentCode} • ${item.className}",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = item.report.overallGrade,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = markColor(item.report.averageMarks)
                    )

                    Text(
                        text = "%.2f%%".format(item.report.averageMarks),
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = BackgroundGrey)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = TextGrey,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = item.report.generatedDate,
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = item.status,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = item.status,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            if (item.report.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Remarks: ${item.report.remarks}",
                    fontSize = 14.sp,
                    color = TextGrey
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedIconButton(
                    onClick = onViewClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "View Report",
                        tint = PrimaryBlue
                    )
                }

                OutlinedIconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download PDF",
                        tint = SuccessGreen
                    )
                }

                OutlinedIconButton(
                    onClick = onUpdateClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Update Report",
                        tint = PrimaryBlue
                    )
                }

                OutlinedIconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Report",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun ReportFilterDialog(
    selectedClass: String,
    selectedStatus: String,
    selectedDate: String,
    classList: List<String>,
    statusList: List<String>,
    dateList: List<String>,
    onClassSelected: (String) -> Unit,
    onStatusSelected: (String) -> Unit,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onClearFilter: () -> Unit
) {
    var classExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var dateExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Filter Reports",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                ReportDialogDropdown(
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

                ReportDialogDropdown(
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

                ReportDialogDropdown(
                    label = "Sort",
                    value = selectedDate,
                    expanded = dateExpanded,
                    onExpandedChange = {
                        dateExpanded = it
                    },
                    items = dateList,
                    onItemSelected = { value ->
                        onDateSelected(value)
                        dateExpanded = false
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
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
                    color = PrimaryBlue
                )
            }
        }
    )
}

@Composable
fun ReportDialogDropdown(
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
                contentColor = PrimaryBlue
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
                tint = PrimaryBlue,
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
