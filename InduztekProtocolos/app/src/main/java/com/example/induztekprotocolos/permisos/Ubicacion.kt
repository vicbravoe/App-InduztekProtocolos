package com.example.induztekprotocolos.permisos

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat

object Ubicacion {

    @SuppressLint("MissingPermission")
    fun obtener(context: Context, alTerminar: (Location?) -> Unit) {
        if (!Permisos.tiene(context, Permisos.UBICACION)) {
            alTerminar(null)
            return
        }
        try {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val proveedores = manager.getProviders(true)
            if (proveedores.isEmpty()) {
                alTerminar(null)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val proveedor = if (LocationManager.GPS_PROVIDER in proveedores) LocationManager.GPS_PROVIDER else proveedores.first()
                manager.getCurrentLocation(proveedor, null, ContextCompat.getMainExecutor(context)) { ubicacion ->
                    alTerminar(ubicacion ?: ultimaConocida(manager, proveedores))
                }
            } else {
                alTerminar(ultimaConocida(manager, proveedores))
            }
        } catch (e: SecurityException) {
            alTerminar(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun ultimaConocida(manager: LocationManager, proveedores: List<String>): Location? =
        proveedores.mapNotNull { manager.getLastKnownLocation(it) }.maxByOrNull { it.time }
}
