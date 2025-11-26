package com.example.lepwai.data

import com.russhwolf.settings.Settings

class SettingsRepoImpl(
    private val settings: Settings
) : SettingsRepo {

    private val KEY_LOGIN = "login"

    override fun loadLogin(): String? =
        settings.getStringOrNull(KEY_LOGIN)

    override fun saveLogin(login: String?) {
        if (login == null) settings.remove(KEY_LOGIN)
        else settings.putString(KEY_LOGIN, login)
    }

    override fun getCurrentLogin(): String? =
        settings.getStringOrNull(KEY_LOGIN)

    override fun saveCurrentLogin(login: String?) {
        if (login == null) settings.remove(KEY_LOGIN)
        else settings.putString(KEY_LOGIN, login)
    }
}