package com.example.induztekprotocolos.domain

data class DefinicionParametro(val codigo: String, val etiqueta: String, val unidad: String)

object CatalogoPruebas {

    fun pruebasDe(tipoEquipo: TipoEquipo): List<TipoPrueba> = when (tipoEquipo) {
        TipoEquipo.TRANSFORMADOR -> listOf(TipoPrueba.AISLACION, TipoPrueba.TTR, TipoPrueba.DEVANADOS)
        TipoEquipo.INTERRUPTOR -> listOf(TipoPrueba.AISLACION, TipoPrueba.CONTACTOS)
        TipoEquipo.CELDA_MT -> listOf(TipoPrueba.AISLACION, TipoPrueba.CONTACTOS)
        TipoEquipo.CABLE_POTENCIA -> listOf(TipoPrueba.AISLACION)
        TipoEquipo.RELE_PROTECCION -> listOf(TipoPrueba.OPERACION_RELE)
    }

    fun aislacionConIp(tipoEquipo: TipoEquipo) =
        tipoEquipo == TipoEquipo.TRANSFORMADOR || tipoEquipo == TipoEquipo.CABLE_POTENCIA

    fun parametrosDe(tipoPrueba: TipoPrueba, tipoEquipo: TipoEquipo): List<DefinicionParametro> = when (tipoPrueba) {
        TipoPrueba.AISLACION -> buildList {
            add(DefinicionParametro("TENSION_PRUEBA", "Tensión de prueba", "kV"))
            add(DefinicionParametro("LECTURA_1MIN", "Lectura a 1 min", "MΩ"))
            if (aislacionConIp(tipoEquipo)) add(DefinicionParametro("LECTURA_10MIN", "Lectura a 10 min", "MΩ"))
        }
        TipoPrueba.TTR -> listOf(
            DefinicionParametro("RELACION_TEORICA", "Relación teórica", "—"),
            DefinicionParametro("RELACION_MEDIDA", "Relación medida", "—")
        )
        TipoPrueba.DEVANADOS -> listOf(DefinicionParametro("RESISTENCIA", "Resistencia", "mΩ"))
        TipoPrueba.CONTACTOS -> listOf(DefinicionParametro("RESISTENCIA_CONTACTO", "Resistencia de contacto", "µΩ"))
        TipoPrueba.OPERACION_RELE -> listOf(
            DefinicionParametro("CORRIENTE_AJUSTE", "Corriente de ajuste", "A"),
            DefinicionParametro("CORRIENTE_OPERACION", "Corriente de operación", "A"),
            DefinicionParametro("TIEMPO_ESPERADO", "Tiempo esperado", "ms"),
            DefinicionParametro("TIEMPO_MEDIDO", "Tiempo medido", "ms")
        )
    }

    fun elementosSugeridos(tipoPrueba: TipoPrueba, tipoEquipo: TipoEquipo): List<String> = when (tipoPrueba) {
        TipoPrueba.AISLACION -> when (tipoEquipo) {
            TipoEquipo.TRANSFORMADOR -> listOf("AT-BT", "AT-T", "BT-T")
            TipoEquipo.CABLE_POTENCIA -> listOf("Fase A", "Fase B", "Fase C")
            else -> listOf("Único")
        }
        TipoPrueba.TTR -> listOf("H1-X1", "H2-X2", "H3-X3")
        TipoPrueba.DEVANADOS -> listOf("AT", "BT")
        TipoPrueba.CONTACTOS -> listOf("Polo 1", "Polo 2", "Polo 3")
        TipoPrueba.OPERACION_RELE -> listOf("Único")
    }

    fun esManual(tipoPrueba: TipoPrueba) = tipoPrueba == TipoPrueba.DEVANADOS
}
