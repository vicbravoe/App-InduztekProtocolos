package com.example.induztekprotocolos.data

import com.example.induztekprotocolos.data.remoto.AmbienteDto
import com.example.induztekprotocolos.data.remoto.CambioEstadoDto
import com.example.induztekprotocolos.data.remoto.CierreVisitaDto
import com.example.induztekprotocolos.data.remoto.CredencialesDto
import com.example.induztekprotocolos.data.remoto.ErrorApi
import com.example.induztekprotocolos.data.remoto.EvidenciaDto
import com.example.induztekprotocolos.data.remoto.InduztekApi
import com.example.induztekprotocolos.data.remoto.NuevoProtocoloDto
import com.example.induztekprotocolos.data.remoto.ProtocoloDetalleDto
import com.example.induztekprotocolos.data.remoto.ResultadoPruebaDto
import com.example.induztekprotocolos.domain.CatalogoPruebas
import com.example.induztekprotocolos.domain.ErrorNegocio
import com.example.induztekprotocolos.domain.EstadoProtocolo
import com.example.induztekprotocolos.domain.GeneradorNumeros
import com.example.induztekprotocolos.domain.MomentoPrueba
import com.example.induztekprotocolos.domain.ResultadoCalculator
import com.example.induztekprotocolos.domain.ResultadoCalculo
import com.example.induztekprotocolos.domain.ResultadoPrueba
import com.example.induztekprotocolos.domain.Rol
import com.example.induztekprotocolos.domain.Rut
import com.example.induztekprotocolos.domain.TipoEquipo
import com.example.induztekprotocolos.domain.TipoPrueba
import com.example.induztekprotocolos.domain.TipoVisita
import com.example.induztekprotocolos.domain.TransicionesEstado
import com.example.induztekprotocolos.model.Equipo
import com.example.induztekprotocolos.model.Instalacion
import com.example.induztekprotocolos.model.Instrumento
import com.example.induztekprotocolos.model.Medicion
import com.example.induztekprotocolos.model.Protocolo
import com.example.induztekprotocolos.model.ProtocoloVista
import com.example.induztekprotocolos.model.Prueba
import com.example.induztekprotocolos.domain.Validaciones
import com.example.induztekprotocolos.model.Usuario
import com.example.induztekprotocolos.model.Visita
import com.example.induztekprotocolos.model.VisitaResumen
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException

class InduztekRepositorio(private val proveedorApi: () -> InduztekApi) {

    private val api: InduztekApi get() = proveedorApi()

    private suspend fun <T> llamar(bloque: suspend () -> T): T =
        try {
            bloque()
        } catch (e: HttpException) {
            throw ErrorNegocio(ErrorApi.mensaje(e.code(), e.response()?.errorBody()?.string()), e.code())
        } catch (e: IOException) {
            throw ErrorNegocio(ErrorApi.SIN_CONEXION)
        } catch (e: JsonParseException) {
            throw ErrorNegocio(ErrorApi.RESPUESTA_INVALIDA)
        }

    private suspend fun <T> buscar(bloque: suspend () -> T): T? =
        try {
            llamar(bloque)
        } catch (e: ErrorNegocio) {
            if (e.codigo == 404) null else throw e
        }

    suspend fun iniciarSesion(correo: String, clave: String): Usuario {
        Validaciones.correo(correo)?.let { throw ErrorNegocio(it) }
        Validaciones.clave(clave)?.let { throw ErrorNegocio(it) }
        return llamar { api.login(CredencialesDto(correo.trim(), clave)) }
    }

    suspend fun listarUsuariosActivos(): List<Usuario> = llamar { api.usuarios(null, true) }

    suspend fun listarPersonalPorRol(rol: Rol): List<Usuario> = llamar { api.usuarios(rol.name, true) }

    suspend fun buscarUsuario(id: Long): Usuario? = buscar { api.usuario(id) }

    suspend fun listarInstalaciones(): List<Instalacion> = llamar { api.instalaciones(null) }

    suspend fun listarInstalacionesDeCliente(idCliente: Long): List<Instalacion> = llamar { api.instalaciones(idCliente) }

