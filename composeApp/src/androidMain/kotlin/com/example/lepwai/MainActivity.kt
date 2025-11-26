package com.example.lepwai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.russhwolf.settings.SharedPreferencesSettings
import com.example.lepwai.data.SettingsRepoImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settings = SharedPreferencesSettings(
            getSharedPreferences("lepwai_prefs", MODE_PRIVATE)
        )

        val settingsRepo = SettingsRepoImpl(settings)

        setContent { App(settingsRepo) }
    }
}
