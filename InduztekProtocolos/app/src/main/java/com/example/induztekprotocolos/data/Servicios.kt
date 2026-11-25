package com.example.induztekprotocolos.data

import android.content.Context
import com.example.induztekprotocolos.data.remoto.InduztekApi
import com.example.induztekprotocolos.data.remoto.Red

object Servicios {

    private var urlActual: String? = null
    private var apiActual: InduztekApi? = null

    @Synchronized
    fun api(context: Context): InduztekApi {
        val url = ConfigServidor(context).leer()
        val existente = apiActual
        if (existente != null && url == urlActual) return existente
        val nueva = Red.crearApi(url)
        apiActual = nueva
        urlActual = url
        return nueva
    }

    fun proveedorApi(context: Context): () -> InduztekApi {
        val aplicacion = context.applicationContext
        return { api(aplicacion) }
    }
}
