package com.example.lepwai.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(val id: Int, val title: String)

@Serializable
data class MessageDto(val id: Int, val isUserMsg: Boolean, val content: String)

@Serializable
data class SendMessageRequest(val chatId: Int?, val message: String)

@Serializable
data class SendMessageResponse(val chatId: Int, val messages: List<MessageDto>)
