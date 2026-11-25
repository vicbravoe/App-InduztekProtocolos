package com.example.induztekprotocolos.domain

data class Parseo(val valor: Double?, val error: String?)

data class ParseoFecha(val valor: Long?, val error: String?)

object Validaciones {

    fun numero(texto: String, minimo: Double? = null, maximo: Double? = null, exclusivoMinimo: Boolean = false): Parseo {
        val limpio = texto.trim().replace(',', '.')
        if (limpio.isEmpty()) return Parseo(null, "Campo obligatorio")
        val n = limpio.toDoubleOrNull() ?: return Parseo(null, "Escribe un número (ej: 12.5)")
        if (minimo != null && (if (exclusivoMinimo) n <= minimo else n < minimo))
            return Parseo(null, if (exclusivoMinimo) "Debe ser mayor que ${formato(minimo)}" else "Mínimo ${formato(minimo)}")
        if (maximo != null && n > maximo) return Parseo(null, "Máximo ${formato(maximo)}")
        return Parseo(n, null)
    }

    fun temperatura(texto: String): Parseo = numero(texto, -20.0, 60.0)
    fun humedad(texto: String): Parseo = numero(texto, 0.0, 100.0)
    fun medicion(texto: String): Parseo = numero(texto, 0.0, null, exclusivoMinimo = true)

    fun fecha(texto: String): ParseoFecha {
        if (texto.isBlank()) return ParseoFecha(null, "Campo obligatorio")
        val formato = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.forLanguageTag("es-CL"))
        formato.isLenient = false
        return try {
            ParseoFecha(formato.parse(texto.trim())?.time, null)
        } catch (e: java.text.ParseException) {
            ParseoFecha(null, "Usa el formato dd-mm-aaaa")
        }
    }

    fun requerido(texto: String, nombre: String): String? =
        if (texto.isBlank()) "$nombre es obligatorio" else null

    fun tag(texto: String): String? = when {
        texto.isBlank() -> "El TAG es obligatorio"
        texto.trim().length < 3 -> "El TAG debe tener al menos 3 caracteres"
        !texto.trim().all { it.isLetterOrDigit() || it == '-' || it == '_' || it == '/' } -> "Solo letras, números y guiones"
        else -> null
    }

    fun rut(texto: String): String? = when {
        texto.isBlank() -> "El RUT es obligatorio"
        !Rut.esValido(texto) -> "RUT inválido (ej: 12.345.678-5)"
        else -> null
    }

    fun correo(texto: String): String? = when {
        texto.isBlank() -> "El correo es obligatorio"
        !texto.trim().matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) -> "Correo inválido (ej: nombre@empresa.cl)"
        else -> null
    }

    fun clave(texto: String): String? = if (texto.isEmpty()) "La contraseña es obligatoria" else null

    fun nombre(texto: String, etiqueta: String = "El nombre"): String? = when {
        texto.isBlank() -> "$etiqueta es obligatorio"
        texto.trim().length < 3 -> "$etiqueta debe tener al menos 3 caracteres"
        else -> null
    }

    private fun formato(d: Double) = if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()
}
