package com.example.lepwai.screens

import androidx.compose.runtime.Composable
import com.example.lepwai.data.LearningNavigationState

@Composable
fun LearningRootScreen(
    learningState: LearningNavigationState,
    onResetToCourses: () -> Unit = {}
) {
    when {
        learningState.selectedCourseId == null -> {
            ChooseCourseScreen(
                onSelectCourse = { id, name ->
                    learningState.selectedCourseId = id
                    learningState.selectedCourseName = name
                }
            )
        }

        learningState.selectedTopicId == null -> {
            ChooseTopicScreen(
                courseId = learningState.selectedCourseId!!,
                courseName = learningState.selectedCourseName ?: "",
                onBack = {
                    learningState.selectedCourseId = null
                    learningState.selectedCourseName = null
                    learningState.selectedTopicId = null
                    learningState.selectedLevelId = null
                },
                onSelectTopic = { id, name ->
                    learningState.selectedTopicId = id
                    learningState.selectedTopicName = name
                }
            )
        }

        learningState.selectedLevelId == null -> {
            ChooseLevelScreen(
                topicId = learningState.selectedTopicId!!,
                topicName = learningState.selectedTopicName ?: "",
                onBack = {
                    learningState.selectedTopicId = null
                    learningState.selectedTopicName = null
                    learningState.selectedLevelId = null
                },
                onSelectLevel = { id, name ->
                    learningState.selectedLevelId = id
                    learningState.selectedLevelName = name
                }
            )
        }

        else -> {
            ViewLevelScreen(
                levelId = learningState.selectedLevelId!!,
                levelName = learningState.selectedLevelName ?: "",
                onBack = {
                    learningState.selectedLevelId = null
                    learningState.selectedLevelName = null
                }
            )
        }
    }
}
