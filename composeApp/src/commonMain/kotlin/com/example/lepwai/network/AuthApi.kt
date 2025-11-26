package com.example.lepwai.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import io.ktor.client.request.setBody
import io.ktor.http.contentType

@Serializable
data class RegisterRequest(val login: String, val password: String, val passwordRepeat: String)
@Serializable
data class RegisterResponse(val ok: Boolean, val error: String? = null)
@Serializable
data class LoginRequest(val login: String, val password: String)
@Serializable
data class LoginResponse(val ok: Boolean, val error: String? = null, val login: String? = null)

class AuthApi(private val client: HttpClient, private val baseUrl: String) {

    suspend fun register(login: String, password: String, passwordRepeat: String): RegisterResponse {
        return client.post("$baseUrl/register") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(RegisterRequest(login,password,passwordRepeat))
        }.body<RegisterResponse>()
    }

    suspend fun login(login: String, password: String): LoginResponse {
        return client.post("$baseUrl/login") {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(LoginRequest(login,password))
        }.body<LoginResponse>()
    }
}
