package com.wilanwell.sgrms.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.SgrmsRadius
import com.wilanwell.sgrms.ui.theme.ColorOutline
import com.wilanwell.sgrms.ui.theme.ColorSurface
import com.wilanwell.sgrms.ui.theme.ColorBackground

@Composable
fun SgrmsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isError: Boolean = false,
    errorMessage: String? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val fieldIcon = leadingIcon ?: icon

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label)
        },
        placeholder = {
            if (placeholder.isNotBlank()) {
                Text(text = placeholder)
            }
        },
        leadingIcon = if (fieldIcon != null) {
            {
                Icon(
                    imageVector = fieldIcon,
                    contentDescription = label
                )
            }
        } else {
            null
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SgrmsRadius.Field),
        singleLine = singleLine,
        minLines = minLines,
        isError = isError,
        readOnly = readOnly,
        enabled = enabled,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        supportingText = {
            if (isError && !errorMessage.isNullOrBlank()) {
                Text(text = errorMessage)
            }
        },
        colors = sgrmsFieldColors()
    )
}

/** White fields with a soft outline and an indigo focus ring. */
@Composable
fun sgrmsFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ColorPrimary,
    unfocusedBorderColor = ColorOutline,
    focusedLabelColor = ColorPrimary,
    cursorColor = ColorPrimary,
    focusedContainerColor = ColorSurface,
    unfocusedContainerColor = ColorSurface,
    disabledContainerColor = ColorBackground,
    errorContainerColor = ColorSurface
)
