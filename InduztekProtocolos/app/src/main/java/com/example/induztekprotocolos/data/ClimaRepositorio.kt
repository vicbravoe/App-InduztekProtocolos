package com.example.induztekprotocolos.data

import com.example.induztekprotocolos.data.remoto.ClimaApi
import com.example.induztekprotocolos.data.remoto.ErrorApi
import com.example.induztekprotocolos.domain.ErrorNegocio
import com.example.induztekprotocolos.model.ClimaActual
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException

class ClimaRepositorio(private val api: ClimaApi) {

    suspend fun obtener(latitud: Double, longitud: Double): ClimaActual {
        val respuesta = try {
            api.climaActual(latitud, longitud, VARIABLES)
        } catch (e: HttpException) {
            throw ErrorNegocio(MENSAJE_SERVICIO, e.code())
        } catch (e: IOException) {
            throw ErrorNegocio(MENSAJE_SIN_CONEXION)
        } catch (e: JsonParseException) {
            throw ErrorNegocio(ErrorApi.RESPUESTA_INVALIDA)
        }
        val temperatura = respuesta.current?.temperatura
        val humedad = respuesta.current?.humedad
        if (temperatura == null || humedad == null) throw ErrorNegocio(MENSAJE_SIN_DATOS)
        return ClimaActual(temperatura, humedad)
    }

    companion object {
        const val VARIABLES = "temperature_2m,relative_humidity_2m"
        const val MENSAJE_SIN_CONEXION = "No se pudo consultar el clima. Revisa la conexión a internet del teléfono"
        const val MENSAJE_SERVICIO = "El servicio de clima no respondió. Inténtalo más tarde o escribe los valores a mano"
        const val MENSAJE_SIN_DATOS = "El servicio de clima no entregó datos para esta ubicación"
    }
}
