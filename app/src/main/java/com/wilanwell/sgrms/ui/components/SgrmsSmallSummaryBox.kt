package com.wilanwell.sgrms.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import com.wilanwell.sgrms.ui.theme.SgrmsRadius
import com.wilanwell.sgrms.ui.theme.ColorPrimaryContainer

@Composable
fun SgrmsSmallSummaryBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    height: Dp = 86.dp
) {
    Card(
        modifier = modifier.height(height),
        shape = RoundedCornerShape(SgrmsRadius.Card),
        colors = CardDefaults.cardColors(
            containerColor = ColorPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ColorPrimary
            )

            Text(
                text = title,
                fontSize = 13.sp,
                color = ColorOnSurfaceVariant
            )
        }
    }
}