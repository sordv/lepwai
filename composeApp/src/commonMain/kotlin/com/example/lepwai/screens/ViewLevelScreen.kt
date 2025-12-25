package com.example.lepwai.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lepwai.config.ServerConfig
import com.example.lepwai.network.ChooseLevelApi
import com.example.lepwai.network.Level
import com.example.lepwai.network.createHttpClient
import com.example.lepwai.theme.AppColors
import com.example.lepwai.ui.HtmlView
import kotlinx.coroutines.launch

enum class RunState { None, CompileError, WrongAnswer, HiddenFailed, Success }

@Composable
fun ViewLevelScreen(
    userLogin: String,
    levelId: Int,
    levelName: String,
    courseName: String,
    onBack: () -> Unit = {},
    onOpenChat: (String) -> Unit
) {
    val client = remember { createHttpClient() }
    val api = remember { ChooseLevelApi(client, ServerConfig.BASE_URL) }
    val scope = rememberCoroutineScope()

    var level by remember { mutableStateOf<Level?>(null) }
    var completed by remember { mutableStateOf(false) }
    var savedAnswer by remember { mutableStateOf<String?>(null) }

    var userCode by remember { mutableStateOf("") }
    var output by remember { mutableStateOf<String?>(null) }
    var runState by remember { mutableStateOf(RunState.None) }

    val scrollState = rememberScrollState()

    LaunchedEffect(levelId) {
        level = api.getLevelById(levelId)

        val progress = api.getUserProgress(userLogin)
            .firstOrNull { it.levelId == levelId }

        completed = progress?.status == "done"
        savedAnswer = progress?.answer

        if (completed) {
            runState = RunState.Success
            userCode = savedAnswer ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundBlack)
    ) {

        // ===== TOP BAR =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = AppColors.ButtonGray,
                modifier = Modifier
                    .size(45.dp)
                    .clickable { onBack() }
            )

            Text(
                text = levelName,
                color = AppColors.TextWhite,
                fontSize = 32.sp
            )

            Spacer(Modifier.size(45.dp))
        }

        level?.let { lvl ->

            // ================== ТЕОРИЯ ==================
            if (lvl.difficulty == null) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    HtmlView(htmlFileName = levelId.toString())
                }

                if (!completed) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    api.completeLevel(userLogin, levelId)
                                    completed = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.DoneGreen
                            ),
                            modifier = Modifier
                                .height(52.dp)
                                .fillMaxWidth(0.8f)
                        ) {
                            Text(
                                text = "Закончить",
                                color = AppColors.TextWhite,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

            // ================== ПРАКТИКА ==================
            else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(20.dp)
                ) {
                    Text(
                        text = lvl.value,
                        color = AppColors.TextLightGray,
                        fontSize = 22.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = userCode,
                        onValueChange = {
                            userCode = it
                            runState = RunState.None
                        },
                        enabled = !completed,
                        textStyle = LocalTextStyle.current.copy(
                            color = AppColors.TextWhite,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppColors.BackGroundMediumGray,
                            unfocusedContainerColor = AppColors.BackGroundMediumGray,
                            cursorColor = AppColors.MainBlue,
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    if (runState != RunState.None) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .background(
                                    when (runState) {
                                        RunState.Success -> AppColors.DoneGreen
                                        RunState.HiddenFailed -> AppColors.MainBlue
                                        RunState.WrongAnswer -> AppColors.ErrorRed
                                        RunState.CompileError -> AppColors.DifficultyMedium
                                        else -> AppColors.DifficultyMedium
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (runState) {
                                    RunState.Success -> "Выполнено"
                                    RunState.HiddenFailed -> "Не пройден скрытый тест"
                                    RunState.WrongAnswer -> "Не верно"
                                    RunState.CompileError -> "Ошибка кода"
                                    else -> ""
                                },
                                color = AppColors.TextWhite
                            )
                        }
                    }

                    output?.let {
                        Spacer(Modifier.height(12.dp))
                        Text("Результат:", color = AppColors.TextLightGray)
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.BackGroundMediumGray)
                                .padding(12.dp)
                        ) {
                            Text(it, color = AppColors.TextLightGray)
                        }
                    }
                }

                // ===== КНОПКИ =====
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = "Вопрос",
                        tint = AppColors.ButtonGray,
                        modifier = Modifier
                            .size(65.dp)
                            .clickable {
                                level?.let { lvl ->
                                    val prompt = "Привет, помоги мне с заданием на $courseName: ${lvl.value}"
                                    onOpenChat(prompt)
                                }
                            }
                    )

                    if (!completed) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Запустить",
                            tint = AppColors.MainBlue,
                            modifier = Modifier
                                .size(65.dp)
                                .clickable {
                                    scope.launch {
                                        val result = api.runPractice(
                                            login = userLogin,
                                            levelId = levelId,
                                            code = userCode
                                        )

                                        output = result.output
                                        runState = when (result.status) {
                                            "compile_error" -> RunState.CompileError
                                            "wrong_answer" -> RunState.WrongAnswer
                                            "hidden_failed" -> RunState.HiddenFailed
                                            "success" -> {
                                                completed = true
                                                RunState.Success
                                            }
                                            else -> RunState.None
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}
