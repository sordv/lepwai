package com.example.lepwai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lepwai.network.Level
import com.example.lepwai.network.ChooseLevelApi
import com.example.lepwai.network.createHttpClient
import com.example.lepwai.theme.AppColors

@Composable
fun ChooseLevel(
    topicId: Int,
    topicName: String,
    onLevelClick: (Level) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val client = remember { createHttpClient() }
    val chooseLevelApi = remember { ChooseLevelApi(client, "http://10.0.2.2:8080") }

    var levels by remember { mutableStateOf<List<Level>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(topicId) {
        try {
            levels = chooseLevelApi.getLevelsForTopic(topicId).sortedBy { it.sort }
        } catch (e: Throwable) {
            error = e.message ?: "Ошибка подключения к серверу"
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
                .background(AppColors.DifficultyEasy) //TODO: UBRAT POTOM
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .background(AppColors.DifficultyMedium) // TODO убрать
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
                text = topicName,
                color = AppColors.TextWhite,
                fontSize = 36.sp
            )

            Box(modifier = Modifier.size(65.dp)) // заглушка
        }
        // END TOP BAR

        when {
            error != null -> Text(
                text = "Ошибка: $error",
                color = AppColors.ErrorRed,
                modifier = Modifier.padding(16.dp)
            )

            levels.isNotEmpty() ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(levels) { level ->
                        val (label, color) = when (level.difficulty) {
                            null -> "Theory" to AppColors.TextLightGray
                            1 -> "Easy" to AppColors.DifficultyEasy
                            2 -> "Medium" to AppColors.DifficultyMedium
                            3 -> "Hard" to AppColors.DifficultyHard
                            else -> "Unknown" to AppColors.TextLightGray
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clickable { onLevelClick(level) }
                                .padding(start = 12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = label,
                                color = color,
                                fontSize = 25.sp
                            )

                            Text(
                                text = level.name,
                                color = AppColors.TextWhite,
                                fontSize = 32.sp
                            )
                        }
                    }
                }
        }
    }
}
