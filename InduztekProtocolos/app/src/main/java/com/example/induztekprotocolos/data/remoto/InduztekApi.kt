package com.example.induztekprotocolos.data.remoto

import com.example.induztekprotocolos.model.Equipo
import com.example.induztekprotocolos.model.Instalacion
import com.example.induztekprotocolos.model.Instrumento
import com.example.induztekprotocolos.model.Protocolo
import com.example.induztekprotocolos.model.Prueba
import com.example.induztekprotocolos.model.Usuario
import com.example.induztekprotocolos.model.Visita
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface InduztekApi {

    @GET("api/usuarios")
    suspend fun usuarios(@Query("rol") rol: String?, @Query("activos") activos: Boolean?): List<Usuario>

    @POST("api/usuarios/login")
    suspend fun login(@Body credenciales: CredencialesDto): Usuario

    @GET("api/usuarios/{id}")
    suspend fun usuario(@Path("id") id: Long): Usuario

    @GET("api/instalaciones")
    suspend fun instalaciones(@Query("idCliente") idCliente: Long?): List<Instalacion>

    @GET("api/instalaciones/{id}")
    suspend fun instalacion(@Path("id") id: Long): Instalacion

    @GET("api/equipos")
    suspend fun equipos(@Query("idInstalacion") idInstalacion: Long?): List<Equipo>

    @GET("api/equipos/{id}")
    suspend fun equipo(@Path("id") id: Long): Equipo

    @POST("api/equipos")
    suspend fun crearEquipo(@Body equipo: Equipo): Equipo

    @GET("api/instrumentos")
    suspend fun instrumentos(): List<Instrumento>

    @GET("api/instrumentos/{id}")
    suspend fun instrumento(@Path("id") id: Long): Instrumento

    @POST("api/instrumentos")
    suspend fun crearInstrumento(@Body instrumento: Instrumento): Instrumento

    @GET("api/visitas")
    suspend fun visitas(): List<VisitaResumenDto>

    @GET("api/visitas/{id}")
    suspend fun visita(@Path("id") id: Long): Visita

    @POST("api/visitas")
    suspend fun crearVisita(@Body visita: Visita): Visita

    @PUT("api/visitas/{id}/cierre")
    suspend fun cerrarVisita(@Path("id") id: Long, @Body cierre: CierreVisitaDto): Visita

    @GET("api/protocolos")
    suspend fun protocolos(
        @Query("idVisita") idVisita: Long?,
        @Query("idEquipo") idEquipo: Long?,
        @Query("estado") estado: String?
    ): List<Protocolo>

    @GET("api/protocolos/{id}")
    suspend fun protocolo(@Path("id") id: Long): Protocolo

    @GET("api/protocolos/{id}/detalle")
    suspend fun detalleProtocolo(@Path("id") id: Long): ProtocoloDetalleDto

    @GET("api/protocolos/equipo/{idEquipo}/detalle")
    suspend fun detalleDeEquipo(@Path("idEquipo") idEquipo: Long): List<ProtocoloDetalleDto>

    @POST("api/protocolos")
    suspend fun iniciarProtocolo(@Body cuerpo: NuevoProtocoloDto): Protocolo

    @PUT("api/protocolos/{id}/ambiente")
    suspend fun guardarAmbiente(@Path("id") id: Long, @Body cuerpo: AmbienteDto): Protocolo

    @PUT("api/protocolos/{id}/evidencia")
    suspend fun guardarEvidencia(@Path("id") id: Long, @Body cuerpo: EvidenciaDto): Protocolo

    @GET("api/protocolos/{id}/foto")
    suspend fun foto(@Path("id") id: Long): FotoDto

    @PUT("api/protocolos/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Body cuerpo: CambioEstadoDto): Protocolo

    @POST("api/protocolos/{id}/pruebas")
    suspend fun registrarPrueba(@Path("id") idProtocolo: Long, @Body prueba: Prueba): Prueba

    @PUT("api/pruebas/{id}/resultado")
    suspend fun fijarResultado(@Path("id") id: Long, @Body cuerpo: ResultadoPruebaDto): Prueba
}
