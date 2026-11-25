package com.example.induztekprotocolos

import com.example.induztekprotocolos.data.ArmadoVistas
import com.example.induztekprotocolos.data.remoto.ProtocoloDetalleDto
import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.ResultadoCalculator
import com.example.induztekprotocolos.domain.ResultadoPrueba
import com.example.induztekprotocolos.domain.TipoEquipo
import com.example.induztekprotocolos.domain.TipoPrueba
import com.example.induztekprotocolos.model.Equipo
import com.example.induztekprotocolos.model.Medicion
import com.example.induztekprotocolos.model.Protocolo
import com.example.induztekprotocolos.model.Prueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculoResultadosTest {

    private fun aislacion(l1: Double, l10: Double, tipo: TipoEquipo = TipoEquipo.TRANSFORMADOR) =
        ResultadoCalculator.calcular(TipoPrueba.AISLACION, tipo, mapOf("LECTURA_1MIN" to l1, "LECTURA_10MIN" to l10), null)

    @Test
    fun aislacionDeTransformadorSegunIndiceDePolarizacion() {
        assertEquals(ResultadoPrueba.APROBADO, aislacion(1000.0, 2500.0).resultado)
        assertEquals(2.5, aislacion(1000.0, 2500.0).valorDerivado)
        assertEquals(ResultadoPrueba.OBSERVADO, aislacion(1000.0, 1500.0).resultado)
        assertEquals(ResultadoPrueba.RECHAZADO, aislacion(1000.0, 900.0).resultado)
    }

    @Test
    fun aislacionConLecturaBajaSiempreSeRechaza() {
        assertEquals(ResultadoPrueba.RECHAZADO, aislacion(90.0, 500.0).resultado)
    }

    @Test
    fun aislacionDeInterruptorNoUsaIndiceDePolarizacion() {
        val r = ResultadoCalculator.calcular(TipoPrueba.AISLACION, TipoEquipo.INTERRUPTOR, mapOf("LECTURA_1MIN" to 500.0), null)
        assertEquals(ResultadoPrueba.APROBADO, r.resultado)
    }

    @Test
    fun relacionDeTransformacionSegunErrorPorcentual() {
        fun ttr(medida: Double) = ResultadoCalculator.calcular(TipoPrueba.TTR, TipoEquipo.TRANSFORMADOR, mapOf("RELACION_TEORICA" to 100.0, "RELACION_MEDIDA" to medida), null).resultado
        assertEquals(ResultadoPrueba.APROBADO, ttr(100.3))
        assertEquals(ResultadoPrueba.OBSERVADO, ttr(100.8))
        assertEquals(ResultadoPrueba.RECHAZADO, ttr(101.5))
    }

    @Test
    fun contactosSeCompararConElMaximoDePlaca() {
        fun contactos(valor: Double, maximo: Double?) =
            ResultadoCalculator.calcular(TipoPrueba.CONTACTOS, TipoEquipo.CELDA_MT, mapOf("RESISTENCIA_CONTACTO" to valor), maximo).resultado
        assertEquals(ResultadoPrueba.APROBADO, contactos(55.0, 60.0))
        assertEquals(ResultadoPrueba.OBSERVADO, contactos(66.0, 60.0))
        assertEquals(ResultadoPrueba.RECHAZADO, contactos(80.0, 60.0))
        assertNull(contactos(55.0, null))
    }

    @Test
    fun operacionDeReleSegunDesviacionDelTiempo() {
        fun rele(medido: Double) = ResultadoCalculator.calcular(TipoPrueba.OPERACION_RELE, TipoEquipo.RELE_PROTECCION, mapOf("TIEMPO_ESPERADO" to 100.0, "TIEMPO_MEDIDO" to medido), null).resultado
        assertEquals(ResultadoPrueba.APROBADO, rele(104.0))
        assertEquals(ResultadoPrueba.OBSERVADO, rele(108.0))
        assertEquals(ResultadoPrueba.RECHAZADO, rele(115.0))
    }

    @Test
    fun resistenciaDeDevanadosLaDecideElIngeniero() {
        val r = ResultadoCalculator.calcular(TipoPrueba.DEVANADOS, TipoEquipo.TRANSFORMADOR, mapOf("RESISTENCIA" to 12.0), null)
        assertNull(r.resultado)
    }

    @Test
    fun resultadoGlobalEsElPeorDeLasPruebas() {
        assertEquals(ResultadoPrueba.OBSERVADO, ResultadoCalculator.resultadoGlobal(listOf(ResultadoPrueba.APROBADO, ResultadoPrueba.OBSERVADO)))
        assertEquals(ResultadoPrueba.RECHAZADO, ResultadoCalculator.resultadoGlobal(listOf(ResultadoPrueba.APROBADO, ResultadoPrueba.RECHAZADO, ResultadoPrueba.OBSERVADO)))
        assertNull(ResultadoCalculator.resultadoGlobal(emptyList()))
    }

    @Test
    fun variacionContraLaPruebaAnterior() {
        assertTrue(ResultadoCalculator.variacionSignificativa(130.0, 100.0))
        assertFalse(ResultadoCalculator.variacionSignificativa(110.0, 100.0))
        assertEquals(-20.0, ResultadoCalculator.porcentajeVariacion(80.0, 100.0))
    }

    private val equipo = Equipo(id = 1, idInstalacion = 1, tag = "TR-01", tipo = TipoEquipo.TRANSFORMADOR)

    private fun protocolo(id: Long, fecha: Long, estado: EstadoProtocolo = EstadoProtocolo.FORMALIZADO) =
        Protocolo(id = id, numero = "PT-$id", idVisita = 1, idEquipo = 1, idUsuarioRegistra = 1, fechaHoraPrueba = fecha, estado = estado)

    private fun prueba(valor: Double, resultado: ResultadoPrueba) = Prueba(
        idProtocolo = 1, tipoPrueba = TipoPrueba.AISLACION, elemento = "Fase A", resultado = resultado,
        valorDerivado = valor, mediciones = listOf(Medicion(idPrueba = 1, parametro = "LECTURA_1MIN", valor = valor, unidad = "MΩ"))
    )

    @Test
    fun armadoDeVistaComparaConLaPruebaAnteriorDelMismoElemento() {
        val anterior = ProtocoloDetalleDto(protocolo(1, 1000), listOf(prueba(100.0, ResultadoPrueba.APROBADO)), emptyList())
        val actual = protocolo(2, 2000, EstadoProtocolo.BORRADOR)
        val historial = listOf(anterior, ProtocoloDetalleDto(actual, emptyList(), emptyList()))
        val vista = ArmadoVistas.construir(actual, equipo, listOf(prueba(130.0, ResultadoPrueba.OBSERVADO)), historial, emptyMap())
        val p = vista.pruebas.single()
        assertEquals(100.0, p.valorAnterior)
        assertEquals(30.0, p.variacionPct)
        assertTrue(p.significativa)
        assertEquals(ResultadoPrueba.OBSERVADO, vista.global)
    }

    @Test
    fun armadoDeVistaIgnoraProtocolosAnuladosYPosteriores() {
        val anulado = ProtocoloDetalleDto(protocolo(1, 1000, EstadoProtocolo.ANULADO), listOf(prueba(100.0, ResultadoPrueba.APROBADO)), emptyList())
        val posterior = ProtocoloDetalleDto(protocolo(3, 3000), listOf(prueba(500.0, ResultadoPrueba.APROBADO)), emptyList())
        val actual = protocolo(2, 2000)
        val vista = ArmadoVistas.construir(actual, equipo, listOf(prueba(130.0, ResultadoPrueba.APROBADO)), listOf(anulado, posterior), emptyMap())
        assertNull(vista.pruebas.single().valorAnterior)
        assertNull(vista.pruebas.single().variacionPct)
    }
}
