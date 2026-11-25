package com.example.induztekprotocolos.model

import com.example.induztekprotocolos.domain.ResultadoPrueba

data class PruebaVista(
    val prueba: Prueba,
    val mediciones: List<Medicion>,
    val instrumento: String?,
    val valorActual: Double?,
    val valorAnterior: Double?,
    val variacionPct: Double?,
    val significativa: Boolean
)

data class ProtocoloVista(
    val protocolo: Protocolo,
    val equipo: Equipo,
    val pruebas: List<PruebaVista>,
    val global: ResultadoPrueba?
)

data class VisitaResumen(
    val visita: Visita,
    val instalacion: String,
    val totalProtocolos: Int,
    val formalizados: Int
)

data class EquipoEnVisita(val equipo: Equipo, val protocolo: Protocolo?)

data class DetalleVisita(
    val visita: Visita,
    val instalacion: Instalacion,
    val equipos: List<EquipoEnVisita>,
    val ingeniero: String
)

data class Res<T>(val valor: T? = null, val error: String? = null) {
    val ok get() = error == null
}
