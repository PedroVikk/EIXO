package app.eixo.ui.telas.viagens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eixo.dados.Dados
import app.eixo.dados.Perfil
import app.eixo.dados.Sessao
import app.eixo.dados.StatusViagem
import app.eixo.ui.componentes.EixoBotao
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoEstadoVazio
import app.eixo.ui.componentes.EixoEtiqueta
import app.eixo.ui.componentes.EixoLinhaLista
import app.eixo.ui.componentes.EixoPlaca
import app.eixo.ui.componentes.EixoRodapeAcao
import app.eixo.ui.componentes.EixoRotuloSecao
import app.eixo.ui.componentes.EixoTela
import app.eixo.ui.componentes.Tom
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoAlvo
import app.eixo.ui.design.EixoEspaco
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTipo
import app.eixo.ui.telas.comum.TelaEmConstrucao
import app.eixo.util.formatarConsumo
import app.eixo.util.formatarDiaMesHora
import app.eixo.util.formatarKm

enum class FiltroStatusViagem(val rotulo: String) {
    TODAS("Todas"),
    EM_ANDAMENTO("Em andamento"),
    PLANEJADAS("Planejadas"),
    FINALIZADAS("Finalizadas"),
}

internal fun tomParaStatus(status: StatusViagem): Tom = when (status) {
    StatusViagem.EM_ANDAMENTO -> Tom.Acao
    StatusViagem.PLANEJADA -> Tom.Neutro
    StatusViagem.FINALIZADA -> Tom.Sucesso
    StatusViagem.CANCELADA -> Tom.Critico
}

/**
 * TELA 07: Lista de Viagens.
 */
