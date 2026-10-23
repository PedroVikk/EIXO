package app.eixo.ui.telas.painel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.eixo.dados.Dados
import app.eixo.dominio.calcularIndicadores
import app.eixo.ui.componentes.EixoAlerta
import app.eixo.ui.componentes.EixoBotaoCabecalho
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoCartaoEficiencia
import app.eixo.ui.componentes.EixoKpi
import app.eixo.ui.componentes.EixoOdometro
import app.eixo.ui.componentes.EixoResumoNumeros
import app.eixo.ui.componentes.EixoRotuloSecao
import app.eixo.ui.componentes.EixoTela
import app.eixo.ui.componentes.NumeroResumo
import app.eixo.ui.componentes.Tom
import app.eixo.ui.design.EixoEspaco
import java.util.Locale

/*
 * MÓDULO PAINEL (dashboard do administrador).
 * Ordem das seções = design 02-painel:
 *   cabeçalho com odômetro → resumo da frota → indicadores do mês →
 *   "precisa de você" (alertas) → eficiência por caminhão.
 * Os números vêm prontos de dominio/Indicadores.kt → calcularIndicadores(...).
 */

private val ptBr = Locale.forLanguageTag("pt-BR")

// Formatadores locais (só texto). Se util/Formatos.kt já tiver equivalentes, troque por eles.
private fun dinheiro(v: Double) = "R$ " + String.format(ptBr, "%,.0f", v)
private fun dinheiro2(v: Double) = "R$ " + String.format(ptBr, "%,.2f", v)
private fun litros(v: Double) = String.format(ptBr, "%,.0f", v) + " L"
private fun kmPorLitro(v: Double) = String.format(ptBr, "%.2f", v) + " km/L"

@Composable
fun PainelTela(
    barra: @Composable () -> Unit,
    aoSair: () -> Unit,
    aoAbrirViagens: () -> Unit,
) {
    // CONFIRMAR: nomes dos repositórios em Dados (caminhoes, viagens, abastecimentos).
    val caminhoes by Dados.caminhoes.itens.collectAsState()
    val viagens by Dados.viagens.itens.collectAsState()
    val abastecimentos by Dados.abastecimentos.itens.collectAsState()

    val ind = remember(caminhoes, viagens, abastecimentos) {
        calcularIndicadores(caminhoes, viagens, abastecimentos)
    }

    EixoTela(
        cabecalho = {
            EixoCabecalho(
                titulo = "Painel",
                sobretitulo = "Controle da frota",
                acoes = {
                    EixoBotaoCabecalho(
                        icone = Icons.AutoMirrored.Outlined.Logout,
                        descricao = "Sair",
                        aoClicar = aoSair,
                    )
                },
                conteudo = { EixoOdometro(ind.kmRodados) },
            )
        },
        barraInferior = barra,
    ) {
        // 1. Resumo da frota
        EixoResumoNumeros(
            listOf(
                NumeroResumo(ind.caminhoes.toString(), "Caminhões", Tom.Neutro),
                NumeroResumo(ind.emViagem.toString(), "Em viagem", Tom.Acao),
                NumeroResumo(ind.disponiveis.toString(), "Livres", Tom.Neutro),
                NumeroResumo(ind.viagensPlanejadas.toString(), "Planejadas", Tom.Atencao),
            ),
        )

        // 2. Indicadores do mês (grade 2x2)
        EixoRotuloSecao("Indicadores do mês")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
            Column(Modifier.weight(1f)) {
                EixoKpi("Combustível", dinheiro(ind.gastoTotal), apoio = "diesel + ARLA")
            }
            Column(Modifier.weight(1f)) {
                val precoMedio = if (ind.litros > 0) ind.gastoDiesel / ind.litros else null
                EixoKpi(
                    "Litros",
                    litros(ind.litros),
                    apoio = precoMedio?.let { "preço médio ${dinheiro2(it)}/L" } ?: "sem abastecimentos",
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
            Column(Modifier.weight(1f)) {
                EixoKpi(
                    "Média",
                    ind.consumoMedio?.let { kmPorLitro(it) } ?: "—",
                    apoio = "ponderada",
                )
            }
            Column(Modifier.weight(1f)) {
                EixoKpi(
                    "Custo por km",
                    ind.custoPorKm?.let { dinheiro2(it) } ?: "—",
                    apoio = "combustível",
                )
            }
        }

        // 3. Precisa de você (alertas só aparecem se houver)
        val fora = ind.foraDaFaixa
        if (fora.isNotEmpty() || ind.abastecimentosSemViagem > 0) {
            EixoRotuloSecao("Precisa de você")
            if (fora.isNotEmpty()) {
                EixoAlerta(
                    titulo = if (fora.size == 1) "1 caminhão abaixo do esperado"
                    else "${fora.size} caminhões abaixo do esperado",
                    icone = Icons.Outlined.Speed,
                    descricao = fora.joinToString(" · ") { it.caminhao.placa },
                    tom = Tom.Atencao,
                )
            }
            if (ind.abastecimentosSemViagem > 0) {
                EixoAlerta(
                    titulo = if (ind.abastecimentosSemViagem == 1) "1 abastecimento sem viagem"
                    else "${ind.abastecimentosSemViagem} abastecimentos sem viagem",
                    icone = Icons.Outlined.LinkOff,
                    descricao = "Vincule cada abastecimento a uma viagem",
                    tom = Tom.Atencao,
                )
            }
        }

        // 4. Eficiência por caminhão (só quem já fechou um trecho tem consumo)
        EixoRotuloSecao(
            "Eficiência por caminhão",
            acao = "Ver viagens",
            aoClicarAcao = aoAbrirViagens,
        )
        ind.porCaminhao
            .filter { it.consumo != null }
            .sortedBy { it.consumo!! / it.caminhao.consumoEsperado }
            .forEach { item ->
                EixoCartaoEficiencia(
                    placa = item.caminhao.placa,
                    real = item.consumo!!,
                    esperado = item.caminhao.consumoEsperado,
                    descricao = item.caminhao.modelo,
                    rodape = "${String.format(ptBr, "%,d", item.kmRodados)} km rodados",
                )
            }
    }
}