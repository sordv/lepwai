package com.example.lepwai.data

interface SettingsRepo {

    fun loadLogin(): String?

    fun saveLogin(login: String?)

    fun getCurrentLogin(): String?

    fun saveCurrentLogin(login: String?)
}