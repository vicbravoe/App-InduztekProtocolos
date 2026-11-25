package com.example.induztekprotocolos.model

import java.util.Locale

data class ClimaActual(val temperaturaC: Double, val humedadPct: Double) {

    fun temperaturaTexto(): String = String.format(Locale.US, "%.1f", temperaturaC)

    fun humedadTexto(): String = Math.round(humedadPct).toString()

    fun resumen(): String = "Clima actual (Open-Meteo): ${temperaturaTexto()} °C · ${humedadTexto()} % de humedad"
}
