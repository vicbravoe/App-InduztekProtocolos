package com.example.induztekprotocolos

import com.example.induztekprotocolos.data.ClimaRepositorio
import com.example.induztekprotocolos.data.remoto.ErrorApi
import com.example.induztekprotocolos.data.remoto.RedClima
import com.example.induztekprotocolos.domain.ErrorNegocio
import com.example.induztekprotocolos.model.ClimaActual
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class ClimaRepositorioTest {

    private lateinit var servidor: MockWebServer
    private lateinit var repo: ClimaRepositorio

    @Before
    fun preparar() {
        servidor = MockWebServer()
        servidor.start()
        repo = ClimaRepositorio(RedClima.crear(servidor.url("/").toString()))
    }

    @After
    fun cerrar() {
        try {
            servidor.shutdown()
        } catch (e: Exception) {
        }
    }

    private fun json(cuerpo: String, codigo: Int = 200) =
        MockResponse().setResponseCode(codigo).setHeader("Content-Type", "application/json").setBody(cuerpo)

    @Test
    fun laRespuestaDelClimaSeConvierteATemperaturaYHumedad() = runBlocking {
        servidor.enqueue(json("""{"current":{"time":"2026-11-24T21:00","interval":900,"temperature_2m":21.4,"relative_humidity_2m":48}}"""))
        val clima = repo.obtener(-33.45, -70.67)
        assertEquals(21.4, clima.temperaturaC, 0.001)
        assertEquals(48.0, clima.humedadPct, 0.001)
    }

    @Test
    fun laConsultaEnviaCoordenadasYVariablesPedidas() = runBlocking {
        servidor.enqueue(json("""{"current":{"temperature_2m":10.0,"relative_humidity_2m":70}}"""))
        repo.obtener(-33.45, -70.67)
        val pedido = servidor.takeRequest()
        val ruta = pedido.path ?: ""
        assertEquals("GET", pedido.method)
        assertTrue(ruta.startsWith("/v1/forecast?"))
        assertTrue(ruta.contains("latitude=-33.45"))
        assertTrue(ruta.contains("longitude=-70.67"))
        assertTrue(ruta.contains("current=temperature_2m"))
        assertTrue(ruta.contains("relative_humidity_2m"))
    }

    @Test
    fun sinBloqueCurrentSeAvisaQueNoHayDatos() {
        servidor.enqueue(json("""{"latitude":-33.45}"""))
        val error = assertFailsWith<ErrorNegocio> { runBlocking { repo.obtener(-33.45, -70.67) } }
        assertEquals(ClimaRepositorio.MENSAJE_SIN_DATOS, error.message)
    }

    @Test
    fun siFaltaLaHumedadSeAvisaQueNoHayDatos() {
        servidor.enqueue(json("""{"current":{"temperature_2m":18.0}}"""))
        val error = assertFailsWith<ErrorNegocio> { runBlocking { repo.obtener(0.0, 0.0) } }
        assertEquals(ClimaRepositorio.MENSAJE_SIN_DATOS, error.message)
    }

    @Test
    fun unErrorDelServicioSeTraduceAUnMensajeConSuCodigo() {
        servidor.enqueue(json("""{"error":true,"reason":"falla"}""", 500))
        val error = assertFailsWith<ErrorNegocio> { runBlocking { repo.obtener(-33.45, -70.67) } }
        assertEquals(ClimaRepositorio.MENSAJE_SERVICIO, error.message)
        assertEquals(500, error.codigo)
    }

    @Test
    fun unaRespuestaConFormatoInesperadoSeAvisaComoInvalida() {
        servidor.enqueue(json("[]"))
        val error = assertFailsWith<ErrorNegocio> { runBlocking { repo.obtener(-33.45, -70.67) } }
        assertEquals(ErrorApi.RESPUESTA_INVALIDA, error.message)
    }

    @Test
    fun sinConexionSeAvisaQueRevisenInternet() {
        servidor.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        val error = assertFailsWith<ErrorNegocio> { runBlocking { repo.obtener(-33.45, -70.67) } }
        assertEquals(ClimaRepositorio.MENSAJE_SIN_CONEXION, error.message)
    }

    @Test
    fun elClimaSeFormateaParaLosCamposDelProtocolo() {
        val clima = ClimaActual(21.449, 47.6)
        assertEquals("21.4", clima.temperaturaTexto())
        assertEquals("48", clima.humedadTexto())
        assertTrue(clima.resumen().contains("Open-Meteo"))
    }
}
