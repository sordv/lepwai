package com.example.lepwai

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.lepwai.screens.ChatScreen
import com.example.lepwai.screens.ProfileScreen
import com.example.lepwai.screens.LearningRootScreen
import com.example.lepwai.theme.AppColors
import com.example.lepwai.data.LearningNavigationState
import com.example.lepwai.data.SettingsRepo
import com.example.lepwai.network.AuthApi
import com.example.lepwai.network.createHttpClient
import com.example.lepwai.screens.LoginScreen
import com.example.lepwai.screens.RegisterScreen
import io.ktor.client.*

@Composable
fun App(settingsRepo: SettingsRepo) {
    val httpClient: HttpClient = remember { createHttpClient() }
    val authApi: AuthApi = remember { AuthApi(httpClient, "http://10.0.2.2:8080") }
    var loggedInUser by remember { mutableStateOf(settingsRepo.loadLogin()) }
    var authScreen by remember { mutableStateOf("login") }

    // Если никто не вошёл — показываем экран входа/регистрации
    if (loggedInUser == null) {
        when (authScreen) {
            "login" -> LoginScreen(
                authApi = authApi,
                settingsRepo = settingsRepo,
                onLoginSuccess = { login ->
                    settingsRepo.saveCurrentLogin(login)
                    loggedInUser = login
                },
                onNavigateToRegister = { authScreen = "register" }
            )
            "register" -> RegisterScreen(
                authApi = authApi,
                settingsRepo = settingsRepo,
                onRegisterSuccess = { login ->
                    settingsRepo.saveCurrentLogin(login)
                    loggedInUser = login
                },
                onNavigateToLogin = { authScreen = "login" }
            )
        }
        return
    }

    // if logged in
    var selectedScreen by remember { mutableStateOf("learning") }

    // Learning navigation state (переходит в LearningRootScreen)
    val learningState = remember { LearningNavigationState() }

    val screens = listOf(
        Screen("learning", "Обучение", Icons.Default.School),
        Screen("chat", "Чат", Icons.Default.Chat),
        Screen("profile", "Профиль", Icons.Default.Person)
    )

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = AppColors.BackgroundDarkGray,
                    modifier = Modifier.height(110.dp)
                ) {
                    screens.forEach { screen ->
                        NavigationBarItem(
                            selected = selectedScreen == screen.route,
                            onClick = {
                                if (selectedScreen == "learning" && screen.route == "learning") {
                                    // пользователь уже в разделе "Обучение" — сбрасываем состояние и показываем список курсов
                                    learningState.selectedCourseId = null
                                    learningState.selectedCourseName = null
                                    learningState.selectedTopicId = null
                                    learningState.selectedTopicName = null
                                    learningState.selectedLevelId = null
                                    learningState.selectedLevelName = null
                                }
                                selectedScreen = screen.route
                            },
                            icon = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.label,
                                        modifier = Modifier.size(66.dp)
                                    )
                                }
                            },
                            label = null,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.MainBlue,
                                unselectedIconColor = AppColors.ButtonGray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (selectedScreen) {
                    "learning" -> LearningRootScreen(
                        learningState = learningState,
                        onResetToCourses = {
                            learningState.selectedCourseId = null
                            learningState.selectedTopicId = null
                            learningState.selectedLevelId = null
                        }
                    )
                    "chat" -> ChatScreen(settingsRepo = settingsRepo)
                    "profile" -> ProfileScreen(
                        settingsRepo = settingsRepo,
                        onLogout = {
                            settingsRepo.saveCurrentLogin(null)
                            loggedInUser = null
                        }
                    )
                }
            }
        }
    }
}

data class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
)