    suspend fun buscarInstalacion(id: Long): Instalacion? = buscar { api.instalacion(id) }

    suspend fun listarEquiposDeInstalacion(idInstalacion: Long): List<Equipo> = llamar { api.equipos(idInstalacion) }

    suspend fun buscarEquipo(id: Long): Equipo? = buscar { api.equipo(id) }

    suspend fun listarInstrumentos(): List<Instrumento> = llamar { api.instrumentos() }

    suspend fun buscarInstrumento(id: Long): Instrumento? = buscar { api.instrumento(id) }

    suspend fun registrarEquipo(idInstalacion: Long, tag: String, tipo: TipoEquipo, marca: String, serie: String, maxContactos: Double?): Long {
        if (tag.isBlank()) throw ErrorNegocio("El TAG del equipo es obligatorio")
        val nuevo = Equipo(idInstalacion = idInstalacion, tag = tag.trim(), tipo = tipo, marca = marca.trim(), numeroSerie = serie.trim(), maxContactosUohm = maxContactos)
        return llamar { api.crearEquipo(nuevo).id }
    }

    suspend fun registrarInstrumento(nombre: String, marca: String, modelo: String, serie: String, calibracion: Long, vencimiento: Long): Long {
        if (nombre.isBlank()) throw ErrorNegocio("El nombre es obligatorio")
        if (vencimiento <= calibracion) throw ErrorNegocio("El vencimiento debe ser posterior a la calibración")
        val nuevo = Instrumento(nombre = nombre.trim(), marca = marca.trim(), modelo = modelo.trim(), numeroSerie = serie.trim(), fechaUltimaCalibracion = calibracion, fechaVencimiento = vencimiento)
        return llamar { api.crearInstrumento(nuevo).id }
    }

    suspend fun calibracionVigente(idInstrumento: Long, fecha: Long = System.currentTimeMillis()): Boolean =
        (buscarInstrumento(idInstrumento)?.fechaVencimiento ?: 0L) >= fecha

    suspend fun listarVisitas(): List<VisitaResumen> {
        val nombres = listarInstalaciones().associate { it.id to it.nombre }
        return llamar { api.visitas() }.map {
            VisitaResumen(it.visita, nombres[it.visita.idInstalacion] ?: "—", it.totalProtocolos, it.formalizados)
        }
    }

    suspend fun buscarVisita(id: Long): Visita? = buscar { api.visita(id) }

    suspend fun iniciarVisita(idInstalacion: Long, tipo: TipoVisita, idIngeniero: Long, participantes: List<Long>): Long {
        val nueva = Visita(
            numeroOt = GeneradorNumeros.numeroOt(),
            idInstalacion = idInstalacion,
            fecha = System.currentTimeMillis(),
            tipoVisita = tipo,
            idIngenieroCargo = idIngeniero,
            participantes = participantes
        )
        return llamar { api.crearVisita(nueva).id }
    }

    suspend fun cerrarVisita(idVisita: Long, receptorNombre: String, receptorRut: String, receptorCargo: String) {
        val visita = buscarVisita(idVisita) ?: throw ErrorNegocio("La visita no existe")
        if (visita.cerrada) throw ErrorNegocio("La visita ya está cerrada")
        val pendientes = listarProtocolosDeVisita(idVisita).filterNot { it.estado == EstadoProtocolo.REGISTRADO_TERRENO || it.estado == EstadoProtocolo.ANULADO }
        if (pendientes.isNotEmpty()) throw ErrorNegocio("Faltan ${pendientes.size} protocolo(s) por dejar en \"Registrado en terreno\"")
        if (receptorNombre.isBlank() || receptorCargo.isBlank()) throw ErrorNegocio("Nombre y cargo de quien recibe son obligatorios")
        if (!Rut.esValido(receptorRut)) throw ErrorNegocio("El RUT de quien recibe no es válido")
        llamar { api.cerrarVisita(idVisita, CierreVisitaDto(receptorNombre.trim(), Rut.formatear(receptorRut), receptorCargo.trim())) }
    }

    suspend fun listarProtocolosDeVisita(idVisita: Long): List<Protocolo> = llamar { api.protocolos(idVisita, null, null) }

