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
import com.example.lepwai.network.*
import com.example.lepwai.theme.AppColors

@Composable
fun ChooseCourseScreen(
    userLogin: String,
    onSelectCourse: (Int, String) -> Unit = { _, _ -> }
) {

    val client = remember { createHttpClient() }
    val courseApi = remember { ChooseCourseApi(client, "http://10.0.2.2:8080") }
    val topicApi = remember { ChooseTopicApi(client, "http://10.0.2.2:8080") }
    val levelApi = remember { ChooseLevelApi(client, "http://10.0.2.2:8080") }

    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var completedLevels by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var courseLevels by remember { mutableStateOf<Map<Int, List<Int>>>(emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            courses = courseApi.getCourses()

            completedLevels = levelApi
                .getUserProgress(userLogin)
                .filter { it.status == "done" }
                .map { it.levelId }
                .toSet()

            courseLevels = courses.associate { course ->
                val topics = topicApi.getTopicsForCourse(course.id)

                val levels = topics.flatMap { topic ->
                    levelApi.getLevelsForTopic(topic.id).map { it.id }
                }

                course.id to levels
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
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Курсы",
                color = AppColors.TextWhite,
                fontSize = 36.sp
            )
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
                items(courses) { course ->

                    val levels = courseLevels[course.id] ?: emptyList()
                    val done = levels.count { completedLevels.contains(it) }
                    val total = levels.size
                    val progress = if (total == 0) 0f else done.toFloat() / total

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCourse(course.id, course.name) }
                    ) {

                        Text(
                            text = course.name,
                            color = AppColors.TextWhite,
                            fontSize = 32.sp
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
