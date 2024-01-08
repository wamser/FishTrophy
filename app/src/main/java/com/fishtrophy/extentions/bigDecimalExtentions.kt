package com.fishtrophy.extentions

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

fun BigDecimal.formataDuasCasas(): String {
    val formatador=DecimalFormat("#,###.##")
    return formatador.format(this)
}