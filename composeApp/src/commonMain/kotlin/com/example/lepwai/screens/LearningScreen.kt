package com.example.lepwai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lepwai.network.CoursesApi
import com.example.lepwai.network.createHttpClient
import com.example.lepwai.network.Course
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.lepwai.theme.AppColors

@Composable
fun LearningScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Курсы",
            color = AppColors.TextWhite,
            fontSize = 36.sp
        )

        var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
        var loading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var showLoading by remember { mutableStateOf(false) }
        var showError by remember { mutableStateOf(false) }

        val client = remember { createHttpClient() }
        val baseUrl = "http://10.0.2.2:8080"
        val coursesApi = remember { CoursesApi(client, baseUrl) }

        LaunchedEffect(Unit) {
            val loadingJob = launch {
                kotlinx.coroutines.delay(2000)
                if (loading) showLoading = true
            }

            try {
                courses = coursesApi.getCourses()
            } catch (t: Throwable) {
                error = t.message ?: "Ошибка при загрузке курсов"
                launch {
                    kotlinx.coroutines.delay(2000)
                    if (error != null && courses.isEmpty()) showError = true
                }
            } finally {
                loading = false
                showLoading = false
            }
        }

        when {
            courses.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(courses) { course ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable {
                                    // TODO: навигация
                                }
                                .padding(start = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = course.name,
                                color = AppColors.TextWhite,
                                fontSize = 32.sp
                            )
                        }
                    }
                }
            }

            showLoading -> Text("Загрузка...", color = AppColors.TextWhite, modifier = Modifier.padding(top = 8.dp))
            showError -> Text("Ошибка: $error", color = AppColors.ErrorRed, modifier = Modifier.padding(top = 8.dp))
        }
    }
}