package com.example.lepwai

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform