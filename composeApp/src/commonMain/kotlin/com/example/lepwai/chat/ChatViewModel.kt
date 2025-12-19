package com.example.lepwai.chat

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

data class UiMessage(
    val id: Int,
    val isUser: Boolean,
    val text: String
)

class ChatViewModel(
    private val login: String
) {

    private val client = createHttpClient()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val chats = MutableStateFlow<List<ChatDto>>(emptyList())
    val messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val currentChatId = MutableStateFlow<Int?>(null)

    fun loadChats() = scope.launch {
        chats.value = client.get("http://10.0.2.2:8080/chat/list") {
            header("X-User-Login", login)
        }.body()
    }

    fun loadChat(id: Int) = scope.launch {
        currentChatId.value = id

        val list = client.get("http://10.0.2.2:8080/chat/$id") {
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

        // 1. Оптимистично показываем сообщение сразу
        val tempMsg = UiMessage(
            id = -System.currentTimeMillis().toInt(),
            isUser = true,
            text = text
        )
        messages.value = messages.value + tempMsg

        try {
            // 2. Получаем ответ КАК ТЕКСТ
            val responseText = client.post("http://10.0.2.2:8080/chat/send") {
                header("X-User-Login", login)
                contentType(ContentType.Application.Json)
                setBody(SendMessageRequest(chatId, text))
            }.bodyAsText()

            // 3. Парсим JSON вручную
            val json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            }

            // 4. Проверяем: это ошибка?
            val element = json.parseToJsonElement(responseText)

            if (element is kotlinx.serialization.json.JsonObject && element["error"] != null) {
                // сервер вернул ошибку
                messages.value = messages.value + UiMessage(
                    id = -1,
                    isUser = false,
                    text = "Ошибка сервера. Попробуйте позже."
                )
                return@launch
            }

            // 5. Это нормальный ответ
            val resp = json.decodeFromString<SendMessageResponse>(responseText)

            currentChatId.value = resp.chatId

            messages.value = resp.messages.map {
                UiMessage(it.id, it.isUserMsg, it.content)
            }

            loadChats()

        } catch (e: Exception) {
            println("SERVER ERROR: ${e.message}")

            messages.value = messages.value + UiMessage(
                id = -1,
                isUser = false,
                text = "Ошибка сервера. Попробуйте позже."
            )
        }
    }

    fun deleteChat(id: Int) = scope.launch {
        client.delete("http://10.0.2.2:8080/chat/$id") {
            header("X-User-Login", login)
        }
        loadChats()
    }

    fun startNewChat() {
        currentChatId.value = null
        messages.value = emptyList()
    }
}