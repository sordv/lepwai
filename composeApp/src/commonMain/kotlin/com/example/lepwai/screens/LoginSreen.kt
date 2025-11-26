package com.example.lepwai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lepwai.network.AuthApi
import com.example.lepwai.data.SettingsRepo
import kotlinx.coroutines.launch
import com.example.lepwai.theme.AppColors
import com.example.lepwai.theme.TextInputField

@Composable
fun LoginScreen(
    authApi: AuthApi,
    settingsRepo: SettingsRepo,
    onLoginSuccess: (login: String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundBlack)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Войдите",
            fontSize = 36.sp,
            color = AppColors.TextWhite
        )

        Spacer(Modifier.height(24.dp))

        if (error != null) {
            Text(
                error!!,
                fontSize = 27.sp,
                color = AppColors.ErrorRed,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
        }

        TextInputField(
            value = login,
            placeholder = "Логин",
            onValueChange = { login = it}
        )

        Spacer(Modifier.height(14.dp))

        TextInputField(
            value = password,
            placeholder = "Пароль",
            onValueChange = { password = it},
            isPassword = true
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                error = null
                loading = true
                scope.launch {
                    try {
                        val resp = authApi.login(login, password)
                        if (resp.ok && resp.login != null) {
                            settingsRepo.saveCurrentLogin(resp.login)
                            onLoginSuccess(resp.login)
                        } else {
                            error = resp.error ?: "Ошибка входа"
                        }
                    } catch (t: Throwable) {
                        error = "Сбой соединения: ${t.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading,
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .width(280.dp)
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.MainBlue
            )
        ) {
            Text(
                "Вход",
                fontSize = 36.sp,
                color = AppColors.TextWhite
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "Нет аккаунта?",
            fontSize = 27.sp,
            color = AppColors.TextLightGray
        )

        TextButton(onClick = onNavigateToRegister) {
            Text(
                "Зарегистрируйтесь",
                fontSize = 32.sp,
                color = AppColors.TextWhite,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}
