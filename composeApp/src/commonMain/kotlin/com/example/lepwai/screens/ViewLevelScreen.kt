package com.example.lepwai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lepwai.network.ChooseLevelApi
import com.example.lepwai.network.Level
import com.example.lepwai.network.createHttpClient
import com.example.lepwai.theme.AppColors
import com.example.lepwai.ui.HtmlView
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch

@Composable
fun ViewLevelScreen(
    userLogin: String,
    levelId: Int,
    levelName: String,
    onBack: () -> Unit = {}
) {

    val client = remember { createHttpClient() }
    val api = remember { ChooseLevelApi(client, "http://10.0.2.2:8080") }
    val scope = rememberCoroutineScope()

    var level by remember { mutableStateOf<Level?>(null) }
    var completed by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(levelId) {
        try {
            level = api.getLevelById(levelId)

            completed = api
                .getUserProgress(userLogin)
                .any { it.levelId == levelId && it.status == "done" }

        } catch (e: Throwable) {
            error = e.message
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundBlack)
    ) {

        // TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = AppColors.ButtonGray,
                    modifier = Modifier.size(45.dp)
                )
            }

            Text(
                text = levelName,
                color = AppColors.TextWhite,
                fontSize = 36.sp
            )

            Box(modifier = Modifier.size(65.dp))
        }

        when {
            error != null -> Text(
                text = "Ошибка: $error",
                color = AppColors.ErrorRed,
                modifier = Modifier.padding(16.dp)
            )

            level != null -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                HtmlView(
                    htmlFileName = levelId.toString(),
                    modifier = Modifier.fillMaxWidth()
                )

                // ФЛАЖОК — ТОЛЬКО ДЛЯ ТЕОРИИ
                if (level!!.difficulty == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = completed,
                            enabled = !completed,
                            onCheckedChange = {
                                scope.launch {
                                    api.completeLevel(
                                        login = userLogin,
                                        levelId = levelId
                                    )
                                    completed = true
                                }
                            }
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = if (completed) "Пройдено" else "Отметить как пройдено",
                            color = AppColors.TextWhite,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
