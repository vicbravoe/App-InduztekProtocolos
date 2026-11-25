package com.example.induztekprotocolos.data

import android.content.Context
import androidx.core.content.edit
import com.example.induztekprotocolos.BuildConfig

class ConfigServidor(context: Context) {

    private val prefs = context.getSharedPreferences("servidor", Context.MODE_PRIVATE)

    fun leer(): String = prefs.getString(CLAVE_URL, null) ?: BuildConfig.URL_SERVIDOR

    fun guardar(url: String) {
        prefs.edit { putString(CLAVE_URL, url) }
    }

    private companion object {
        const val CLAVE_URL = "url"
    }
}
