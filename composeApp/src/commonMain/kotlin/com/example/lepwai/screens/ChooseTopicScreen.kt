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
import com.example.lepwai.network.*
import com.example.lepwai.theme.AppColors

@Composable
fun ChooseTopicScreen(
    userLogin: String,
    courseId: Int,
    courseName: String,
    onBack: () -> Unit = {},
    onSelectTopic: (Int, String) -> Unit = { _, _ -> }
) {

    val client = remember { createHttpClient() }
    val topicApi = remember { ChooseTopicApi(client, "http://10.0.2.2:8080") }
    val levelApi = remember { ChooseLevelApi(client, "http://10.0.2.2:8080") }

    var topics by remember { mutableStateOf<List<Topic>>(emptyList()) }
    var completedLevels by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var topicLevels by remember { mutableStateOf<Map<Int, List<Int>>>(emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(courseId) {
        try {
            topics = topicApi.getTopicsForCourse(courseId)

            completedLevels = levelApi
                .getUserProgress(userLogin)
                .filter { it.status == "done" }
                .map { it.levelId }
                .toSet()

            topicLevels = topics.associate { topic ->
                topic.id to levelApi
                    .getLevelsForTopic(topic.id)
                    .map { it.id }
            }

        } catch (e: Throwable) {
            error = e.message ?: "Ошибка загрузки"
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
                text = courseName,
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
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                items(topics) { topic ->

                    val levels = topicLevels[topic.id] ?: emptyList()
                    val done = levels.count { completedLevels.contains(it) }
                    val total = levels.size
                    val progress = if (total == 0) 0f else done.toFloat() / total

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTopic(topic.id, topic.name) }
                    ) {
                        Text(
                            text = topic.name,
                            color = AppColors.TextWhite,
                            fontSize = 30.sp
                        )

                        Spacer(Modifier.height(6.dp))

                        // ПРОГРЕСС-БАР
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(AppColors.BackGroundMediumGray)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(6.dp)
                                    .background(AppColors.DoneGreen)
                            )
                        }
                    }
                }
            }
        }
    }
}