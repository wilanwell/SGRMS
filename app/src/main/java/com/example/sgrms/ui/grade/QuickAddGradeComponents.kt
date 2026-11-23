package com.example.sgrms.ui.grade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.data.model.StudentModel
import com.example.sgrms.ui.theme.ColorBackground
import com.example.sgrms.ui.theme.ColorSurface
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.SgrmsColors
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import com.example.sgrms.utils.GradeCalculator
import com.example.sgrms.ui.theme.SgrmsRadius
import com.example.sgrms.ui.components.sgrmsFieldColors

@Composable
fun QuickGradeStudentCard(
    student: StudentModel,
    marks: String,
    onMarksChange: (String) -> Unit
) {
    val marksValue = marks.toDoubleOrNull()

    val gradeLetter = if (marksValue != null) {
        GradeCalculator.gradeLetter(marksValue)
    } else {
        "-"
    }

    val gradeColor = when (gradeLetter) {
        "A+", "A" -> SgrmsColors.Success
        "B" -> ColorPrimary
        "C" -> SgrmsColors.WarningBar
        "D" -> SgrmsColors.Warning
        "E" -> SgrmsColors.Error
        else -> ColorOnSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SgrmsRadius.Card),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
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

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = student.studentName.ifBlank { "Unknown Student" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Code: ${student.studentCode.ifBlank { "-" }}",
                        fontSize = 13.sp,
                        color = ColorOnSurfaceVariant
                    )
                }

                Text(
                    text = gradeLetter,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = gradeColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = ColorBackground)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = marks,
                onValueChange = onMarksChange,
                label = {
                    Text("Marks")
                },
                placeholder = {
                    Text("0 - 100")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = "Marks"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                shape = RoundedCornerShape(SgrmsRadius.Field),
                colors = sgrmsFieldColors()
            )
        }
    }
}
