package app.eixo.dominio

import app.eixo.dados.Abastecimento
import app.eixo.dados.Caminhao
import app.eixo.dados.StatusViagem
import app.eixo.dados.Viagem

/**
 * Consumo de um caminhão num conjunto de abastecimentos.
 *
 * Regra (RN-05/RN-07): o primeiro abastecimento é só a referência — ele não gera consumo
 * sozinho. Daí em diante, cada abastecimento de tanque cheio fecha um trecho:
 * km do trecho ÷ litros repostos. A média é sempre PONDERADA (soma km ÷ soma litros),
 * nunca a média das médias.
 */
data class ConsumoCaminhao(
    val caminhao: Caminhao,
    val kmRodados: Long,
    val litros: Double,
    val gastoDiesel: Double,
    val gastoArla: Double,
) {
    /** null quando ainda não há trecho fechado (só o abastecimento de referência). */
    val consumo: Double? get() = if (litros > 0 && kmRodados > 0) kmRodados / litros else null
    val custoPorKm: Double? get() = if (kmRodados > 0) (gastoDiesel + gastoArla) / kmRodados else null
    val classificacao: Classificacao? get() = consumo?.let { classificarConsumo(it, caminhao.consumoEsperado) }
}

fun consumoDoCaminhao(caminhao: Caminhao, abastecimentos: List<Abastecimento>): ConsumoCaminhao {
    val doCaminhao = abastecimentos.filter { it.caminhaoId == caminhao.id }.sortedBy { it.km }
    var km = 0L
    var litros = 0.0
    var diesel = 0.0
    var arla = 0.0
    var referencia: Abastecimento? = null
    for (atual in doCaminhao) {
        val ref = referencia
        if (ref != null && atual.tanqueCheio) {
            km += atual.km - ref.km
            litros += atual.litros
            diesel += atual.valorTotal
            arla += atual.arlaValor ?: 0.0
        }
        if (atual.tanqueCheio) referencia = atual
    }
    return ConsumoCaminhao(caminhao, km, litros, diesel, arla)
}

/** Tudo que o painel do administrador mostra. */
data class IndicadoresFrota(
    val caminhoes: Int,
    val emViagem: Int,
    val disponiveis: Int,
    val viagensPlanejadas: Int,
    val viagensFinalizadas: Int,
    val kmRodados: Long,
    val litros: Double,
    /** Gasto total com diesel no período — todos os abastecimentos. */
    val gastoDiesel: Double,
    val gastoArla: Double,
    /** Média ponderada da frota; null se nenhum caminhão fechou um trecho. */
    val consumoMedio: Double?,
    val custoPorKm: Double?,
    val porCaminhao: List<ConsumoCaminhao>,
    val abastecimentosSemViagem: Int,
) {
    val gastoTotal: Double get() = gastoDiesel + gastoArla
    /** Caminhões fora da faixa esperada, do pior para o melhor. */
    val foraDaFaixa: List<ConsumoCaminhao>
        get() = porCaminhao.filter { it.classificacao == Classificacao.ATENCAO || it.classificacao == Classificacao.CRITICO }
            .sortedBy { (it.consumo ?: 0.0) / it.caminhao.consumoEsperado }
}

fun calcularIndicadores(
    caminhoes: List<Caminhao>,
    viagens: List<Viagem>,
    abastecimentos: List<Abastecimento>,
): IndicadoresFrota {
    val porCaminhao = caminhoes.map { consumoDoCaminhao(it, abastecimentos) }
    val emAndamento = viagens.filter { it.status == StatusViagem.EM_ANDAMENTO }.map { it.caminhaoId }.toSet()
    val kmTrechos = porCaminhao.sumOf { it.kmRodados }
    val litrosTrechos = porCaminhao.sumOf { it.litros }
    val gastoTrechos = porCaminhao.sumOf { it.gastoDiesel + it.gastoArla }
    return IndicadoresFrota(
        caminhoes = caminhoes.size,
        emViagem = emAndamento.size,
        disponiveis = caminhoes.size - emAndamento.size,
        viagensPlanejadas = viagens.count { it.status == StatusViagem.PLANEJADA },
        viagensFinalizadas = viagens.count { it.status == StatusViagem.FINALIZADA },
        kmRodados = kmTrechos,
        litros = abastecimentos.sumOf { it.litros },
        gastoDiesel = abastecimentos.sumOf { it.valorTotal },
        gastoArla = abastecimentos.sumOf { it.arlaValor ?: 0.0 },
        consumoMedio = if (litrosTrechos > 0) kmTrechos / litrosTrechos else null,
        custoPorKm = if (kmTrechos > 0) gastoTrechos / kmTrechos else null,
        porCaminhao = porCaminhao,
        abastecimentosSemViagem = abastecimentos.count { it.viagemId == null },
    )
}
