package com.example.induztekprotocolos.domain

enum class Rol(val etiqueta: String) {
    TECNICO("Técnico de terreno"),
    INGENIERO("Ingeniero de pruebas"),
    JEFE_TECNICO("Jefe técnico"),
    CLIENTE("Usuario cliente")
}

enum class ClasificacionEquipo { PRIMARIO, SECUNDARIO }

enum class TipoEquipo(val etiqueta: String, val clasificacion: ClasificacionEquipo) {
    TRANSFORMADOR("Transformador", ClasificacionEquipo.PRIMARIO),
    INTERRUPTOR("Interruptor", ClasificacionEquipo.PRIMARIO),
    CELDA_MT("Celda de media tensión", ClasificacionEquipo.PRIMARIO),
    CABLE_POTENCIA("Cable de poder", ClasificacionEquipo.PRIMARIO),
    RELE_PROTECCION("Relé de protección", ClasificacionEquipo.SECUNDARIO)
}

enum class TipoPrueba(val etiqueta: String) {
    AISLACION("Resistencia de aislación"),
    TTR("Relación de transformación (TTR)"),
    DEVANADOS("Resistencia de devanados"),
    CONTACTOS("Resistencia de contactos"),
    OPERACION_RELE("Operación de relé")
}

enum class ResultadoPrueba(val etiqueta: String) {
    APROBADO("Aprobado"), OBSERVADO("Observado"), RECHAZADO("Rechazado")
}

enum class MomentoPrueba(val etiqueta: String) {
    UNICA("Única"), PREVIA("Previa al mantenimiento"), POSTERIOR("Posterior al mantenimiento")
}

enum class TipoVisita(val etiqueta: String) {
    SOLO_PRUEBAS("Solo pruebas"), PRUEBAS_Y_MANTENIMIENTO("Pruebas y mantenimiento")
}

enum class EstadoProtocolo(val etiqueta: String) {
    BORRADOR("Borrador"),
    REGISTRADO_TERRENO("Registrado en terreno"),
    OBSERVADO("Observado"),
    FORMALIZADO("Formalizado"),
    ANULADO("Anulado")
}
