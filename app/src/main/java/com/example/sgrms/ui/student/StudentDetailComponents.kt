package com.example.sgrms.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import com.example.sgrms.ui.theme.markColor

data class StudentDetailSubjectItem(
    val subjectName: String,
    val subjectCode: String,
    val marks: Double,
    val grade: String
)

@Composable
fun StudentDetailInfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = ColorPrimary,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.size(14.dp))

        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                color = ColorOnSurfaceVariant
            )

            Text(
                text = value.ifBlank { "-" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StudentDetailSubjectRow(
    subject: StudentDetailSubjectItem
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = subject.subjectName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${subject.subjectCode} • Grade ${subject.grade.ifBlank { "-" }}",
                    fontSize = 13.sp,
                    color = markColor(subject.marks),
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "%.2f%%".format(subject.marks),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = markColor(subject.marks)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = {
                (subject.marks / 100.0).toFloat().coerceIn(0f, 1f)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = markColor(subject.marks),
            trackColor = ColorBackground
        )
    }
}
