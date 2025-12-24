package com.example.lepwai.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import io.ktor.http.ContentType
import io.ktor.http.contentType

@Serializable data class Level(val id: Int, val name: String, val sort: Int, val parent: Int, val value: String, val answer: String? = null, val difficulty: Int?)
@Serializable data class LevelProgressDto(val levelId: Int, val status: String, val answer: String?)
@Serializable data class CompleteLevelRequest(val login: String, val levelId: Int)
@Serializable data class RunPracticeRequest(val login: String, val levelId: Int, val code: String)
@Serializable data class RunPracticeResponse(val status: String, val output: String)
class ChooseLevelApi(private val client: HttpClient, private val baseUrl: String) {
    suspend fun getLevelsForTopic(topicId: Int): List<Level> {
        return client.get("$baseUrl/topics/$topicId/levels").body()
    }

    suspend fun getLevelById(levelId: Int): Level {
        return client.get("$baseUrl/levels/$levelId").body()
    }

    suspend fun getUserProgress(login: String): List<LevelProgressDto> =
        client.get("$baseUrl/user/$login/progress").body()

    suspend fun completeLevel(login: String, levelId: Int) {
        client.post("$baseUrl/levels/complete") {
            contentType(ContentType.Application.Json)
            setBody(CompleteLevelRequest(login, levelId))
        }
    }

    suspend fun runPractice(login: String, levelId: Int, code: String): RunPracticeResponse {
        return client.post("$baseUrl/levels/run-practice") {
            contentType(ContentType.Application.Json)
            setBody(RunPracticeRequest(login, levelId, code))
        }.body()
    }

}