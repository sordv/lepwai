package com.example.lepwai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lepwai.config.ServerConfig
import com.example.lepwai.network.ChooseLevelApi
import com.example.lepwai.network.Level
import com.example.lepwai.network.createHttpClient
import com.example.lepwai.theme.AppColors

@Composable
fun ChooseLevelScreen(
    userLogin: String,
    topicId: Int,
    topicName: String,
    onBack: () -> Unit = {},
    onSelectLevel: (Int, String) -> Unit = { _, _ -> }
) {

    val client = remember { createHttpClient() }
    val api = remember { ChooseLevelApi(client, ServerConfig.BASE_URL) }

    var levels by remember { mutableStateOf<List<Level>>(emptyList()) }
    var completedLevels by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(topicId) {
        try {
            levels = api.getLevelsForTopic(topicId).sortedBy { it.sort }

            completedLevels = api
                .getUserProgress(userLogin)
                .filter { it.status == "done" }
                .map { it.levelId }
                .toSet()

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
                text = topicName,
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

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(levels) { level ->

                    val isCompleted = completedLevels.contains(level.id)

                    val (label, color) = when (level.difficulty) {
                        null -> "Theory" to AppColors.TextLightGray
                        1 -> "Easy" to AppColors.DifficultyEasy
                        2 -> "Medium" to AppColors.DifficultyMedium
                        3 -> "Hard" to AppColors.DifficultyHard
                        else -> "Unknown" to AppColors.TextLightGray
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable { onSelectLevel(level.id, level.name) }
                    ) {

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            // СТРОКА: сложность / теория + "Выполнено"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = label,
                                    color = color,
                                    fontSize = 22.sp
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                if (isCompleted) {
                                    Text(
                                        text = "Выполнено",
                                        color = AppColors.DoneGreen,
                                        fontSize = 22.sp
                                    )
                                }
                            }

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = level.name,
                                color = AppColors.TextWhite,
                                fontSize = 30.sp
                            )
                        }

                    }
                }
            }
        }
    }
}
