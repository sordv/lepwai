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
import com.example.lepwai.screens.LearningScreen
import com.example.lepwai.screens.ProfileScreen
import com.example.lepwai.theme.AppColors

@Composable
fun App() {
    var selectedScreen by remember { mutableStateOf("learning") }

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
                            onClick = { selectedScreen = screen.route },
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
                    "learning" -> LearningScreen()
                    "chat" -> ChatScreen()
                    "profile" -> ProfileScreen()
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