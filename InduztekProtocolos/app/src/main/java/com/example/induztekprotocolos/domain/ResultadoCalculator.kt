package com.example.induztekprotocolos.domain

import kotlin.math.abs

data class ResultadoCalculo(val resultado: ResultadoPrueba?, val valorDerivado: Double? = null, val etiquetaDerivada: String? = null)

object ResultadoCalculator {

    fun calcular(tipoPrueba: TipoPrueba, tipoEquipo: TipoEquipo, valores: Map<String, Double>, maxContactosUohmDePlaca: Double?): ResultadoCalculo =
        when (tipoPrueba) {
            TipoPrueba.AISLACION -> aislacion(tipoEquipo, valores)
            TipoPrueba.TTR -> ttr(valores)
            TipoPrueba.CONTACTOS -> contactos(valores, maxContactosUohmDePlaca)
            TipoPrueba.OPERACION_RELE -> operacionRele(valores)
            TipoPrueba.DEVANADOS -> ResultadoCalculo(null)
        }

    private fun aislacion(tipoEquipo: TipoEquipo, v: Map<String, Double>): ResultadoCalculo {
        val l1 = v["LECTURA_1MIN"] ?: return ResultadoCalculo(null)
        if (l1 < 100.0) return ResultadoCalculo(ResultadoPrueba.RECHAZADO, l1, "Lectura 1 min")
        if (!CatalogoPruebas.aislacionConIp(tipoEquipo)) return ResultadoCalculo(ResultadoPrueba.APROBADO, l1, "Lectura 1 min")
        val l10 = v["LECTURA_10MIN"] ?: return ResultadoCalculo(null)
        val ip = l10 / l1
        val r = when {
            ip >= 2.0 -> ResultadoPrueba.APROBADO
            ip >= 1.0 -> ResultadoPrueba.OBSERVADO
            else -> ResultadoPrueba.RECHAZADO
        }
        return ResultadoCalculo(r, ip, "IP")
    }

    private fun ttr(v: Map<String, Double>): ResultadoCalculo {
        val teorica = v["RELACION_TEORICA"] ?: return ResultadoCalculo(null)
        val medida = v["RELACION_MEDIDA"] ?: return ResultadoCalculo(null)
        if (teorica == 0.0) return ResultadoCalculo(null)
        val e = abs(medida - teorica) / teorica * 100.0
        val r = when {
            e <= 0.5 -> ResultadoPrueba.APROBADO
            e <= 1.0 -> ResultadoPrueba.OBSERVADO
            else -> ResultadoPrueba.RECHAZADO
        }
        return ResultadoCalculo(r, e, "Error %")
    }

    private fun contactos(v: Map<String, Double>, maxDePlaca: Double?): ResultadoCalculo {
        val medido = v["RESISTENCIA_CONTACTO"] ?: return ResultadoCalculo(null)
        if (maxDePlaca == null || maxDePlaca == 0.0) return ResultadoCalculo(null)
        val pct = medido / maxDePlaca * 100.0
        val r = when {
            pct <= 100.0 -> ResultadoPrueba.APROBADO
            pct <= 120.0 -> ResultadoPrueba.OBSERVADO
            else -> ResultadoPrueba.RECHAZADO
        }
        return ResultadoCalculo(r, pct, "% del máximo de placa")
    }

    private fun operacionRele(v: Map<String, Double>): ResultadoCalculo {
        val esperado = v["TIEMPO_ESPERADO"] ?: return ResultadoCalculo(null)
        val medido = v["TIEMPO_MEDIDO"] ?: return ResultadoCalculo(null)
        if (esperado == 0.0) return ResultadoCalculo(null)
        val d = abs(medido - esperado) / esperado * 100.0
        val r = when {
            d <= 5.0 -> ResultadoPrueba.APROBADO
            d <= 10.0 -> ResultadoPrueba.OBSERVADO
            else -> ResultadoPrueba.RECHAZADO
        }
        return ResultadoCalculo(r, d, "Diferencia %")
    }

    fun resultadoGlobal(resultados: List<ResultadoPrueba>): ResultadoPrueba? = when {
        resultados.isEmpty() -> null
        ResultadoPrueba.RECHAZADO in resultados -> ResultadoPrueba.RECHAZADO
        ResultadoPrueba.OBSERVADO in resultados -> ResultadoPrueba.OBSERVADO
        else -> ResultadoPrueba.APROBADO
    }

    fun variacionSignificativa(actual: Double, anterior: Double): Boolean =
        anterior != 0.0 && abs(actual - anterior) / abs(anterior) * 100.0 > 25.0

    fun porcentajeVariacion(actual: Double, anterior: Double): Double? =
        if (anterior == 0.0) null else (actual - anterior) / abs(anterior) * 100.0
}
