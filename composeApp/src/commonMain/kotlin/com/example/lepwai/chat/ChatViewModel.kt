package com.example.lepwai.chat

import com.example.lepwai.config.ServerConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import com.example.lepwai.model.*
import com.example.lepwai.network.createHttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType

data class UiMessage(val id: Int, val isUser: Boolean, val text: String)

class ChatViewModel(
    private val login: String
) {

    private val client = createHttpClient()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val chats = MutableStateFlow<List<ChatDto>>(emptyList())
    val messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val currentChatId = MutableStateFlow<Int?>(null)

    private var pollingJob: Job? = null

    fun loadChats(selectLastIfEmpty: Boolean = true) = scope.launch {
        val loadedChats: List<ChatDto> = client.get("${ServerConfig.BASE_URL}/chat/list") {
            header("X-User-Login", login)
        }.body()

        chats.value = loadedChats

        if (selectLastIfEmpty &&
            currentChatId.value == null &&
            loadedChats.isNotEmpty()
        ) { loadChat(loadedChats.first().id) }
    }

    fun loadChat(id: Int) = scope.launch {
        currentChatId.value = id

        val list = client.get("${ServerConfig.BASE_URL}/chat/$id") {
            header("X-User-Login", login)
        }.body<List<MessageDto>>()

        messages.value = list.map {
            UiMessage(
                id = it.id,
                isUser = it.isUserMsg,
                text = it.content
            )
        }
    }

    fun sendMessage(text: String) = scope.launch {
        val chatId = currentChatId.value

        // показываем сразу
        messages.value += UiMessage(
            id = -System.currentTimeMillis().toInt(),
            isUser = true,
            text = text
        )

        try {
            val resp: Map<String, Int> =
                client.post("${ServerConfig.BASE_URL}/chat/send") {
                    header("X-User-Login", login)
                    contentType(ContentType.Application.Json)
                    setBody(SendMessageRequest(chatId, text))
                }.body()

            currentChatId.value = resp["chatId"]
            loadChats(selectLastIfEmpty = false)

        } catch (e: Exception) {
            println("SEND FAILED: ${e.message}")
        }
    }

    fun deleteChat(id: Int) = scope.launch {
        client.delete("${ServerConfig.BASE_URL}/chat/$id") {
            header("X-User-Login", login)
        }
        loadChats()
    }

    fun startNewChat() {
        currentChatId.value = null
        messages.value = emptyList()
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                currentChatId.value?.let { loadChat(it) }
                delay(2000)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }
}