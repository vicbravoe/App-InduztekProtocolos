package com.example.induztekprotocolos.data.remoto

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class ClimaActualDto(
    @SerializedName("temperature_2m") val temperatura: Double?,
    @SerializedName("relative_humidity_2m") val humedad: Double?
)

data class RespuestaClimaDto(val current: ClimaActualDto?)

interface ClimaApi {

    @GET("v1/forecast")
    suspend fun climaActual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") variables: String
    ): RespuestaClimaDto
}
