package app.eixo.ui.telas.combustivel

import androidx.compose.runtime.Composable
import app.eixo.ui.telas.comum.TelaEmConstrucao

/*
 * MÓDULO COMBUSTÍVEL — registro de abastecimento.
 * Dados: Dados.abastecimentos. KM sugerido: Dados.ultimoKm(caminhaoId).
 * Campos prontos: EixoCampoKm (com sugestão), EixoCampoLitros, EixoCampoDinheiro,
 * EixoCampoCalculado (preço/L), EixoSimNao (tanque cheio), EixoSimNao + campos de ARLA.
 * Telas de referência no design system: "Abastecer · números" e "Conferência".
 */

@Composable
fun AbastecimentosTela(
    barra: @Composable () -> Unit,
    aoNovo: () -> Unit,
    aoAbrir: (abastecimentoId: String) -> Unit,
) {
    TelaEmConstrucao(
        titulo = "Combustível",
        modulo = "Combustível",
        oQueFazer = "Histórico de abastecimentos: data, placa, KM, litros e valor. Motorista vê só os dele.",
        barra = barra,
    )
}

/** `viagemId` vem preenchido quando o registro é aberto a partir de uma viagem; pode ser nulo (avulso). */
@Composable
fun AbastecimentoFormTela(abastecimentoId: String?, viagemId: String?, aoVoltar: () -> Unit) {
    TelaEmConstrucao(
        titulo = "Abastecimento",
        modulo = "Combustível",
        oQueFazer = "KM pré-preenchido, litros, valor, preço por litro calculado, combustível, posto, tanque cheio e ARLA opcional.",
        aoVoltar = aoVoltar,
    )
}
