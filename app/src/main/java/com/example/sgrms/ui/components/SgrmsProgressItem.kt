package com.example.sgrms.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.theme.BackgroundGrey
import com.example.sgrms.ui.theme.PrimaryBlue
import com.example.sgrms.ui.theme.SuccessGreen
import com.example.sgrms.ui.theme.TextGrey

@Composable
fun SgrmsProgressItem(
    title: String,
    progress: Float,
    percentageText: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    subtitleAsGrade: Boolean = false
) {
    Column(
        modifier = modifier.padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = if (subtitleAsGrade) SuccessGreen else TextGrey,
                        fontWeight = if (subtitleAsGrade) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Text(
                text = percentageText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = {
                progress.coerceIn(0f, 1f)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = PrimaryBlue,
            trackColor = BackgroundGrey
        )
    }
}