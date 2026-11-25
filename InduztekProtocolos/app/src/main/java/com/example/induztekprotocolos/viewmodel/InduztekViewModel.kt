package com.example.induztekprotocolos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztekprotocolos.data.ConfigServidor
import com.example.induztekprotocolos.data.InduztekRepositorio
import com.example.induztekprotocolos.data.Servicios
import com.example.induztekprotocolos.data.SesionPreferencias
import com.example.induztekprotocolos.data.remoto.Red
import com.example.induztekprotocolos.domain.ErrorNegocio
import com.example.induztekprotocolos.domain.Permiso
import com.example.induztekprotocolos.domain.PermisosRol
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.TipoEquipo
import com.example.induztekprotocolos.domain.TipoVisita
import com.example.induztekprotocolos.model.DetalleVisita
import com.example.induztekprotocolos.model.Equipo
import com.example.induztekprotocolos.model.EquipoEnVisita
import com.example.induztekprotocolos.model.Instalacion
import com.example.induztekprotocolos.model.Instrumento
import com.example.induztekprotocolos.model.ProtocoloVista
import com.example.induztekprotocolos.model.Res
import com.example.induztekprotocolos.model.Usuario
import com.example.induztekprotocolos.model.VisitaResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InduztekViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = InduztekRepositorio(Servicios.proveedorApi(application))
    private val sesion = SesionPreferencias(application)
    private val config = ConfigServidor(application)

    private var pendientes = 0

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    private val _urlServidor = MutableStateFlow(config.leer())
    val urlServidor: StateFlow<String> = _urlServidor.asStateFlow()

    private val _sesionLista = MutableStateFlow(false)
    val sesionLista: StateFlow<Boolean> = _sesionLista.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _visitas = MutableStateFlow<List<VisitaResumen>>(emptyList())
    val visitas: StateFlow<List<VisitaResumen>> = _visitas.asStateFlow()

    private val _instrumentos = MutableStateFlow<List<Instrumento>>(emptyList())
    val instrumentos: StateFlow<List<Instrumento>> = _instrumentos.asStateFlow()

    private val _porRevisar = MutableStateFlow<List<ProtocoloVista>>(emptyList())
    val porRevisar: StateFlow<List<ProtocoloVista>> = _porRevisar.asStateFlow()

    private val _detalle = MutableStateFlow<DetalleVisita?>(null)
    val detalle: StateFlow<DetalleVisita?> = _detalle.asStateFlow()
    private var idDetalle = 0L

    private val _instalaciones = MutableStateFlow<List<Instalacion>>(emptyList())
    val instalaciones: StateFlow<List<Instalacion>> = _instalaciones.asStateFlow()

    private val _ingenieros = MutableStateFlow<List<Usuario>>(emptyList())
    val ingenieros: StateFlow<List<Usuario>> = _ingenieros.asStateFlow()

    private val _tecnicos = MutableStateFlow<List<Usuario>>(emptyList())
    val tecnicos: StateFlow<List<Usuario>> = _tecnicos.asStateFlow()

    private val _equiposPorInstalacion = MutableStateFlow<Map<Long, List<Equipo>>>(emptyMap())
    val equiposPorInstalacion: StateFlow<Map<Long, List<Equipo>>> = _equiposPorInstalacion.asStateFlow()

    private val _historial = MutableStateFlow<List<ProtocoloVista>>(emptyList())
    val historial: StateFlow<List<ProtocoloVista>> = _historial.asStateFlow()

    private val _nombreEquipo = MutableStateFlow("Equipo")
    val nombreEquipo: StateFlow<String> = _nombreEquipo.asStateFlow()

    init {
        viewModelScope.launch {
            trabajar {
                restaurarSesion()
            }
            _sesionLista.value = true
        }
    }

    private suspend fun trabajar(bloque: suspend () -> Unit) {
        pendientes++
        _cargando.value = true
        try {
            bloque()
        } catch (e: ErrorNegocio) {
            _aviso.value = e.message
        } finally {
            pendientes--
            _cargando.value = pendientes > 0
        }
    }

    private fun lanzar(bloque: suspend () -> Unit) {
        viewModelScope.launch { trabajar(bloque) }
    }

    private suspend fun <T> intentar(bloque: suspend () -> T): Res<T> {
        pendientes++
        _cargando.value = true
        return try {
            Res(bloque())
        } catch (e: ErrorNegocio) {
            Res(error = e.message)
        } finally {
            pendientes--
            _cargando.value = pendientes > 0
        }
    }

    fun avisoMostrado() {
        _aviso.value = null
    }

    private suspend fun restaurarSesion() {
        val id = sesion.leer()
        if (id > 0) _usuarioActual.value = repo.buscarUsuario(id)
        if (_usuarioActual.value != null) cargarListas()
    }

    fun guardarUrlServidor(texto: String): String? {
        if (!Red.esUrlValida(texto)) return "La dirección no es válida (ej: http://192.168.1.10:8080)"
        val url = Red.normalizarUrl(texto)
        config.guardar(url)
        _urlServidor.value = url
        return null
    }

    fun iniciarSesion(correo: String, clave: String, alTerminar: (Res<Usuario>) -> Unit) {
        viewModelScope.launch {
            val r = intentar { repo.iniciarSesion(correo, clave) }
            val usuario = r.valor
            if (usuario != null) {
                _usuarioActual.value = usuario
                sesion.guardar(usuario.id)
                trabajar { cargarListas() }
            }
            alTerminar(r)
        }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
    }

    fun puede(permiso: Permiso): Boolean = _usuarioActual.value?.let { PermisosRol.tiene(it.rol, permiso) } == true

    private suspend fun cargarListas() {
        _visitas.value = repo.listarVisitas()
        _instrumentos.value = repo.listarInstrumentos()
        _porRevisar.value = repo.armarVistasPorRevisar()
        if (idDetalle != 0L) construirDetalle(idDetalle)
    }

    fun recargar() {
        lanzar { cargarListas() }
    }

    fun fechaVencimientoOk(i: Instrumento): Boolean = i.fechaVencimiento >= System.currentTimeMillis()

    fun cargarCatalogosDeVisita() {
        lanzar {
            _instalaciones.value = repo.listarInstalaciones()
            _ingenieros.value = repo.listarPersonalPorRol(Rol.INGENIERO)
            _tecnicos.value = repo.listarPersonalPorRol(Rol.TECNICO)
        }
    }

    fun cargarEquiposDelCliente() {
        val usuario = _usuarioActual.value ?: return
        lanzar {
            val lista = repo.listarInstalacionesDeCliente(usuario.id)
            _instalaciones.value = lista
            _equiposPorInstalacion.value = lista.associate { it.id to repo.listarEquiposDeInstalacion(it.id) }
        }
    }

    fun crearVisita(idInstalacion: Long, tipo: TipoVisita, idIngeniero: Long, participantes: List<Long>, alTerminar: (Res<Long>) -> Unit) {
        viewModelScope.launch {
            val r = intentar { repo.iniciarVisita(idInstalacion, tipo, idIngeniero, participantes) }
            alTerminar(r)
            if (r.ok) trabajar { cargarListas() }
        }
    }

    fun cargarDetalle(idVisita: Long) {
        idDetalle = idVisita
        lanzar { construirDetalle(idVisita) }
    }

    private suspend fun construirDetalle(idVisita: Long) {
        val visita = repo.buscarVisita(idVisita) ?: return
        val instalacion = repo.buscarInstalacion(visita.idInstalacion) ?: return
        val equipos = repo.listarEquiposDeInstalacion(instalacion.id)
        val protocolos = repo.listarProtocolosDeVisita(idVisita).associateBy { it.idEquipo }
        val ingeniero = repo.buscarUsuario(visita.idIngenieroCargo)?.nombre ?: "—"
        _detalle.value = DetalleVisita(
            visita = visita,
            instalacion = instalacion,
            equipos = equipos.map { EquipoEnVisita(it, protocolos[it.id]) },
            ingeniero = ingeniero
        )
    }

    fun cerrarVisita(idVisita: Long, nombre: String, rut: String, cargo: String, alTerminar: (Res<Unit>) -> Unit) {
        viewModelScope.launch {
            val r = intentar { repo.cerrarVisita(idVisita, nombre, rut, cargo) }
            alTerminar(r)
            if (r.ok) trabajar { cargarListas() }
        }
    }

    fun registrarEquipo(
        idInstalacion: Long, tag: String, tipo: TipoEquipo, marca: String, serie: String, maxContactos: Double?,
        alTerminar: (Res<Long>) -> Unit
    ) {
        viewModelScope.launch {
            val r = intentar { repo.registrarEquipo(idInstalacion, tag, tipo, marca, serie, maxContactos) }
            alTerminar(r)
            if (r.ok) trabajar { cargarListas() }
        }
    }

    fun registrarInstrumento(
        nombre: String, marca: String, modelo: String, serie: String, calibracion: Long, vencimiento: Long,
        alTerminar: (Res<Long>) -> Unit
    ) {
        viewModelScope.launch {
            val r = intentar { repo.registrarInstrumento(nombre, marca, modelo, serie, calibracion, vencimiento) }
            alTerminar(r)
            if (r.ok) trabajar { cargarListas() }
        }
    }

    fun abrirProtocolo(idVisita: Long, idEquipo: Long, alTerminar: (Res<Long>) -> Unit) {
        val usuario = _usuarioActual.value
        if (usuario == null) {
            alTerminar(Res(error = "Inicia sesión primero"))
            return
        }
        viewModelScope.launch {
            val r = intentar { repo.iniciarProtocolo(idVisita, idEquipo, usuario.id) }
            alTerminar(r)
            if (r.ok) trabajar { cargarListas() }
        }
    }

    fun cargarHistorial(idEquipo: Long) {
        val soloFormalizados = _usuarioActual.value?.rol == Rol.CLIENTE
        _historial.value = emptyList()
        lanzar {
            val equipo = repo.buscarEquipo(idEquipo)
            _nombreEquipo.value = equipo?.let { "${it.tag} · ${it.tipo.etiqueta}" } ?: "Equipo"
            _historial.value = repo.armarVistasDeEquipo(idEquipo, soloFormalizados)
        }
    }
}
