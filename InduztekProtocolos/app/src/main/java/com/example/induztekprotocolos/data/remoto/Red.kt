package com.example.induztekprotocolos.data.remoto

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Red {

    fun normalizarUrl(texto: String): String {
        var url = texto.trim()
        if (url.isEmpty()) return ""
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://$url"
        if (!url.endsWith("/")) url += "/"
        return url
    }

    fun esUrlValida(texto: String): Boolean = normalizarUrl(texto).toHttpUrlOrNull() != null

    fun crearApi(baseUrl: String): InduztekApi {
        val cliente = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(normalizarUrl(baseUrl))
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InduztekApi::class.java)
    }
}
