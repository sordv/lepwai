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
import com.example.lepwai.network.Course
import com.example.lepwai.network.createHttpClient
import com.example.lepwai.theme.AppColors

@Composable
fun LearningScreen() {

    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    selectedCourse?.let { course ->
        LearningDeepScreen(
            courseId = course.id,
            courseName = course.name,
            onBack = { selectedCourse = null }
        )
        return
    }

    val client = remember { createHttpClient() }
    val coursesApi = remember { CoursesApi(client, "http://10.0.2.2:8080") }

    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            courses = coursesApi.getCourses()
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
            Box(modifier = Modifier.size(65.dp)) // заглушка

            Text(
                text = "Курсы",
                color = AppColors.TextWhite,
                fontSize = 36.sp
            )

            Box(modifier = Modifier.size(65.dp)) // заглушка
        }
        // END TOP BAR

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                error != null -> Text(
                    text = "Ошибка: $error",
                    color = AppColors.ErrorRed,
                    modifier = Modifier.padding(top = 12.dp)
                )

                courses.isNotEmpty() ->
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
                                    .clickable { selectedCourse = course }
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
        }
    }
}
