package ai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object LocalAiClient {

    private const val OLLAMA_URL = "http://localhost:11434/api/chat"
    private const val MODEL = "llama3.1:8b"

    private val client = HttpClient(CIO) {

        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            )
        }

        install(io.ktor.client.plugins.HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 120_000
            socketTimeoutMillis = 120_000
        }
    }


    private const val SYSTEM_PROMPT = """
Ты — ИИ-помощник для изучения программирования.
Объясняй просто и понятным языком.
Помогай с базовыми темами: переменные, типы данных, условия, циклы.
Отвечай на русском языке.
"""

    @Serializable data class Message(val role: String, val content: String)
    @Serializable data class ChatRequest(val model: String, val messages: List<Message>, val stream: Boolean = false)
    @Serializable data class ChatResponse(val message: Message)

    suspend fun ask(messagesFromDb: List<Message>): String {
        val messages = mutableListOf<Message>()
        // system prompt всегда первый
        messages += Message("system", SYSTEM_PROMPT.trim())
        // история чата
        messages += messagesFromDb

        val response: ChatResponse = client.post(OLLAMA_URL) {
            contentType(ContentType.Application.Json)
            setBody(ChatRequest(
                model = MODEL,
                messages = messages
            ))
        }.body()

        return response.message.content
    }
}
