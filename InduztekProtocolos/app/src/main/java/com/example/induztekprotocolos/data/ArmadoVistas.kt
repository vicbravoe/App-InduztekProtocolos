package com.example.induztekprotocolos.data

import com.example.induztekprotocolos.data.remoto.ProtocoloDetalleDto
import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.ResultadoCalculator
import com.example.induztekprotocolos.domain.ResultadoPrueba
import com.example.induztekprotocolos.domain.TipoPrueba
import com.example.induztekprotocolos.model.Equipo
import com.example.induztekprotocolos.model.Protocolo
import com.example.induztekprotocolos.model.ProtocoloVista
import com.example.induztekprotocolos.model.Prueba
import com.example.induztekprotocolos.model.PruebaVista

object ArmadoVistas {

    fun valorPrincipal(prueba: Prueba): Double? = prueba.valorDerivado ?: prueba.mediciones.firstOrNull()?.valor

    fun resultadoGlobal(pruebas: List<Prueba>): ResultadoPrueba? =
        ResultadoCalculator.resultadoGlobal(pruebas.filter { it.realizada }.mapNotNull { it.resultado })

    fun pruebaAnterior(protocolo: Protocolo, tipo: TipoPrueba, elemento: String, historial: List<ProtocoloDetalleDto>): Prueba? =
        historial
            .filter { it.protocolo.id != protocolo.id && it.protocolo.fechaHoraPrueba < protocolo.fechaHoraPrueba && it.protocolo.estado != EstadoProtocolo.ANULADO }
            .sortedByDescending { it.protocolo.fechaHoraPrueba }
            .firstNotNullOfOrNull { anterior -> anterior.pruebas.firstOrNull { it.tipoPrueba == tipo && it.elemento == elemento && it.realizada } }

    fun construir(
        protocolo: Protocolo,
        equipo: Equipo,
        pruebas: List<Prueba>,
        historial: List<ProtocoloDetalleDto>,
        nombresInstrumentos: Map<Long, String>
    ): ProtocoloVista {
        val vistas = pruebas.map { prueba ->
            val actual = valorPrincipal(prueba)
            val anterior = if (prueba.realizada) pruebaAnterior(protocolo, prueba.tipoPrueba, prueba.elemento, historial)?.let { valorPrincipal(it) } else null
            val variacion = if (actual != null && anterior != null) ResultadoCalculator.porcentajeVariacion(actual, anterior) else null
            PruebaVista(
                prueba = prueba,
                mediciones = prueba.mediciones,
                instrumento = prueba.idInstrumento?.let { nombresInstrumentos[it] },
                valorActual = actual,
                valorAnterior = anterior,
                variacionPct = variacion,
                significativa = actual != null && anterior != null && ResultadoCalculator.variacionSignificativa(actual, anterior)
            )
        }
        return ProtocoloVista(protocolo, equipo, vistas, resultadoGlobal(pruebas))
    }
}
