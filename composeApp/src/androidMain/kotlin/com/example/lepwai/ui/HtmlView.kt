package com.example.lepwai.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun HtmlView(
    htmlFileName: String,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = false
                settings.allowFileAccess = true
                settings.domStorageEnabled = true

                loadUrl("file:///android_asset/levels/$htmlFileName.html")
            }
        }
    )
}