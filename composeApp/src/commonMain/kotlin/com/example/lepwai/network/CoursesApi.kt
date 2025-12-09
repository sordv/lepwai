package com.example.lepwai.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@Serializable
data class Course(val id: Int, val name: String, val sort: Int)

class CoursesApi(private val client: HttpClient, private val baseUrl: String) {
    suspend fun getCourses(): List<Course> {
        return client.get("$baseUrl/courses").body<List<Course>>()
    }
}
