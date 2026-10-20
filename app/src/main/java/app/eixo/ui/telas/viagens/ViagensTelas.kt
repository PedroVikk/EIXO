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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eixo.dados.Carga
import app.eixo.dados.Dados
import app.eixo.dados.Perfil
import app.eixo.dados.Sessao
import app.eixo.dados.StatusViagem
import app.eixo.dados.Viagem
import app.eixo.ui.componentes.EixoAlerta
import app.eixo.ui.componentes.EixoBotao
import app.eixo.ui.componentes.EixoBotaoCabecalho
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoCampo
import app.eixo.ui.componentes.EixoCampoDataHora
import app.eixo.ui.componentes.EixoDialogoConfirmacao
import app.eixo.ui.componentes.EixoEstadoVazio
import app.eixo.ui.componentes.EixoEtiqueta
import app.eixo.ui.componentes.EixoLinhaLista
import app.eixo.ui.componentes.EixoPlaca
import app.eixo.ui.componentes.EixoRodapeAcao
import app.eixo.ui.componentes.EixoRotuloSecao
import app.eixo.ui.componentes.EixoSeletor
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
import java.time.LocalDateTime

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

/** TELA 08: Formulário para criar e editar viagem — só administrador. */
@Composable
fun ViagemFormTela(viagemId: String?, aoVoltar: () -> Unit) {
    val existente = remember(viagemId) { viagemId?.let { Dados.viagens.buscar(it) } }
    val caminhoes by Dados.caminhoes.itens.collectAsState()
    val usuarios by Dados.usuarios.itens.collectAsState()
    val cargas by Dados.cargas.itens.collectAsState()
    val viagens by Dados.viagens.itens.collectAsState()

    val motoristas = remember(usuarios) {
        usuarios.filter { it.perfil == Perfil.MOTORISTA && it.ativo }
    }

    var caminhaoSelecionado by remember { mutableStateOf(caminhoes.firstOrNull { it.id == existente?.caminhaoId }) }
    var motoristaSelecionado by remember { mutableStateOf(motoristas.firstOrNull { it.id == existente?.motoristaId }) }
    var cargaSelecionada by remember { mutableStateOf(cargas.firstOrNull { it.id == existente?.cargaId }) }
    var origem by rememberSaveable { mutableStateOf(existente?.origem ?: "") }
    var destino by rememberSaveable { mutableStateOf(existente?.destino ?: "") }
    var saidaPrevista by remember { mutableStateOf(existente?.saidaPrevista ?: LocalDateTime.now().plusHours(2)) }
    var observacao by rememberSaveable { mutableStateOf(existente?.observacao ?: "") }

    var tentouSalvar by rememberSaveable { mutableStateOf(false) }
    var confirmarExclusao by rememberSaveable { mutableStateOf(false) }

    val erroCaminhao = if (caminhaoSelecionado == null) "Escolha o caminhão." else null
    val erroMotorista = if (motoristaSelecionado == null) "Escolha o motorista." else null
    val erroOrigem = if (origem.isBlank()) "Informe a origem da viagem." else null
    val erroDestino = if (destino.isBlank()) "Informe o destino da viagem." else null
    val valido = listOf(erroCaminhao, erroMotorista, erroOrigem, erroDestino).all { it == null }

    val numeroViagem = existente?.numero ?: remember { Dados.proximoNumeroViagem() }

    val caminhoesEmViagemIds = remember(viagens) {
        viagens.filter { it.status == StatusViagem.EM_ANDAMENTO && it.id != existente?.id }.map { it.caminhaoId }.toSet()
    }
    val motoristasEmViagemIds = remember(viagens) {
        viagens.filter { it.status == StatusViagem.EM_ANDAMENTO && it.id != existente?.id }.map { it.motoristaId }.toSet()
    }

    fun salvar() {
        tentouSalvar = true
        if (!valido) return
        Dados.viagens.salvar(
            Viagem(
                id = existente?.id ?: Dados.viagens.novoId(),
                numero = numeroViagem,
                caminhaoId = caminhaoSelecionado!!.id,
                motoristaId = motoristaSelecionado!!.id,
                cargaId = cargaSelecionada?.id,
                origem = origem.trim(),
                destino = destino.trim(),
                saidaPrevista = saidaPrevista,
                status = existente?.status ?: StatusViagem.PLANEJADA,
                kmInicial = existente?.kmInicial,
                kmFinal = existente?.kmFinal,
                observacao = observacao.trim(),
            ),
        )
        aoVoltar()
    }

    EixoTela(
        cabecalho = {
            EixoCabecalho(
                titulo = if (existente == null) "Nova viagem" else "Editar viagem",
                sobretitulo = "VIAGEM #$numeroViagem",
                aoVoltar = aoVoltar,
                acoes = {
                    if (existente != null && existente.status == StatusViagem.PLANEJADA) {
                        EixoBotaoCabecalho(Icons.Outlined.DeleteOutline, "Excluir viagem") {
                            confirmarExclusao = true
                        }
                    }
                },
            )
        },
        rodape = {
            EixoRodapeAcao {
                EixoBotao(if (existente == null) "Criar viagem" else "Salvar alterações", aoClicar = ::salvar)
            }
        },
    ) {
        EixoSeletor(
            rotulo = "Caminhão",
            opcoes = caminhoes,
            selecionado = caminhaoSelecionado,
            aoSelecionar = { caminhaoSelecionado = it },
            textoDe = { "${it.placa} - ${it.modelo}" },
            detalheDe = { c ->
                val statusCam = if (caminhoesEmViagemIds.contains(c.id)) "em viagem" else "disponível"
                "${c.modelo} · $statusCam"
            },
            erro = if (tentouSalvar) erroCaminhao else null,
        )

        EixoSeletor(
            rotulo = "Motorista",
            opcoes = motoristas,
            selecionado = motoristaSelecionado,
            aoSelecionar = { motoristaSelecionado = it },
            textoDe = { it.nome },
            detalheDe = { u ->
                val cnh = u.motorista?.categoriaCnh ?: ""
                val statusMot = if (motoristasEmViagemIds.contains(u.id)) "com viagem em aberto" else "sem viagem em aberto"
                "CNH $cnh · $statusMot"
            },
            erro = if (tentouSalvar) erroMotorista else null,
        )

        EixoSeletor(
            rotulo = "Carga",
            opcoes = listOf<Carga?>(null) + cargas,
            selecionado = cargaSelecionada,
            aoSelecionar = { cargaSelecionada = it },
            textoDe = { it?.descricao ?: "Nenhuma (sem carga)" },
            detalheDe = { c -> c?.let { "${it.cliente} · ${formatarKm(it.pesoKg)} kg" } },
            placeholder = "Selecione uma carga (opcional)",
            dica = "OPCIONAL",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2),
        ) {
            EixoCampo(
                valor = origem,
                aoMudar = { origem = it },
                rotulo = "Origem",
                placeholder = "Goiânia/GO",
                modifier = Modifier.weight(1f),
                erro = if (tentouSalvar) erroOrigem else null,
                teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            )
            EixoCampo(
                valor = destino,
                aoMudar = { destino = it },
                rotulo = "Destino",
                placeholder = "Cuiabá/MT",
                modifier = Modifier.weight(1f),
                erro = if (tentouSalvar) erroDestino else null,
                teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            )
        }

        EixoCampoDataHora(
            rotulo = "Saída prevista",
            dataHora = saidaPrevista,
            aoSelecionar = { saidaPrevista = it },
        )

        EixoAlerta(
            titulo = "KM inicial vem na saída",
            descricao = "O motorista informa ao iniciar a viagem.",
            icone = Icons.Outlined.Info,
            tom = Tom.Acao,
        )

        EixoCampo(
            valor = observacao,
            aoMudar = { observacao = it },
            rotulo = "Observação",
            etiqueta = "Opcional",
            tomEtiqueta = Tom.Neutro,
            placeholder = "Informações adicionais da rota",
            linhas = 2,
            teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Default),
        )
    }

    if (confirmarExclusao && existente != null) {
        EixoDialogoConfirmacao(
            titulo = "Excluir viagem #${existente.numero}?",
            texto = "A viagem será removida. Essa ação não pode ser desfeita.",
            textoConfirmar = "Excluir viagem",
            perigoso = true,
            aoConfirmar = {
                Dados.viagens.remover(existente.id)
                confirmarExclusao = false
                aoVoltar()
            },
            aoCancelar = { confirmarExclusao = false },
        )
    }
}

/** TELA 09: Detalhe e acompanhamento da viagem */
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
        oQueFazer = "Detalhes da viagem",
        aoVoltar = aoVoltar,
    )
}
