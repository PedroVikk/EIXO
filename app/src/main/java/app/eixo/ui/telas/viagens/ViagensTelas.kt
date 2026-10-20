package app.eixo.ui.telas.viagens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.PlayArrow
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
import app.eixo.dominio.Classificacao
import app.eixo.dominio.classificarConsumo
import app.eixo.ui.componentes.EixoAlerta
import app.eixo.ui.componentes.EixoBotao
import app.eixo.ui.componentes.EixoBotaoCabecalho
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoCampo
import app.eixo.ui.componentes.EixoCampoDataHora
import app.eixo.ui.componentes.EixoCartao
import app.eixo.ui.componentes.EixoCartaoEficiencia
import app.eixo.ui.componentes.EixoDialogoConfirmacao
import app.eixo.ui.componentes.EixoEstadoVazio
import app.eixo.ui.componentes.EixoEtiqueta
import app.eixo.ui.componentes.EixoLinhaLista
import app.eixo.ui.componentes.EixoLinhaValor
import app.eixo.ui.componentes.EixoOdometro
import app.eixo.ui.componentes.EixoPlaca
import app.eixo.ui.componentes.EixoResumoNumeros
import app.eixo.ui.componentes.EixoRodapeAcao
import app.eixo.ui.componentes.EixoRotuloSecao
import app.eixo.ui.componentes.EixoSeletor
import app.eixo.ui.componentes.EixoTela
import app.eixo.ui.componentes.NumeroResumo
import app.eixo.ui.componentes.TamanhoBotao
import app.eixo.ui.componentes.Tom
import app.eixo.ui.componentes.VarianteBotao
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoAlvo
import app.eixo.ui.design.EixoEspaco
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTipo
import app.eixo.util.formatarConsumo
import app.eixo.util.formatarDecimal
import app.eixo.util.formatarDiaMesHora
import app.eixo.util.formatarKm
import app.eixo.util.formatarMoeda
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

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

