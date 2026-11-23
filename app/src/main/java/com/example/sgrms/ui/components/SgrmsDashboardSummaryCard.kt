package com.example.sgrms.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.theme.ColorSurface
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant
import com.example.sgrms.ui.theme.SgrmsRadius

enum class SgrmsSummaryCardStyle {
    Primary,
    White
}

@Composable
fun SgrmsDashboardSummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    style: SgrmsSummaryCardStyle = SgrmsSummaryCardStyle.Primary,
    height: Dp = 120.dp,
    onClick: (() -> Unit)? = null
) {
    val containerColor = when (style) {
        SgrmsSummaryCardStyle.Primary -> ColorPrimary
        SgrmsSummaryCardStyle.White -> ColorSurface
    }

    val iconColor = when (style) {
        SgrmsSummaryCardStyle.Primary -> Color.White
        SgrmsSummaryCardStyle.White -> ColorPrimary
    }

    val titleColor = when (style) {
        SgrmsSummaryCardStyle.Primary -> Color.White
        SgrmsSummaryCardStyle.White -> ColorOnSurfaceVariant
    }

    val valueColor = when (style) {
        SgrmsSummaryCardStyle.Primary -> Color.White
        SgrmsSummaryCardStyle.White -> ColorPrimary
    }

    Card(
        modifier = modifier
            .height(height)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(if (style == SgrmsSummaryCardStyle.Primary) SgrmsRadius.Hero else SgrmsRadius.Card),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = title,
                    color = titleColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = value,
                color = valueColor,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}