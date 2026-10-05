package app.eixo.dominio

/**
 * Classificação de consumo contra o esperado do caminhão.
 *   real ≥ esperado          → NORMAL
 *   real entre 90% e 99%     → ATENCAO
 *   real abaixo de 90%       → CRITICO
 */
enum class Classificacao(val rotulo: String) {
    NORMAL("Normal"),
    ATENCAO("Atenção"),
    CRITICO("Crítico"),
}

fun classificarConsumo(real: Double, esperado: Double, toleranciaAtencao: Double = 0.90): Classificacao {
    if (esperado <= 0) return Classificacao.NORMAL
    val razao = real / esperado
    return when {
        razao >= 1.0 -> Classificacao.NORMAL
        razao >= toleranciaAtencao -> Classificacao.ATENCAO
        else -> Classificacao.CRITICO
    }
}

/** Diferença percentual do real para o esperado: 1,23 contra 1,40 → -12,1 */
fun desvioPercentual(real: Double, esperado: Double): Double =
    if (esperado <= 0) 0.0 else (real / esperado - 1.0) * 100.0
