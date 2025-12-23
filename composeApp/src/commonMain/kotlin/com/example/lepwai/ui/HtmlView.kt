package com.example.lepwai.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun HtmlView(
    htmlFileName: String,
    modifier: Modifier = Modifier
)