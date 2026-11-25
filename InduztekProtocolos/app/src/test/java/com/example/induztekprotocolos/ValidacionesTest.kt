package com.example.induztekprotocolos

import com.example.induztekprotocolos.domain.Rut
import com.example.induztekprotocolos.domain.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesTest {

    @Test
    fun rutConDigitoVerificadorCorrecto() {
        assertTrue(Rut.esValido("17.284.519-3"))
        assertTrue(Rut.esValido("76.482.915-8"))
        assertTrue(Rut.esValido("13.871.905-7"))
    }

    @Test
    fun rutConDigitoVerificadorAlteradoOMalFormado() {
        assertFalse(Rut.esValido("17.284.519-4"))
        assertFalse(Rut.esValido("abc"))
        assertFalse(Rut.esValido(""))
    }

    @Test
    fun rutSeFormateaConPuntosYGuion() {
        assertEquals("17.284.519-3", Rut.formatear("172845193"))
    }

    @Test
    fun temperaturaAceptaComaYRespetaElRango() {
        assertEquals(25.5, Validaciones.temperatura("25,5").valor)
        assertNotNull(Validaciones.temperatura("").error)
        assertNotNull(Validaciones.temperatura("99").error)
        assertNotNull(Validaciones.temperatura("-30").error)
    }

    @Test
    fun humedadDebeEstarEntreCeroYCien() {
        assertEquals(55.0, Validaciones.humedad("55").valor)
        assertNotNull(Validaciones.humedad("120").error)
        assertNotNull(Validaciones.humedad("abc").error)
    }

    @Test
    fun medicionDebeSerMayorQueCero() {
        assertNotNull(Validaciones.medicion("0").error)
        assertNotNull(Validaciones.medicion("-4").error)
        assertEquals(12.5, Validaciones.medicion("12.5").valor)
    }

    @Test
    fun tagDeEquipo() {
        assertNotNull(Validaciones.tag(""))
        assertNotNull(Validaciones.tag("TR"))
        assertNotNull(Validaciones.tag("TR#1"))
        assertNull(Validaciones.tag("TR-001"))
    }

    @Test
    fun mensajesDeRutYNombre() {
        assertNotNull(Validaciones.rut("17.284.519-4"))
        assertNull(Validaciones.rut("17.284.519-3"))
        assertNotNull(Validaciones.nombre("Al"))
        assertNull(Validaciones.nombre("Ana Pérez"))
    }

    @Test
    fun fechasEstrictasConFormatoDiaMesAnio() {
        assertNotNull(Validaciones.fecha("31-02-2026").error)
        assertNotNull(Validaciones.fecha("").error)
        assertNotNull(Validaciones.fecha("15-03-2026").valor)
    }

    @Test
    fun correoDebeTenerFormatoValido() {
        assertNull(Validaciones.correo("acontreras@induztek.ejemplo"))
        assertNotNull(Validaciones.correo(""))
        assertNotNull(Validaciones.correo("sin-arroba"))
        assertNotNull(Validaciones.correo("a@b"))
    }

    @Test
    fun claveEsObligatoria() {
        assertNull(Validaciones.clave("Induztek2026"))
        assertNotNull(Validaciones.clave(""))
    }
}
