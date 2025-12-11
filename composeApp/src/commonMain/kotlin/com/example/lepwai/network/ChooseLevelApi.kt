package com.example.lepwai.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@Serializable
data class Level(val id: Int, val name: String, val sort: Int, val parent: Int, val value: String, val answer: String?, val difficulty: Int?)

class ChooseLevelApi(private val client: HttpClient, private val baseUrl: String) {
    suspend fun getLevelsForTopic(topicId: Int): List<Level> {
        return client.get("$baseUrl/topics/$topicId/levels").body()
    }

    suspend fun getLevelById(levelId: Int): Level {
        return client.get("$baseUrl/levels/$levelId").body()
    }
}