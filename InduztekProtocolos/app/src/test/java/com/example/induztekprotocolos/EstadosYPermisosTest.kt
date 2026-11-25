package com.example.induztekprotocolos

import com.example.induztekprotocolos.domain.CatalogoPruebas
import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.GeneradorNumeros
import com.example.induztekprotocolos.domain.Permiso
import com.example.induztekprotocolos.domain.PermisosRol
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.TipoEquipo
import com.example.induztekprotocolos.domain.TipoPrueba
import com.example.induztekprotocolos.domain.TransicionesEstado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EstadosYPermisosTest {

    @Test
    fun soloIngenieroYJefeFormalizan() {
        assertFalse(TransicionesEstado.permitida(EstadoProtocolo.REGISTRADO_TERRENO, EstadoProtocolo.FORMALIZADO, Rol.TECNICO))
        assertTrue(TransicionesEstado.permitida(EstadoProtocolo.REGISTRADO_TERRENO, EstadoProtocolo.FORMALIZADO, Rol.INGENIERO))
        assertTrue(TransicionesEstado.permitida(EstadoProtocolo.REGISTRADO_TERRENO, EstadoProtocolo.FORMALIZADO, Rol.JEFE_TECNICO))
    }

    @Test
    fun unProtocoloFormalizadoNoSeAnula() {
        assertFalse(TransicionesEstado.permitida(EstadoProtocolo.FORMALIZADO, EstadoProtocolo.ANULADO, Rol.JEFE_TECNICO))
        assertTrue(TransicionesEstado.esFinal(EstadoProtocolo.FORMALIZADO))
    }

    @Test
    fun elTecnicoPuedeVolverARegistrarUnProtocoloObservado() {
        assertTrue(TransicionesEstado.permitida(EstadoProtocolo.OBSERVADO, EstadoProtocolo.REGISTRADO_TERRENO, Rol.TECNICO))
        assertTrue(TransicionesEstado.permitida(EstadoProtocolo.BORRADOR, EstadoProtocolo.REGISTRADO_TERRENO, Rol.TECNICO))
    }

    @Test
    fun observarYAnularExigenMotivo() {
        assertTrue(TransicionesEstado.requiereMotivo(EstadoProtocolo.OBSERVADO))
        assertTrue(TransicionesEstado.requiereMotivo(EstadoProtocolo.ANULADO))
        assertFalse(TransicionesEstado.requiereMotivo(EstadoProtocolo.FORMALIZADO))
    }

    @Test
    fun soloSeEditaEnBorradorYObservado() {
        assertTrue(TransicionesEstado.editable(EstadoProtocolo.BORRADOR))
        assertTrue(TransicionesEstado.editable(EstadoProtocolo.OBSERVADO))
        assertFalse(TransicionesEstado.editable(EstadoProtocolo.REGISTRADO_TERRENO))
    }

    @Test
    fun matrizDePermisosPorRol() {
        assertFalse(PermisosRol.tiene(Rol.TECNICO, Permiso.FORMALIZAR_OBSERVAR))
        assertTrue(PermisosRol.tiene(Rol.JEFE_TECNICO, Permiso.GESTIONAR_INSTRUMENTOS))
        assertTrue(PermisosRol.tiene(Rol.CLIENTE, Permiso.CONSULTAR_PROPIA_EMPRESA))
        assertFalse(PermisosRol.tiene(Rol.CLIENTE, Permiso.REGISTRAR_PROTOCOLO))
    }

    @Test
    fun catalogoDePruebasPorTipoDeEquipo() {
        assertEquals(3, CatalogoPruebas.pruebasDe(TipoEquipo.TRANSFORMADOR).size)
        assertTrue(CatalogoPruebas.parametrosDe(TipoPrueba.AISLACION, TipoEquipo.CELDA_MT).none { it.codigo == "LECTURA_10MIN" })
    }

    @Test
    fun numeracionDeProtocolosYOrdenesDeTrabajo() {
        assertTrue(GeneradorNumeros.numeroProtocolo().startsWith("PT-"))
        assertTrue(GeneradorNumeros.numeroOt().startsWith("OT-"))
    }
}
