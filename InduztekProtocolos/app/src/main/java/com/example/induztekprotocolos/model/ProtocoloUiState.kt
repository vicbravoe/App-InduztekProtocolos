package com.example.induztekprotocolos.model

import com.example.induztekprotocolos.domain.MomentoPrueba
import com.example.induztekprotocolos.domain.ResultadoCalculo
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.TipoPrueba

data class FormAmbiente(
    val temperatura: String = "",
    val humedad: String = "",
    val descripcion: String = "",
    val errorTemperatura: String? = null,
    val errorHumedad: String? = null
)

data class FormPrueba(
    val visible: Boolean = false,
    val tipo: TipoPrueba? = null,
    val elemento: String = "",
    val momento: MomentoPrueba = MomentoPrueba.UNICA,
    val realizada: Boolean = true,
    val motivo: String = "",
    val idInstrumento: Long? = null,
    val valores: Map<String, String> = emptyMap(),
    val erroresValores: Map<String, String> = emptyMap(),
    val errorTipo: String? = null,
    val errorElemento: String? = null,
    val errorMotivo: String? = null,
    val errorInstrumento: String? = null,
    val errorGeneral: String? = null,
    val estimado: ResultadoCalculo? = null
)

data class HistorialItem(val registro: HistorialEstado, val nombreUsuario: String)

data class ProtocoloUiState(
    val vista: ProtocoloVista? = null,
    val visita: Visita? = null,
    val rol: Rol? = null,
    val puedeEditar: Boolean = false,
    val instrumentos: List<Instrumento> = emptyList(),
    val historial: List<HistorialItem> = emptyList(),
    val ambiente: FormAmbiente = FormAmbiente(),
    val prueba: FormPrueba = FormPrueba(),
    val mensaje: String? = null,
    val errorEstado: String? = null,
    val guardados: Int = 0,
    val cargando: Boolean = false,
    val climaCargando: Boolean = false,
    val climaResumen: String? = null
)
