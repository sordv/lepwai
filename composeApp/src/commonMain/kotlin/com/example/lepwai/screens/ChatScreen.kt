package com.example.lepwai.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lepwai.chat.ChatViewModel
import com.example.lepwai.data.SettingsRepo
import com.example.lepwai.theme.AppColors
import com.example.lepwai.theme.TextInputField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    settingsRepo: SettingsRepo
) {
    val login = remember { settingsRepo.getCurrentLogin() }

    if (login == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.BackgroundBlack),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Пользователь не авторизован",
                color = AppColors.ErrorRed,
                fontSize = 24.sp
            )
        }
        return
    }

    val vm = remember(login) { ChatViewModel(login) }

    val chats by vm.chats.collectAsState()
    val messages by vm.messages.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val currentChat by vm.currentChatId.collectAsState()

    LaunchedEffect(currentChat) {
        if (currentChat != null) {
            vm.startPolling()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            vm.stopPolling()
        }
    }

    var drawer by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var deleteChatId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) { vm.loadChats() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundBlack)
    ) {
        Column {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(65.dp)
                        //.background(AppColors.DifficultyMedium) // TODO убрать
                        .clickable { drawer = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Чаты",
                        tint = AppColors.ButtonGray,
                        modifier = Modifier.size(45.dp)
                    )
                }

                // Заголовок
                //Text(
                //    "Чат",
                //    color = AppColors.TextWhite,
                //    fontSize = 36.sp
                //)

                Box(
                    modifier = Modifier
                        .size(65.dp)
                        //.background(AppColors.DifficultyMedium) // TODO убрать
                        .clickable { vm.startNewChat() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Новый чат",
                        tint = AppColors.ButtonGray,
                        modifier = Modifier.size(45.dp)
                    )
                }
            }

            // Список сообщений
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(messages) { message ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (message.isUser)
                                AppColors.MainBlue
                            else
                                AppColors.BackGroundMediumGray,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .wrapContentWidth()
                        ) {
                            Text(
                                message.text,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                color = AppColors.TextWhite,
                                fontSize = 28.sp
                            )
                        }
                    }
                }
            }

            // Панель ввода сообщения
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Поле ввода
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = AppColors.TextWhite,
                        fontSize = 28.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = AppColors.BackGroundMediumGray,
                        focusedContainerColor = AppColors.BackGroundMediumGray,
                        unfocusedIndicatorColor = AppColors.BackGroundMediumGray,
                        focusedIndicatorColor = AppColors.BackGroundMediumGray,
                        cursorColor = AppColors.TextWhite,
                        unfocusedTextColor = AppColors.TextWhite,
                        focusedTextColor = AppColors.TextWhite
                    ),
                    placeholder = {
                        Text(
                            "Сообщение...",
                            color = AppColors.TextLightGray,
                            fontSize = 28.sp
                        )
                    }
                )

                // Кнопка отправки
                Box(
                    modifier = Modifier
                        .size(65.dp)
                        //.background(AppColors.DifficultyMedium) // TODO убрать
                        .clickable {
                            vm.sendMessage(input)
                            input = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowCircleUp,
                        contentDescription = "Отправить",
                        tint = AppColors.MainBlue,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Боковое меню чатов
        if (drawer) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp),
                color = AppColors.BackgroundDarkGray
            ) {
                Column {
                    // Заголовок меню
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Чаты",
                            color = AppColors.TextWhite,
                            fontSize = 32.sp
                        )

                        Box(
                            modifier = Modifier
                                .size(65.dp)
                                //.background(AppColors.DifficultyMedium) // TODO убрать
                                .clickable { drawer = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть меню",
                                tint = AppColors.ButtonGray,
                                modifier = Modifier.size(45.dp)
                            )
                        }
                    }

                    // Список чатов
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chats) { chat ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                vm.loadChat(chat.id)
                                                drawer = false
                                            },
                                            onLongPress = {
                                                deleteChatId = chat.id
                                            }
                                        )
                                    }
                                    .padding(start = 12.dp, end = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    chat.title,
                                    color = if (chat.id == currentChat)
                                        AppColors.MainBlue
                                    else
                                        AppColors.TextWhite,
                                    fontSize = 28.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Диалог удаления чата
        if (deleteChatId != null) {
            AlertDialog(
                onDismissRequest = { deleteChatId = null },
                modifier = Modifier.background(AppColors.BackgroundDarkGray),
                title = {
                    Text(
                        "Удалить чат?",
                        color = AppColors.TextWhite,
                        fontSize = 28.sp
                    )
                },
                text = {
                    Text(
                        "Вы уверены, что хотите удалить этот чат?",
                        color = AppColors.TextWhite,
                        fontSize = 24.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.deleteChat(deleteChatId!!)
                            deleteChatId = null
                        }
                    ) {
                        Text(
                            "Удалить",
                            color = AppColors.ErrorRed,
                            fontSize = 24.sp
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { deleteChatId = null }
                    ) {
                        Text(
                            "Отмена",
                            color = AppColors.TextWhite,
                            fontSize = 24.sp
                        )
                    }
                },
                containerColor = AppColors.BackgroundDarkGray
            )
        }
    }
}