internal fun tempoRelativo(dataHora: LocalDateTime): String {
    val hoje = LocalDate.now()
    val data = dataHora.toLocalDate()
    val dias = ChronoUnit.DAYS.between(data, hoje)
    return when {
        dias == 0L -> "hoje"
        dias == 1L -> "ontem"
        dias > 1L -> "há $dias dias"
        dias == -1L -> "amanhã"
        else -> "em ${abs(dias)} dias"
    }
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
    val usuario by Sessao.usuario.collectAsState()
    val viagens by Dados.viagens.itens.collectAsState()
    val caminhoes by Dados.caminhoes.itens.collectAsState()
    val usuarios by Dados.usuarios.itens.collectAsState()
    val cargas by Dados.cargas.itens.collectAsState()
    val abastecimentos by Dados.abastecimentos.itens.collectAsState()

    val viagem = viagens.firstOrNull { it.id == viagemId }

    if (viagem == null) {
        EixoTela(
            cabecalho = { EixoCabecalho("Viagem", aoVoltar = aoVoltar) },
        ) {
            EixoEstadoVazio(
                icone = Icons.Outlined.Route,
                titulo = "Viagem não encontrada",
                descricao = "A viagem solicitada foi removida ou não existe.",
                acao = { EixoBotao("Voltar", aoClicar = aoVoltar, larguraTotal = false) },
            )
        }
        return
    }

    val caminhao = caminhoes.firstOrNull { it.id == viagem.caminhaoId }
    val motorista = usuarios.firstOrNull { it.id == viagem.motoristaId }
    val carga = cargas.firstOrNull { it.id == viagem.cargaId }
    val abasts = abastecimentos.filter { it.viagemId == viagem.id }.sortedBy { it.km }

    val eAdmin = usuario?.perfil == Perfil.ADMIN

    var mostrarDialogoIniciar by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoFinalizar by rememberSaveable { mutableStateOf(false) }

    val kmInicialSugestao = remember(viagem, caminhao) {
        Dados.ultimoKm(viagem.caminhaoId) ?: caminhao?.kmAtual ?: 0L
    }

    val kmFinalSugestao = remember(abasts, caminhao, viagem) {
        val maxAbast = abasts.maxOfOrNull { it.km }
        val maxKm = listOfNotNull(maxAbast, caminhao?.kmAtual, viagem.kmInicial).maxOrNull() ?: 0L
        maxKm
    }

    // KM Tacógrafo exibido no cabeçalho
    val kmTacografo = when (viagem.status) {
        StatusViagem.FINALIZADA -> viagem.kmFinal ?: abasts.lastOrNull()?.km ?: kmInicialSugestao
        StatusViagem.EM_ANDAMENTO -> abasts.lastOrNull()?.km ?: viagem.kmInicial ?: kmInicialSugestao
        else -> kmInicialSugestao
    }

    // Cálculos da viagem
    val kmIn = viagem.kmInicial ?: 0L
    val kmFin = viagem.kmFinal ?: abasts.lastOrNull()?.km ?: kmTacografo
    val kmRodados = when (viagem.status) {
        StatusViagem.FINALIZADA -> if (viagem.kmInicial != null && viagem.kmFinal != null) viagem.kmFinal - viagem.kmInicial else (kmFin - kmIn).coerceAtLeast(0L)
        StatusViagem.EM_ANDAMENTO -> if (viagem.kmInicial != null && kmTacografo > viagem.kmInicial) kmTacografo - viagem.kmInicial else 0L
        else -> 0L
    }
    val totalLitros = abasts.sumOf { it.litros }
    val consumoMedio = if (totalLitros > 0 && kmRodados > 0) kmRodados.toDouble() / totalLitros else null
    val esperado = caminhao?.consumoEsperado ?: 2.00
    val classificacao = consumoMedio?.let { classificarConsumo(it, esperado) }

    val tomStatus = tomParaStatus(viagem.status)

    EixoTela(
        cabecalho = {
            EixoCabecalho(
                titulo = viagem.rota,
                sobretitulo = "#${viagem.numero} · ${motorista?.nome ?: ""}",
                aoVoltar = aoVoltar,
                acoes = {
                    if (eAdmin && viagem.status != StatusViagem.FINALIZADA) {
                        EixoBotaoCabecalho(Icons.Outlined.Edit, "Editar viagem", aoEditar)
                    }
                },
                conteudo = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        EixoPlaca(caminhao?.placa ?: "—", sobreEscuro = true)
                        EixoEtiqueta(
                            texto = viagem.status.rotulo,
                            tom = tomStatus,
                            pulsando = viagem.status == StatusViagem.EM_ANDAMENTO,
                        )
                    }
                    Column(Modifier.padding(top = 4.dp)) {
                        Text(
                            "KM ATUAL · TACÓGRAFO",
                            style = EixoTipo.microRotulo,
                            color = Eixo.cores.cabecalhoRotulo,
                        )
                        Box(Modifier.padding(top = 4.dp)) {
                            EixoOdometro(kmTacografo)
                        }
                    }
                },
            )
        },
        rodape = {
            EixoRodapeAcao {
                when (viagem.status) {
                    StatusViagem.PLANEJADA -> {
                        EixoBotao(
                            "Iniciar viagem",
                            aoClicar = {
                                mostrarDialogoIniciar = true
                            },
                            variante = VarianteBotao.Primario,
                            icone = Icons.Outlined.PlayArrow,
                            tamanho = TamanhoBotao.Grande,
                        )
                    }
                    StatusViagem.EM_ANDAMENTO -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2),
                        ) {
                            EixoBotao(
                                "Abastecer",
                                aoClicar = { aoRegistrarAbastecimento(viagem.id) },
                                variante = VarianteBotao.Primario,
                                icone = Icons.Outlined.LocalGasStation,
                                modifier = Modifier.weight(1f),
                                tamanho = TamanhoBotao.Grande,
                            )
                            EixoBotao(
                                "Finalizar",
                                aoClicar = {
                                    mostrarDialogoFinalizar = true
                                },
                                variante = VarianteBotao.Secundario,
                                icone = Icons.Outlined.Flag,
                                modifier = Modifier.weight(1f),
                                tamanho = TamanhoBotao.Grande,
                            )
                        }
                    }
                    StatusViagem.FINALIZADA -> {
                        EixoBotao(
                            "Registrar combustível",
                            aoClicar = { aoRegistrarAbastecimento(viagem.id) },
                            variante = VarianteBotao.Fantasma,
                            icone = Icons.Outlined.LocalGasStation,
                        )
                    }
                    StatusViagem.CANCELADA -> {}
                }
            }
        },
    ) {
        // Faixa com KPIs da viagem
        EixoResumoNumeros(
            itens = listOf(
                NumeroResumo(formatarKm(kmRodados), "km rodados"),
                NumeroResumo(abasts.size.toString(), "abastec."),
                NumeroResumo(formatarDecimal(totalLitros, 0), "litros"),
                NumeroResumo(
                    consumoMedio?.let { formatarDecimal(it) } ?: "—",
                    "km/L",
                    tom = classificacao?.let {
                        when (it) {
                            Classificacao.NORMAL -> Tom.Sucesso
                            Classificacao.ATENCAO -> Tom.Atencao
                            Classificacao.CRITICO -> Tom.Critico
                        }
                    },
                ),
            ),
        )

        // Cartão de eficiência / consumo
        if (caminhao != null) {
            val consumoExibir = consumoMedio ?: caminhao.consumoEsperado
            val rodapeCard = if (consumoMedio != null) {
                val desvio = (consumoMedio / caminhao.consumoEsperado - 1.0) * 100.0
                val sinal = if (desvio > 0) "+" else ""
                "$sinal${formatarDecimal(desvio, 1)}% em relação à meta da frota"
            } else {
                "Sem abastecimentos suficientes para calcular consumo"
            }
            EixoCartaoEficiencia(
                placa = caminhao.placa,
                real = consumoExibir,
                esperado = caminhao.consumoEsperado,
                descricao = caminhao.modelo,
                rodape = rodapeCard,
            )
        }

        // Carga vinculada
        if (carga != null) {
            EixoCartao(espacamento = 4.dp) {
                EixoRotuloSecao("CARGA")
                EixoLinhaValor("Descrição", carga.descricao)
                EixoLinhaValor("Cliente", carga.cliente)
                EixoLinhaValor("Tipo / Peso", "${carga.tipo.rotulo} · ${formatarKm(carga.pesoKg)} kg")
                if (carga.notaFiscal.isNotBlank()) {
                    EixoLinhaValor("Nota Fiscal", carga.notaFiscal)
                }
                if (carga.observacao.isNotBlank()) {
                    EixoLinhaValor("Observação", carga.observacao, numerico = false, ultima = true)
                }
            }
        }

        // Seção Abastecimentos
        EixoRotuloSecao(
            texto = "ABASTECIMENTOS",
            acao = "Registrar",
            aoClicarAcao = { aoRegistrarAbastecimento(viagem.id) },
        )

        if (abasts.isEmpty()) {
            EixoEstadoVazio(
                icone = Icons.Outlined.LocalGasStation,
                titulo = "Nenhum abastecimento",
                descricao = "Os abastecimentos registrados nesta viagem aparecerão aqui.",
                acao = {
                    EixoBotao(
                        "Registrar combustível",
                        aoClicar = { aoRegistrarAbastecimento(viagem.id) },
                        icone = Icons.Outlined.LocalGasStation,
                        larguraTotal = false,
                    )
                },
            )
        } else {
            abasts.forEach { abast ->
                val arlaTexto = abast.arlaLitros?.let { " · ARLA ${formatarDecimal(it, 0)} L" } ?: ""
                val statusTanque = if (abast.tanqueCheio) "cheio" else "parcial"
                EixoLinhaLista(
                    titulo = "${formatarKm(abast.km)} km",
                    subtitulo = "${tempoRelativo(abast.dataHora)} · ${formatarDecimal(abast.litros, 0)} L · $statusTanque$arlaTexto",
                    apoio = abast.posto,
                    inicio = {
                        Box(
                            Modifier.size(38.dp).clip(EixoRaio.sm).background(Eixo.cores.superficie2),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.LocalGasStation, contentDescription = null, tint = Eixo.cores.textoFraco, modifier = Modifier.size(20.dp))
                        }
                    },
                    fim = {
                        Text(
                            formatarMoeda(abast.valorTotal),
                            style = EixoTipo.dadoPequeno,
                            color = Eixo.cores.texto,
                        )
                    },
                )
            }
        }
    }

    // Diálogo para Iniciar Viagem
    if (mostrarDialogoIniciar) {
        EixoDialogoConfirmacao(
            titulo = "Iniciar viagem #${viagem.numero}?",
            texto = "A viagem passará para o estado 'Em andamento' com o KM inicial registrado (${formatarKm(kmInicialSugestao)} km).",
            textoConfirmar = "Confirmar início",
            aoConfirmar = {
                Dados.viagens.salvar(
                    viagem.copy(
                        status = StatusViagem.EM_ANDAMENTO,
                        kmInicial = kmInicialSugestao,
                    ),
                )
                if (caminhao != null && kmInicialSugestao > caminhao.kmAtual) {
                    Dados.caminhoes.salvar(caminhao.copy(kmAtual = kmInicialSugestao))
                }
                mostrarDialogoIniciar = false
            },
            aoCancelar = { mostrarDialogoIniciar = false },
        )
    }

    // Diálogo para Finalizar Viagem
    if (mostrarDialogoFinalizar) {
        EixoDialogoConfirmacao(
            titulo = "Finalizar viagem #${viagem.numero}?",
            texto = "A viagem será concluída com o KM final registrado (${formatarKm(kmFinalSugestao)} km). Verifique se todos os abastecimentos foram lançados.",
            textoConfirmar = "Finalizar viagem",
            aoConfirmar = {
                Dados.viagens.salvar(
                    viagem.copy(
                        status = StatusViagem.FINALIZADA,
                        kmFinal = kmFinalSugestao,
                    ),
                )
                if (caminhao != null && kmFinalSugestao > caminhao.kmAtual) {
                    Dados.caminhoes.salvar(caminhao.copy(kmAtual = kmFinalSugestao))
                }
                mostrarDialogoFinalizar = false
            },
            aoCancelar = { mostrarDialogoFinalizar = false },
        )
    }
}
