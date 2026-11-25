package com.example.induztekprotocolos.ui

import java.text.SimpleDateFormat
import java.util.Locale

object Formato {
    private val local = Locale.forLanguageTag("es-CL")
    fun fecha(t: Long): String = SimpleDateFormat("dd-MM-yyyy", local).format(t)
    fun fechaHora(t: Long): String = SimpleDateFormat("dd-MM-yyyy HH:mm", local).format(t)
    fun numero(d: Double): String = if (d == d.toLong().toDouble()) d.toLong().toString() else String.format(local, "%.2f", d)
    fun porcentaje(d: Double): String = String.format(local, "%+.1f%%", d)
    fun coordenada(d: Double): String = String.format(local, "%.5f", d)
}
