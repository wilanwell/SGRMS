package com.wilanwell.sgrms.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import com.wilanwell.sgrms.ui.theme.SgrmsRadius

@Composable
fun SgrmsSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Search",
    placeholder: String = "Search here",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label)
        },
        placeholder = {
            Text(text = placeholder)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = ColorOnSurfaceVariant
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SgrmsRadius.Field),
        singleLine = true,
        colors = sgrmsFieldColors()
    )
}