    suspend fun listarProtocolosPorEstado(estado: EstadoProtocolo): List<Protocolo> = llamar { api.protocolos(null, null, estado.name) }

    suspend fun buscarProtocolo(id: Long): Protocolo? = buscar { api.protocolo(id) }

    suspend fun detalleDeProtocolo(id: Long): ProtocoloDetalleDto = llamar { api.detalleProtocolo(id) }

    suspend fun iniciarProtocolo(idVisita: Long, idEquipo: Long, idUsuario: Long): Long {
        llamar { api.protocolos(idVisita, idEquipo, null) }.firstOrNull()?.let { return it.id }
        val visita = buscarVisita(idVisita) ?: throw ErrorNegocio("La visita no existe")
        if (visita.cerrada) throw ErrorNegocio("La visita está cerrada")
        val nuevo = NuevoProtocoloDto(GeneradorNumeros.numeroProtocolo(), idVisita, idEquipo, idUsuario, System.currentTimeMillis())
        return llamar { api.iniciarProtocolo(nuevo).id }
    }

    fun puedeEditar(protocolo: Protocolo, usuario: Usuario): Boolean =
        TransicionesEstado.editable(protocolo.estado) && (usuario.rol != Rol.TECNICO || usuario.id == protocolo.idUsuarioRegistra) && usuario.rol != Rol.CLIENTE

    suspend fun guardarAmbiente(p: Protocolo, temperatura: Double, humedad: Double, descripcionMantenimiento: String?) {
        llamar { api.guardarAmbiente(p.id, AmbienteDto(temperatura, humedad, descripcionMantenimiento?.ifBlank { null })) }
    }

    suspend fun guardarEvidencia(p: Protocolo, foto: ByteArray?, latitud: Double?, longitud: Double?) {
        val cuerpo = EvidenciaDto(foto?.let { ConvertidorImagen.aBase64(it) }, latitud, longitud)
        llamar { api.guardarEvidencia(p.id, cuerpo) }
    }

    suspend fun obtenerFoto(idProtocolo: Long): ByteArray? =
        buscar { api.foto(idProtocolo) }?.let { ConvertidorImagen.desdeBase64(it.fotoBase64) }

    suspend fun registrarPrueba(
        protocolo: Protocolo, equipo: Equipo, tipoPrueba: TipoPrueba, elemento: String, momento: MomentoPrueba,
        realizada: Boolean, motivoNoRealizada: String?, idInstrumento: Long?, valores: Map<String, Double>
    ): Long {
        if (elemento.isBlank()) throw ErrorNegocio("Indica el elemento probado (devanado, fase o polo)")
        if (!realizada && motivoNoRealizada.isNullOrBlank()) throw ErrorNegocio("Si la prueba no se realizó, el motivo es obligatorio")
        if (realizada) {
            if (idInstrumento == null) throw ErrorNegocio("Selecciona el instrumento utilizado")
            if (!calibracionVigente(idInstrumento)) throw ErrorNegocio("El instrumento seleccionado tiene la calibración vencida")
        }
        val calculo = if (realizada) ResultadoCalculator.calcular(tipoPrueba, equipo.tipo, valores, equipo.maxContactosUohm) else ResultadoCalculo(null)
        val definiciones = CatalogoPruebas.parametrosDe(tipoPrueba, equipo.tipo).associateBy { it.codigo }
        val mediciones = if (realizada) valores.mapNotNull { (codigo, valor) -> definiciones[codigo]?.let { Medicion(idPrueba = 0, parametro = codigo, valor = valor, unidad = it.unidad) } } else emptyList()
        val prueba = Prueba(
            idProtocolo = protocolo.id, tipoPrueba = tipoPrueba, elemento = elemento.trim(), momento = momento, realizada = realizada,
            motivoNoRealizada = motivoNoRealizada?.trim(), idInstrumento = if (realizada) idInstrumento else null,
            resultado = calculo.resultado, valorDerivado = calculo.valorDerivado, etiquetaDerivada = calculo.etiquetaDerivada,
            mediciones = mediciones
        )
        return llamar { api.registrarPrueba(protocolo.id, prueba).id }
    }

