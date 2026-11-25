package com.example.induztekprotocolos.domain

import com.example.induztekprotocolos.domain.EstadoProtocolo.*

object TransicionesEstado {
    fun permitida(desde: EstadoProtocolo, hacia: EstadoProtocolo, rol: Rol): Boolean = when {
        hacia == FORMALIZADO -> desde == REGISTRADO_TERRENO && rol in setOf(Rol.INGENIERO, Rol.JEFE_TECNICO)
        hacia == OBSERVADO -> desde == REGISTRADO_TERRENO && rol in setOf(Rol.INGENIERO, Rol.JEFE_TECNICO)
        hacia == REGISTRADO_TERRENO && desde == OBSERVADO -> rol in setOf(Rol.TECNICO, Rol.INGENIERO)
        hacia == REGISTRADO_TERRENO && desde == BORRADOR -> rol in setOf(Rol.TECNICO, Rol.INGENIERO)
        hacia == ANULADO -> desde != FORMALIZADO && desde != ANULADO && rol in setOf(Rol.INGENIERO, Rol.JEFE_TECNICO)
        else -> false
    }

    fun esFinal(estado: EstadoProtocolo) = estado == FORMALIZADO

    fun editable(estado: EstadoProtocolo) = estado == BORRADOR || estado == OBSERVADO

    fun requiereMotivo(hacia: EstadoProtocolo) = hacia == OBSERVADO || hacia == ANULADO
}
