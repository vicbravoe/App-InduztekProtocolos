package com.example.induztekprotocolos.model

import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.MomentoPrueba
import com.example.induztekprotocolos.domain.ResultadoPrueba
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.TipoEquipo
import com.example.induztekprotocolos.domain.TipoPrueba
import com.example.induztekprotocolos.domain.TipoVisita

data class Usuario(
    val id: Long = 0,
    val rol: Rol,
    val rut: String,
    val nombre: String,
    val correo: String,
    val telefono: String = "",
    val activo: Boolean = true,
    val giro: String? = null,
    val contactoNombre: String? = null,
    val contactoCargo: String? = null
)

data class Instalacion(
    val id: Long = 0,
    val idUsuarioCliente: Long?,
    val nombre: String,
    val comuna: String,
    val observacionesAcceso: String = ""
)

data class Equipo(
    val id: Long = 0,
    val idInstalacion: Long,
    val tag: String,
    val tipo: TipoEquipo,
    val marca: String = "",
    val numeroSerie: String = "",
    val tensiones: String = "",
    val maxContactosUohm: Double? = null
)

data class Instrumento(
    val id: Long = 0,
    val nombre: String,
    val marca: String = "",
    val modelo: String = "",
    val numeroSerie: String = "",
    val fechaUltimaCalibracion: Long,
    val fechaVencimiento: Long
)

data class Visita(
    val id: Long = 0,
    val numeroOt: String,
    val idInstalacion: Long,
    val fecha: Long,
    val tipoVisita: TipoVisita,
    val idIngenieroCargo: Long,
    val fechaHoraCierre: Long? = null,
    val receptorNombre: String? = null,
    val receptorRut: String? = null,
    val receptorCargo: String? = null,
    val participantes: List<Long> = emptyList()
) {
    val cerrada get() = fechaHoraCierre != null
}

data class Protocolo(
    val id: Long = 0,
    val numero: String,
    val idVisita: Long,
    val idEquipo: Long,
    val idUsuarioRegistra: Long,
    val fechaHoraPrueba: Long,
    val temperaturaC: Double? = null,
    val humedadPct: Double? = null,
    val descripcionMantenimiento: String? = null,
    val estado: EstadoProtocolo = EstadoProtocolo.BORRADOR,
    val tieneFoto: Boolean = false,
    @Transient val foto: ByteArray? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

data class Prueba(
    val id: Long = 0,
    val idProtocolo: Long,
    val tipoPrueba: TipoPrueba,
    val elemento: String,
    val momento: MomentoPrueba = MomentoPrueba.UNICA,
    val realizada: Boolean = true,
    val motivoNoRealizada: String? = null,
    val idInstrumento: Long? = null,
    val resultado: ResultadoPrueba? = null,
    val valorDerivado: Double? = null,
    val etiquetaDerivada: String? = null,
    val mediciones: List<Medicion> = emptyList()
)

data class Medicion(val id: Long = 0, val idPrueba: Long, val parametro: String, val valor: Double, val unidad: String)

data class HistorialEstado(
    val id: Long = 0,
    val idProtocolo: Long,
    val estadoAnterior: EstadoProtocolo?,
    val estadoNuevo: EstadoProtocolo,
    val idUsuario: Long,
    val fechaHora: Long,
    val motivo: String? = null
)
