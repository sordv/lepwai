package com.example.lepwai.screens

import androidx.compose.runtime.Composable
import com.example.lepwai.data.LearningNavigationState

@Composable
fun LearningRootScreen(
    userLogin: String,
    learningState: LearningNavigationState,
    onResetToCourses: () -> Unit = {},
    onOpenChatWithPrefill: (String) -> Unit
) {
    when {
        learningState.selectedCourseId == null -> {
            ChooseCourseScreen(
                userLogin = userLogin,
                onSelectCourse = { id, name ->
                    learningState.selectedCourseId = id
                    learningState.selectedCourseName = name
                }
            )
        }

        learningState.selectedTopicId == null -> {
            ChooseTopicScreen(
                userLogin = userLogin,
                courseId = learningState.selectedCourseId!!,
                courseName = learningState.selectedCourseName ?: "",
                onBack = {
                    learningState.selectedCourseId = null
                    learningState.selectedCourseName = null
                    //learningState.selectedTopicId = null
                    //learningState.selectedLevelId = null
                },
                onSelectTopic = { id, name ->
                    learningState.selectedTopicId = id
                    learningState.selectedTopicName = name
                }
            )
        }

        learningState.selectedLevelId == null -> {
            ChooseLevelScreen(
                userLogin = userLogin,
                topicId = learningState.selectedTopicId!!,
                topicName = learningState.selectedTopicName ?: "",
                onBack = {
                    learningState.selectedTopicId = null
                    learningState.selectedTopicName = null
                    //learningState.selectedLevelId = null
                },
                onSelectLevel = { id, name ->
                    learningState.selectedLevelId = id
                    learningState.selectedLevelName = name
                }
            )
        }

        else -> {
            ViewLevelScreen(
                userLogin = userLogin,
                levelId = learningState.selectedLevelId!!,
                levelName = learningState.selectedLevelName ?: "",
                courseName = learningState.selectedCourseName ?: "",
                onBack = {
                    learningState.selectedLevelId = null
                    learningState.selectedLevelName = null
                },
                onOpenChat = { prompt ->
                    onOpenChatWithPrefill(prompt)
                }
            )
        }
    }
}
