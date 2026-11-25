package com.example.induztekprotocolos

import com.example.induztekprotocolos.data.InduztekRepositorio
import com.example.induztekprotocolos.data.remoto.ErrorApi
import com.example.induztekprotocolos.data.remoto.Red
import com.example.induztekprotocolos.domain.ErrorNegocio
import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.MomentoPrueba
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.TipoEquipo
import com.example.induztekprotocolos.domain.TipoPrueba
import com.example.induztekprotocolos.model.Equipo
import com.example.induztekprotocolos.model.Protocolo
import com.example.induztekprotocolos.model.Usuario
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class RepositorioRemotoTest {

    private lateinit var servidor: MockWebServer
    private lateinit var repo: InduztekRepositorio

    @Before
    fun preparar() {
        servidor = MockWebServer()
        servidor.start()
        val api = Red.crearApi(servidor.url("/").toString())
        repo = InduztekRepositorio { api }
    }

    @After
    fun cerrar() {
        try {
            servidor.shutdown()
        } catch (e: Exception) {
        }
    }

    private fun json(cuerpo: String, codigo: Int = 200) = MockResponse().setResponseCode(codigo).setHeader("Content-Type", "application/json").setBody(cuerpo)

    @Test
    fun listaDeUsuariosSeConvierteAModelosConSuRol() = runBlocking {
        servidor.enqueue(json("""[{"id":1,"rol":"TECNICO","rut":"17.284.519-3","nombre":"Matías Godoy","correo":"m@x.cl","telefono":"","activo":true,"giro":null,"contactoNombre":null,"contactoCargo":null}]"""))
        val usuarios = repo.listarUsuariosActivos()
        assertEquals(1, usuarios.size)
        assertEquals(Rol.TECNICO, usuarios[0].rol)
        assertEquals("Matías Godoy", usuarios[0].nombre)
        assertEquals("/api/usuarios?activos=true", servidor.takeRequest().path)
    }

    @Test
    fun registrarEquipoEnviaElCuerpoYDevuelveElIdCreado() = runBlocking {
        servidor.enqueue(json("""{"id":12,"idInstalacion":1,"tag":"TR-09","tipo":"TRANSFORMADOR","marca":"Rhona","numeroSerie":"S1","tensiones":"","maxContactosUohm":null}""", 201))
        val id = repo.registrarEquipo(1, " TR-09 ", TipoEquipo.TRANSFORMADOR, "Rhona", "S1", null)
        val pedido = servidor.takeRequest()
        assertEquals(12L, id)
        assertEquals("POST", pedido.method)
        assertEquals("/api/equipos", pedido.path)
        assertTrue(pedido.body.readUtf8().contains("\"tag\":\"TR-09\""))
    }

    @Test
    fun errorDelServidorSeConvierteEnErrorDeNegocioConSuMensaje() = runBlocking {
        servidor.enqueue(json("""{"estado":409,"mensaje":"El equipo \"TR-09\" ya existe en esta instalación"}""", 409))
        val error = assertFailsWith<ErrorNegocio> { repo.registrarEquipo(1, "TR-09", TipoEquipo.TRANSFORMADOR, "", "", null) }
        assertEquals(409, error.codigo)
        assertTrue(error.message!!.contains("ya existe"))
    }

    @Test
    fun buscarUnRecursoInexistenteDevuelveNulo() = runBlocking {
        servidor.enqueue(json("""{"estado":404,"mensaje":"El usuario 9 no existe"}""", 404))
        assertNull(repo.buscarUsuario(9))
    }

    @Test
    fun sinConexionSeAvisaConUnMensajeClaro() = runBlocking {
        servidor.shutdown()
        val error = assertFailsWith<ErrorNegocio> { repo.listarInstrumentos() }
        assertEquals(ErrorApi.SIN_CONEXION, error.message)
    }

    @Test
    fun unTagVacioNoLlegaAlServidor() = runBlocking {
        assertFailsWith<ErrorNegocio> { repo.registrarEquipo(1, "  ", TipoEquipo.TRANSFORMADOR, "", "", null) }
        assertEquals(0, servidor.requestCount)
    }

    @Test
    fun unaTransicionNoPermitidaSeRechazaSinLlamarAlServidor() = runBlocking {
        val protocolo = Protocolo(id = 5, numero = "PT-1", idVisita = 1, idEquipo = 1, idUsuarioRegistra = 3, fechaHoraPrueba = 1L, estado = EstadoProtocolo.REGISTRADO_TERRENO)
        val tecnico = Usuario(id = 3, rol = Rol.TECNICO, rut = "17.284.519-3", nombre = "Matías", correo = "m@x.cl")
        assertFailsWith<ErrorNegocio> { repo.cambiarEstado(protocolo, EstadoProtocolo.FORMALIZADO, tecnico) }
        assertEquals(0, servidor.requestCount)
    }

    private val equipoCelda = Equipo(id = 1, idInstalacion = 1, tag = "CEL-MT-03", tipo = TipoEquipo.CELDA_MT, maxContactosUohm = 60.0)
    private val protocoloBorrador = Protocolo(id = 5, numero = "PT-1", idVisita = 1, idEquipo = 1, idUsuarioRegistra = 3, fechaHoraPrueba = 1L)

    private fun instrumento(vencimiento: Long) =
        """{"id":4,"nombre":"Micro-ohmímetro","marca":"Megger","modelo":"DLRO10X","numeroSerie":"S","fechaUltimaCalibracion":1000,"fechaVencimiento":$vencimiento}"""

    @Test
    fun unaPruebaConInstrumentoVencidoSeRechaza() = runBlocking {
        servidor.enqueue(json(instrumento(2000)))
        val error = assertFailsWith<ErrorNegocio> {
            repo.registrarPrueba(protocoloBorrador, equipoCelda, TipoPrueba.CONTACTOS, "Polo A", MomentoPrueba.UNICA, true, null, 4, mapOf("RESISTENCIA_CONTACTO" to 55.0))
        }
        assertTrue(error.message!!.contains("calibración vencida"))
    }

    @Test
    fun laPruebaSeEnviaConElResultadoCalculadoEnElCliente() = runBlocking {
        servidor.enqueue(json(instrumento(4102444800000L)))
        servidor.enqueue(json("""{"id":99,"idProtocolo":5,"tipoPrueba":"CONTACTOS","elemento":"Polo A","momento":"UNICA","realizada":true,"mediciones":[]}""", 201))
        val id = repo.registrarPrueba(protocoloBorrador, equipoCelda, TipoPrueba.CONTACTOS, "Polo A", MomentoPrueba.UNICA, true, null, 4, mapOf("RESISTENCIA_CONTACTO" to 55.0))
        servidor.takeRequest()
        val cuerpo = servidor.takeRequest().body.readUtf8()
        assertEquals(99L, id)
        assertTrue(cuerpo.contains("\"resultado\":\"APROBADO\""))
        assertTrue(cuerpo.contains("RESISTENCIA_CONTACTO"))
    }

    @Test
    fun puedeEditarSegunEstadoYRol() {
        val tecnico = Usuario(id = 3, rol = Rol.TECNICO, rut = "1", nombre = "A", correo = "a@x.cl")
        val otroTecnico = tecnico.copy(id = 8)
        assertTrue(repo.puedeEditar(protocoloBorrador, tecnico))
        assertFalse(repo.puedeEditar(protocoloBorrador, otroTecnico))
        assertFalse(repo.puedeEditar(protocoloBorrador.copy(estado = EstadoProtocolo.FORMALIZADO), tecnico))
    }

    @Test
    fun mensajesDeErrorSegunElCodigoHttp() {
        assertEquals("falló", ErrorApi.mensaje(409, """{"estado":409,"mensaje":"falló"}"""))
        assertEquals("No se encontró el recurso pedido", ErrorApi.mensaje(404, null))
        assertEquals("El servidor tuvo un problema. Inténtalo más tarde", ErrorApi.mensaje(500, "no es json"))
    }

    @Test
    fun direccionesDelServidorSeNormalizanYValidan() {
        assertEquals("http://192.168.1.10:8080/", Red.normalizarUrl("192.168.1.10:8080"))
        assertEquals("https://mi-tunel.devtunnels.ms/", Red.normalizarUrl(" https://mi-tunel.devtunnels.ms "))
        assertTrue(Red.esUrlValida("http://10.0.2.2:8080"))
        assertFalse(Red.esUrlValida(""))
        assertFalse(Red.esUrlValida("http://"))
    }

    @Test
    fun inicioDeSesionEnviaCorreoYClaveYDevuelveElUsuario() = runBlocking {
        servidor.enqueue(json("""{"id":4,"rol":"INGENIERO","rut":"15.906.334-8","nombre":"Andrea Contreras Silva","correo":"acontreras@induztek.ejemplo","telefono":"","activo":true}"""))
        val usuario = repo.iniciarSesion(" acontreras@induztek.ejemplo ", "Induztek2026")
        val pedido = servidor.takeRequest()
        val cuerpo = pedido.body.readUtf8()
        assertEquals("POST", pedido.method)
        assertEquals("/api/usuarios/login", pedido.path)
        assertTrue(cuerpo.contains("\"correo\":\"acontreras@induztek.ejemplo\""))
        assertTrue(cuerpo.contains("\"clave\":\"Induztek2026\""))
        assertEquals(Rol.INGENIERO, usuario.rol)
    }

    @Test
    fun credencialesIncorrectasDanErrorDeNegocio401() = runBlocking {
        servidor.enqueue(json("""{"estado":401,"mensaje":"Correo o contraseña incorrectos"}""", 401))
        val error = assertFailsWith<ErrorNegocio> { repo.iniciarSesion("acontreras@induztek.ejemplo", "mala") }
        assertEquals(401, error.codigo)
        assertEquals("Correo o contraseña incorrectos", error.message)
    }

    @Test
    fun inicioDeSesionSinDatosNoLlamaAlServidor() = runBlocking {
        assertFailsWith<ErrorNegocio> { repo.iniciarSesion("", "Induztek2026") }
        assertFailsWith<ErrorNegocio> { repo.iniciarSesion("acontreras@induztek.ejemplo", "") }
        assertEquals(0, servidor.requestCount)
    }
}