    suspend fun fijarResultadoManual(idPrueba: Long, resultado: ResultadoPrueba) {
        llamar { api.fijarResultado(idPrueba, ResultadoPruebaDto(resultado)) }
    }

    suspend fun cambiarEstado(protocolo: Protocolo, nuevo: EstadoProtocolo, usuario: Usuario, motivo: String? = null) {
        if (!TransicionesEstado.permitida(protocolo.estado, nuevo, usuario.rol))
            throw ErrorNegocio("No puedes mover este protocolo de \"${protocolo.estado.etiqueta}\" a \"${nuevo.etiqueta}\"")
        if (TransicionesEstado.requiereMotivo(nuevo) && motivo.isNullOrBlank()) throw ErrorNegocio("El motivo es obligatorio para pasar a \"${nuevo.etiqueta}\"")
        if (nuevo == EstadoProtocolo.REGISTRADO_TERRENO) {
            if (protocolo.temperaturaC == null || protocolo.humedadPct == null) throw ErrorNegocio("Temperatura y humedad son obligatorias antes de registrar en terreno")
            if (detalleDeProtocolo(protocolo.id).pruebas.isEmpty()) throw ErrorNegocio("Registra al menos una prueba antes de continuar")
        }
        if (nuevo == EstadoProtocolo.FORMALIZADO) {
            val sinResultado = detalleDeProtocolo(protocolo.id).pruebas.filter { it.realizada && it.resultado == null }
            if (sinResultado.isNotEmpty()) throw ErrorNegocio("Define el resultado de ${sinResultado.size} prueba(s) manual(es) (devanados) antes de formalizar")
        }
        llamar { api.cambiarEstado(protocolo.id, CambioEstadoDto(nuevo, usuario.id, motivo?.trim())) }
    }

    private suspend fun nombresDeInstrumentos(): Map<Long, String> = listarInstrumentos().associate { it.id to it.nombre }

    suspend fun armarVista(protocolo: Protocolo): ProtocoloVista? {
        val equipo = buscarEquipo(protocolo.idEquipo) ?: return null
        val pruebas = detalleDeProtocolo(protocolo.id).pruebas
        val historial = llamar { api.detalleDeEquipo(protocolo.idEquipo) }
        val foto = if (protocolo.tieneFoto) obtenerFoto(protocolo.id) else null
        return ArmadoVistas.construir(protocolo.copy(foto = foto), equipo, pruebas, historial, nombresDeInstrumentos())
    }

    suspend fun armarVistasDeEquipo(idEquipo: Long, soloFormalizados: Boolean): List<ProtocoloVista> {
        val equipo = buscarEquipo(idEquipo) ?: return emptyList()
        val historial = llamar { api.detalleDeEquipo(idEquipo) }
        val nombres = nombresDeInstrumentos()
        return historial
            .filter { !soloFormalizados || it.protocolo.estado == EstadoProtocolo.FORMALIZADO }
            .sortedByDescending { it.protocolo.fechaHoraPrueba }
            .map { ArmadoVistas.construir(it.protocolo, equipo, it.pruebas, historial, nombres) }
    }

    suspend fun armarVistasPorRevisar(): List<ProtocoloVista> {
        val protocolos = listarProtocolosPorEstado(EstadoProtocolo.REGISTRADO_TERRENO)
        if (protocolos.isEmpty()) return emptyList()
        val nombres = nombresDeInstrumentos()
        val vistas = mutableListOf<ProtocoloVista>()
        for ((idEquipo, lista) in protocolos.groupBy { it.idEquipo }) {
            val equipo = buscarEquipo(idEquipo) ?: continue
            val historial = llamar { api.detalleDeEquipo(idEquipo) }
            for (protocolo in lista) {
                val detalle = historial.firstOrNull { it.protocolo.id == protocolo.id } ?: continue
                vistas.add(ArmadoVistas.construir(protocolo, equipo, detalle.pruebas, historial, nombres))
            }
        }
        return vistas.sortedBy { it.protocolo.fechaHoraPrueba }
    }
}
