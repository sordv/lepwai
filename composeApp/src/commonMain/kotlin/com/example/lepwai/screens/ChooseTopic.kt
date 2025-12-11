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
import com.example.lepwai.network.ChooseTopicApi
import com.example.lepwai.network.Topic
import com.example.lepwai.network.createHttpClient
import com.example.lepwai.theme.AppColors

@Composable
fun ChooseTopic(
    courseId: Int,
    courseName: String,
    onBack: () -> Unit = {}
) {

    var selectedTopic by remember { mutableStateOf<Topic?>(null) }

    selectedTopic?.let { topic ->
        ChooseLevel(
            topicId = topic.id,
            topicName = topic.name,
            onBack = { selectedTopic = null }
        )
        return
    }

    val client = remember { createHttpClient() }
    val chooseTopicApi = remember { ChooseTopicApi(client, "http://10.0.2.2:8080") }

    var topics by remember { mutableStateOf<List<Topic>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(courseId) {
        try {
            topics = chooseTopicApi.getTopicsForCourse(courseId).sortedBy { it.sort }
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
                .background(AppColors.DifficultyEasy) // TODO убрать
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
                text = courseName,
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

            topics.isNotEmpty() ->
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
                                .clickable { selectedTopic = topic }
                                .padding(start = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = topic.name,
                                color = AppColors.TextWhite,
                                fontSize = 32.sp
                            )
                        }
                    }
                }
        }
    }
}
