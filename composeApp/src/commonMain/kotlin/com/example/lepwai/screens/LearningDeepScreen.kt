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
import com.example.lepwai.network.TopicsApi
import com.example.lepwai.network.Topic
import com.example.lepwai.network.createHttpClient
import com.example.lepwai.theme.AppColors

@Composable
fun LearningDeepScreen(
    courseId: Int,
    courseName: String,
    onTopicClick: (Topic) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val client = remember { createHttpClient() }
    val baseUrl = "http://10.0.2.2:8080"
    val topicsApi = remember { TopicsApi(client, baseUrl) }

    var topics by remember { mutableStateOf<List<Topic>>(emptyList()) }
    var showLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(courseId) {
        showLoading = true
        showError = false
        error = ""
        try {
            topics = topicsApi.getTopicsForCourse(courseId).sortedBy { it.sort }
        } catch (e: Throwable) {
            showError = true
            error = e.message ?: "Неизвестная ошибка"
        } finally {
            showLoading = false
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
                .padding(25.dp),
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

            Box(modifier = Modifier.size(65.dp)) // заглушка
        }
        // END TOP BAR

        when {
            topics.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(topics) { topic ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable { onTopicClick(topic) }
                                .padding(start = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {

                            // Стиль текста 100% как в LearningScreen
                            Text(
                                text = topic.name,
                                color = AppColors.TextWhite,
                                fontSize = 32.sp
                            )
                        }
                    }
                }
            }

            showLoading -> {
                Text(
                    "Загрузка...",
                    color = AppColors.TextWhite,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            showError -> {
                Text(
                    "Ошибка: $error",
                    color = AppColors.ErrorRed,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}