package com.example.lepwai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lepwai.data.SettingsRepo
import com.example.lepwai.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    settingsRepo: SettingsRepo,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var login by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        login = settingsRepo.getCurrentLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.DifficultyEasy) //TODO: UBRAT POTOM
                .padding(25.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = login ?: "",
                color = AppColors.TextWhite,
                fontSize = 36.sp
            )

            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clickable {
                        scope.launch {
                            settingsRepo.saveCurrentLogin(null)
                            onLogout()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Выход",
                    tint = AppColors.ButtonGray,
                    modifier = Modifier.size(45.dp)
                )
            }
        }
    }
}