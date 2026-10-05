package app.eixo.ui.telas.viagens

import androidx.compose.runtime.Composable
import app.eixo.ui.telas.comum.TelaEmConstrucao

/*
 * MÓDULO VIAGENS.
 * Dados: Dados.viagens, Dados.caminhoes, Dados.usuarios (motoristas), Dados.cargas.
 * Status: PLANEJADA → EM_ANDAMENTO → FINALIZADA (ou CANCELADA). Use EixoEtiqueta com o Tom certo.
 * Motorista logado (Sessao.usuario) vê só as viagens dele.
 * Telas de referência no design system: "Viagens", "Nova viagem", "Acompanhamento da viagem".
 */

@Composable
fun ViagensTela(
    barra: @Composable () -> Unit,
    aoNova: () -> Unit,
    aoAbrir: (viagemId: String) -> Unit,
) {
    TelaEmConstrucao(
        titulo = "Viagens",
        modulo = "Viagens",
        oQueFazer = "Lista com placa (EixoPlaca), rota, motorista e etiqueta de status. Filtro por status. Só admin vê o botão \"Nova viagem\".",
        barra = barra,
    )
}

/** Criação e edição — só administrador. Caminhão, motorista, carga (EixoSeletor), origem, destino, saída (EixoCampoDataHora). */
@Composable
fun ViagemFormTela(viagemId: String?, aoVoltar: () -> Unit) {
    TelaEmConstrucao(
        titulo = if (viagemId == null) "Nova viagem" else "Editar viagem",
        modulo = "Viagens",
        oQueFazer = "Formulário com EixoSeletor para caminhão, motorista e carga. Número da viagem: Dados.proximoNumeroViagem().",
        aoVoltar = aoVoltar,
    )
}

/** Detalhe: rota, odômetro, status, abastecimentos da viagem e ação de registrar combustível. */
@Composable
fun ViagemDetalheTela(
    viagemId: String,
    aoVoltar: () -> Unit,
    aoEditar: () -> Unit,
    aoRegistrarAbastecimento: (viagemId: String) -> Unit,
) {
    TelaEmConstrucao(
        titulo = "Viagem",
        modulo = "Viagens",
        oQueFazer = "Cabeçalho com rota e EixoOdometro; EixoResumoNumeros com km, abastecimentos e litros; lista de abastecimentos da viagem.",
        aoVoltar = aoVoltar,
    )
}
