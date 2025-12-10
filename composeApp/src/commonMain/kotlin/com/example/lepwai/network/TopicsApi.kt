package com.example.lepwai.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@Serializable
data class Topic(val id: Int, val name: String, val sort: Int, val parent: Int)

class TopicsApi(private val client: HttpClient, private val baseUrl: String) {

    suspend fun getTopicsForCourse(courseId: Int): List<Topic> {
        return client.get("$baseUrl/courses/$courseId/topics")
            .body()
    }
}
