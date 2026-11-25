package com.example.induztekprotocolos.data.remoto

import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.ResultadoPrueba
import com.example.induztekprotocolos.model.HistorialEstado
import com.example.induztekprotocolos.model.Protocolo
import com.example.induztekprotocolos.model.Prueba
import com.example.induztekprotocolos.model.Visita

data class VisitaResumenDto(val visita: Visita, val totalProtocolos: Int, val formalizados: Int)

data class ProtocoloDetalleDto(val protocolo: Protocolo, val pruebas: List<Prueba>, val historial: List<HistorialEstado>)

data class CierreVisitaDto(val receptorNombre: String, val receptorRut: String, val receptorCargo: String)

data class NuevoProtocoloDto(val numero: String, val idVisita: Long, val idEquipo: Long, val idUsuarioRegistra: Long, val fechaHoraPrueba: Long)

data class AmbienteDto(val temperaturaC: Double, val humedadPct: Double, val descripcionMantenimiento: String?)

data class EvidenciaDto(val fotoBase64: String?, val latitud: Double?, val longitud: Double?)

data class FotoDto(val fotoBase64: String)

data class CambioEstadoDto(val estado: EstadoProtocolo, val idUsuario: Long, val motivo: String?)

data class ResultadoPruebaDto(val resultado: ResultadoPrueba)

data class CredencialesDto(val correo: String, val clave: String)

data class ErrorServidor(val estado: Int = 0, val mensaje: String? = null)
