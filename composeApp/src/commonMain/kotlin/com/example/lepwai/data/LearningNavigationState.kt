package com.example.lepwai.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class LearningNavigationState {

    var selectedCourseId by mutableStateOf<Int?>(null)
    var selectedCourseName by mutableStateOf<String?>(null)

    var selectedTopicId by mutableStateOf<Int?>(null)
    var selectedTopicName by mutableStateOf<String?>(null)

    var selectedLevelId by mutableStateOf<Int?>(null)
    var selectedLevelName by mutableStateOf<String?>(null)

    fun resetToCourses() {
        selectedCourseId = null
        selectedCourseName = null
        selectedTopicId = null
        selectedTopicName = null
        selectedLevelId = null
        selectedLevelName = null
    }
}
