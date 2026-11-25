package com.example.induztekprotocolos.domain

class ErrorNegocio(mensaje: String, val codigo: Int = 0) : Exception(mensaje)
