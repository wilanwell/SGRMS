package com.example.sgrms.ui.report

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.model.GradeModel
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.CardWhite
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.SuccessGreen
import com.example.sgrms.ui.theme.TextGrey
import com.example.sgrms.ui.theme.WarningOrange
import com.example.sgrms.ui.theme.markColor

data class ReportGradeDisplayItem(
    val grade: GradeModel,
    val subjectName: String,
    val subjectCode: String
)

@Composable
fun ReportStudentInfoCard(
    student: StudentModel,
    generatedDate: String,
    reportStatusText: String,
    reportStatusColor: Color,
    reportStatusIcon: ImageVector
) {
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
                Card(
                    modifier = Modifier.size(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryBlue.copy(alpha = 0.12f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Student",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = student.studentName.ifBlank { "Unknown Student" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = student.studentCode.ifBlank { "-" },
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }

                Icon(
                    imageVector = reportStatusIcon,
                    contentDescription = reportStatusText,
                    tint = reportStatusColor,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            HorizontalDivider(color = BackgroundGrey)

            Spacer(modifier = Modifier.height(18.dp))

            ReportInfoRow(
                icon = Icons.Default.School,
                label = "Class",
                value = student.className.ifBlank { "Not assigned" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReportInfoRow(
                icon = Icons.Default.CalendarMonth,
                label = "Generated Date",
                value = generatedDate
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReportInfoRow(
                icon = reportStatusIcon,
                label = "Report Status",
                value = reportStatusText,
                valueColor = reportStatusColor
            )
        }
    }
}

@Composable
fun ReportPerformanceSummaryCard(
    totalMarks: Double,
    averageMarks: Double,
    overallGrade: String,
    completedSubjectCount: Int
) {
    val markColor = markColor(averageMarks)
    val progress = (averageMarks / 100.0).toFloat().coerceIn(0f, 1f)

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
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Performance Summary",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.size(10.dp))

                Text(
                    text = "Performance Summary",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "%.1f%%".format(averageMarks),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = markColor
                    )

                    Text(
                        text = "Average Marks",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = overallGrade,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = markColor
                    )

                    Text(
                        text = "Overall Grade",
                        fontSize = 13.sp,
                        color = TextGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = {
                    progress
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp),
                color = PrimaryBlue,
                trackColor = BackgroundGrey
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReportSmallInfo(
                    label = "Subjects",
                    value = completedSubjectCount.toString()
                )

                ReportSmallInfo(
                    label = "Total Marks",
                    value = "%.2f".format(totalMarks)
                )

                ReportSmallInfo(
                    label = "Grade",
                    value = overallGrade
                )
            }
        }
    }
}

@Composable
fun ReportStatusCard(
    statusText: String,
    statusColor: Color,
    statusIcon: ImageVector,
    remarks: String
) {
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
                    imageVector = statusIcon,
                    contentDescription = statusText,
                    tint = statusColor,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.size(10.dp))

                Text(
                    text = statusText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = remarks.ifBlank { "No remarks available." },
                fontSize = 14.sp,
                color = TextGrey,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ReportInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = PrimaryBlue,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.size(10.dp))

        Column {
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextGrey
            )

            Text(
                text = value.ifBlank { "-" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun ReportGradeCard(
    item: ReportGradeDisplayItem
) {
    val markColor = markColor(item.grade.marks)
    val progress = (item.grade.marks / 100.0).toFloat().coerceIn(0f, 1f)
    val statusText = item.grade.status.ifBlank { "Pending" }

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
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimaryBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Subject",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.subjectName.ifBlank { "Unknown Subject" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.subjectCode.ifBlank { "-" },
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = item.grade.gradeLetter.ifBlank { "-" },
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = markColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "%.1f%%".format(item.grade.marks),
                        fontSize = 15.sp,
                        color = TextGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = {
                    progress
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = PrimaryBlue,
                trackColor = BackgroundGrey
            )

            if (item.grade.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Remarks: ${item.grade.remarks}",
                    fontSize = 15.sp,
                    color = TextGrey
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Status: $statusText",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (statusText.equals("Complete", ignoreCase = true)) {
                    SuccessGreen
                } else {
                    WarningOrange
                }
            )
        }
    }
}

@Composable
fun ReportSmallInfo(
    label: String,
    value: String
) {
    Column {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )

        Text(
            text = label,
            fontSize = 11.sp,
            color = TextGrey
        )
    }
}
