package br.edu.utfpr.trabalhofinal.utils

import br.edu.utfpr.trabalhofinal.data.Conta
import br.edu.utfpr.trabalhofinal.data.TipoContaEnum
import java.math.BigDecimal
import java.sql.Date
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

fun List<Conta>.calcularSaldo(): BigDecimal = map {
    if (it.paga) {
        if (it.tipo == TipoContaEnum.DESPESA) {
            it.valor.negate()
        } else {
            it.valor
        }
    } else {
        BigDecimal.ZERO
    }
}.sumOf { it }

fun List<Conta>.calcularProjecao(): BigDecimal = map {
    if (it.tipo == TipoContaEnum.DESPESA) it.valor.negate() else it.valor
}.sumOf { it }

fun BigDecimal.formatar(): String {
    val formatter = DecimalFormat("R$#,##0.00")
    return formatter.format(this)
}

fun LocalDate.formatar(): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return format(formatter)
}

fun Long.toBrazilianDateFormat(
    pattern: String = "dd/MM/yyyy"
): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(
        pattern, Locale("pt-br")
    ).apply {
        timeZone = TimeZone.getTimeZone("GMT")
    }
    return formatter.format(date)
}