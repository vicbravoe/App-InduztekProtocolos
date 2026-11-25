package com.example.induztekprotocolos.data.remoto

import com.google.gson.Gson
import com.google.gson.JsonParseException

object ErrorApi {

    const val SIN_CONEXION = "No hay conexión con el servidor. Revisa la dirección del servidor y tu red"
    const val RESPUESTA_INVALIDA = "El servidor respondió algo que la app no entiende"

    fun mensaje(codigo: Int, cuerpo: String?): String {
        val delServidor = leerMensaje(cuerpo)
        if (!delServidor.isNullOrBlank()) return delServidor
        return when (codigo) {
            400 -> "Los datos enviados no son válidos"
            401 -> "Correo o contraseña incorrectos"
            404 -> "No se encontró el recurso pedido"
            409 -> "La operación entra en conflicto con datos existentes"
            in 500..599 -> "El servidor tuvo un problema. Inténtalo más tarde"
            else -> "No se pudo completar la operación (código $codigo)"
        }
    }

    private fun leerMensaje(cuerpo: String?): String? {
        if (cuerpo.isNullOrBlank()) return null
        return try {
            Gson().fromJson(cuerpo, ErrorServidor::class.java)?.mensaje
        } catch (e: JsonParseException) {
            null
        }
    }
}
