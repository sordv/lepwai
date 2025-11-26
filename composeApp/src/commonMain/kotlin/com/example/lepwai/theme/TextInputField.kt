package com.example.lepwai.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextInputField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    maxLength: Int = 30,
    isPassword: Boolean = false
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = value,
            onValueChange = { if (it.length <= maxLength) onValueChange(it) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.BackGroundMediumGray),
            textStyle = TextStyle(
                fontSize = 32.sp,
                color = AppColors.TextWhite,
                textAlign = TextAlign.Center
            ),
            placeholder = {
                Text(
                    placeholder,
                    fontSize = 32.sp,
                    color = AppColors.TextLightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            visualTransformation =
                if (isPassword && !isPasswordVisible) PasswordVisualTransformation()
                else VisualTransformation.None,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = AppColors.BackGroundMediumGray,
                focusedContainerColor = AppColors.BackGroundMediumGray,
                unfocusedIndicatorColor = AppColors.BackGroundMediumGray,
                focusedIndicatorColor = AppColors.BackGroundMediumGray,
                cursorColor = AppColors.TextWhite
            )
        )

        if (isPassword && value.isNotEmpty()) {
            IconButton(
                onClick = { isPasswordVisible = !isPasswordVisible },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = if (isPasswordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = AppColors.TextLightGray,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
