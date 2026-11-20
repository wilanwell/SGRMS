package com.example.sgrms.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.ui.theme.PrimaryBlue

@Composable
fun SgrmsFilterButton(
    selectedValue: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    showFilterIcon: Boolean = true
) {
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = {
                onExpandedChange(true)
            },
            modifier = Modifier.height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryBlue
            )
        ) {
            if (showFilterIcon) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.size(6.dp))
            }

            Text(
                text = selectedValue,
                fontSize = 13.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.size(4.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Open Filter",
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
                        Text(text = item)
                    },
                    onClick = {
                        onItemSelected(item)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}