@Composable
fun ViagensTela(
    barra: @Composable () -> Unit,
    aoNova: () -> Unit,
    aoAbrir: (viagemId: String) -> Unit,
) {
    val c = Eixo.cores
    val usuario by Sessao.usuario.collectAsState()
    val viagens by Dados.viagens.itens.collectAsState()
    val caminhoes by Dados.caminhoes.itens.collectAsState()
    val usuarios by Dados.usuarios.itens.collectAsState()
    val abastecimentos by Dados.abastecimentos.itens.collectAsState()

    var filtroSelecionado by rememberSaveable { mutableStateOf(FiltroStatusViagem.TODAS) }

    // Motoristas só enxergam as próprias viagens; administradores veem todas.
    val viagensDoUsuario = remember(viagens, usuario) {
        if (usuario?.perfil == Perfil.MOTORISTA) {
            viagens.filter { it.motoristaId == usuario?.id }
        } else {
            viagens
        }
    }

    // Filtragem secundária pelas pílulas de status
    val filtradas = remember(viagensDoUsuario, filtroSelecionado) {
        when (filtroSelecionado) {
            FiltroStatusViagem.TODAS -> viagensDoUsuario
            FiltroStatusViagem.EM_ANDAMENTO -> viagensDoUsuario.filter { it.status == StatusViagem.EM_ANDAMENTO }
            FiltroStatusViagem.PLANEJADAS -> viagensDoUsuario.filter { it.status == StatusViagem.PLANEJADA }
            FiltroStatusViagem.FINALIZADAS -> viagensDoUsuario.filter { it.status == StatusViagem.FINALIZADA }
        }
    }

    val eAdmin = usuario?.perfil == Perfil.ADMIN

    EixoTela(
        cabecalho = { EixoCabecalho("Viagens", sobretitulo = "Operação") },
        rodape = if (eAdmin) {
            { EixoRodapeAcao { EixoBotao("Nova viagem", aoClicar = aoNova, icone = Icons.Outlined.Add) } }
        } else null,
        barraInferior = barra,
        rolavel = false,
    ) {
        // Barra rolável de filtros de status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2),
        ) {
            FiltroStatusViagem.entries.forEach { filtro ->
                val marcado = filtro == filtroSelecionado
                Box(
                    modifier = Modifier
                        .heightIn(min = EixoAlvo.pequeno)
                        .clip(EixoRaio.pilula)
                        .background(if (marcado) c.superficie else c.superficie2)
                        .border(1.dp, if (marcado) c.acao else c.borda, EixoRaio.pilula)
                        .clickable(role = Role.RadioButton) { filtroSelecionado = filtro }
                        .semantics { selected = marcado }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (marcado) {
                            Icon(Icons.Outlined.Check, contentDescription = null, tint = c.acao, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            filtro.rotulo,
                            style = EixoTipo.rotulo.copy(fontSize = 12.sp),
                            color = if (marcado) c.acao else c.textoFraco,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        EixoRotuloSecao("${filtradas.size} ${if (filtradas.size == 1) "viagem" else "viagens"}")

        if (filtradas.isEmpty()) {
            EixoEstadoVazio(
                icone = Icons.Outlined.Route,
                titulo = if (viagensDoUsuario.isEmpty()) "Nenhuma viagem encontrada" else "Nenhuma viagem nesta categoria",
                descricao = if (viagensDoUsuario.isEmpty()) "Não há viagens cadastradas." else "Altere o filtro acima para ver outras viagens.",
                acao = if (eAdmin && viagensDoUsuario.isEmpty()) {
                    { EixoBotao("Nova viagem", aoClicar = aoNova, icone = Icons.Outlined.Add, larguraTotal = false) }
                } else null,
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(EixoEspaco.s2),
            ) {
                items(filtradas, key = { it.id }) { viagem ->
                    val caminhao = caminhoes.firstOrNull { it.id == viagem.caminhaoId }
                    val motorista = usuarios.firstOrNull { it.id == viagem.motoristaId }
                    val abasts = abastecimentos.filter { it.viagemId == viagem.id }

                    val textoApoio = when (viagem.status) {
                        StatusViagem.EM_ANDAMENTO -> {
                            val kmIn = viagem.kmInicial ?: caminhao?.kmAtual ?: 0L
                            val kmAtual = maxOf(abasts.maxOfOrNull { it.km } ?: kmIn, caminhao?.kmAtual ?: kmIn)
                            val rodados = if (kmAtual > kmIn) kmAtual - kmIn else 0L
                            "${formatarKm(rodados)} km · ${abasts.size} ${if (abasts.size == 1) "abastecimento" else "abastecimentos"}"
                        }
                        StatusViagem.PLANEJADA -> {
                            "Saída ${formatarDiaMesHora(viagem.saidaPrevista)}"
                        }
                        StatusViagem.FINALIZADA -> {
                            val kmRodados = viagem.kmPercorridos ?: 0L
                            val totalLitros = abasts.sumOf { it.litros }
                            val consumo = if (totalLitros > 0 && kmRodados > 0) kmRodados.toDouble() / totalLitros else null
                            if (consumo != null) {
                                "${formatarKm(kmRodados)} km · ${formatarConsumo(consumo)}"
                            } else {
                                "${formatarKm(kmRodados)} km"
                            }
                        }
                        StatusViagem.CANCELADA -> "Viagem cancelada"
                    }

                    EixoLinhaLista(
                        titulo = viagem.rota,
                        subtitulo = "#${viagem.numero} - ${motorista?.nome ?: "Motorista"}",
                        apoio = textoApoio,
                        inicio = { EixoPlaca(caminhao?.placa ?: "—") },
                        fim = {
                            EixoEtiqueta(
                                texto = viagem.status.rotulo,
                                tom = tomParaStatus(viagem.status),
                                pulsando = viagem.status == StatusViagem.EM_ANDAMENTO,
                            )
                        },
                        aoClicar = { aoAbrir(viagem.id) },
                    )
                }
            }
        }
    }
}

/** Criação e edição — só administrador. */
@Composable
fun ViagemFormTela(viagemId: String?, aoVoltar: () -> Unit) {
    TelaEmConstrucao(
        titulo = if (viagemId == null) "Nova viagem" else "Editar viagem",
        modulo = "Viagens",
        oQueFazer = "Formulário de criação/edição",
        aoVoltar = aoVoltar,
    )
}

/** Detalhe da viagem */
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
        oQueFazer = "Detalhes e acompanhamento da viagem",
        aoVoltar = aoVoltar,
    )
}
