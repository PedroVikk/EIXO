package app.eixo.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToLong

/*
 * Regras de escrita de número do Eixo:
 *   KM      152.430 km     — ponto no milhar, sem decimal
 *   Litros  400,00 L       — duas casas
 *   Valor   R$ 2.840,00    — espaço depois de R$, duas casas sempre
 *   Consumo 2,84 km/L
 */

private fun agruparMilhar(digitos: String): String {
    val sb = StringBuilder()
    digitos.forEachIndexed { i, c ->
        if (i > 0 && (digitos.length - i) % 3 == 0) sb.append('.')
        sb.append(c)
    }
    return sb.toString()
}

fun formatarKm(km: Long): String = agruparMilhar(abs(km).toString())

fun formatarDecimal(valor: Double, casas: Int = 2): String {
    val fator = Math.pow(10.0, casas.toDouble())
    val inteiroComCasas = (abs(valor) * fator).roundToLong().toString().padStart(casas + 1, '0')
    val inteiro = inteiroComCasas.dropLast(casas)
    val fracao = inteiroComCasas.takeLast(casas)
    return "${agruparMilhar(inteiro)},$fracao"
}

fun formatarMoeda(valor: Double) = "R$ ${formatarDecimal(valor)}"
fun formatarLitros(litros: Double) = "${formatarDecimal(litros)} L"
fun formatarConsumo(kmPorLitro: Double) = "${formatarDecimal(kmPorLitro)} km/L"
fun formatarCustoKm(custo: Double) = "R$ ${formatarDecimal(custo)}/km"

private val dataBr = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val dataHoraBr = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
private val diaMesHora = DateTimeFormatter.ofPattern("dd/MM HH:mm")

fun formatarData(data: LocalDate): String = data.format(dataBr)
fun formatarDataHora(data: LocalDateTime): String = data.format(dataHoraBr)
fun formatarDiaMesHora(data: LocalDateTime): String = data.format(diaMesHora)

// --- conversão entre o que o campo guarda (só dígitos) e o valor numérico ----

/** O campo de dinheiro guarda centavos como dígitos: "276900" → 2769.0 */
fun digitosParaDecimal(digitos: String, casas: Int = 2): Double =
    (digitos.toLongOrNull() ?: 0L) / Math.pow(10.0, casas.toDouble())

/** 2769.0 → "276900" (para preencher um campo de dinheiro já existente). */
fun decimalParaDigitos(valor: Double, casas: Int = 2): String =
    (valor * Math.pow(10.0, casas.toDouble())).roundToLong().takeIf { it > 0 }?.toString() ?: ""

fun digitosParaKm(digitos: String): Long? = digitos.toLongOrNull()
