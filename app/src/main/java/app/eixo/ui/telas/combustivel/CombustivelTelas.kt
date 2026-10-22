package app.eixo.ui.telas.combustivel

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.eixo.dados.Abastecimento
import app.eixo.dados.Dados
import app.eixo.dados.Perfil
import app.eixo.dados.Sessao
import app.eixo.dados.TipoCombustivel
import app.eixo.ui.componentes.EixoBotao
import app.eixo.ui.componentes.EixoBotaoCabecalho
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoCampo
import app.eixo.ui.componentes.EixoCampoCalculado
import app.eixo.ui.componentes.EixoCampoDataHora
import app.eixo.ui.componentes.EixoCampoDinheiro
import app.eixo.ui.componentes.EixoCampoKm
import app.eixo.ui.componentes.EixoCampoLitros
import app.eixo.ui.componentes.EixoDialogoConfirmacao
import app.eixo.ui.componentes.EixoEstadoVazio
import app.eixo.ui.componentes.EixoEtiqueta
import app.eixo.ui.componentes.EixoLinhaLista
import app.eixo.ui.componentes.EixoRodapeAcao
import app.eixo.ui.componentes.EixoRotuloSecao
import app.eixo.ui.componentes.EixoSeletor
import app.eixo.ui.componentes.EixoSimNao
import app.eixo.ui.componentes.EixoTela
import app.eixo.ui.componentes.Tom
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoEspaco
import app.eixo.ui.design.EixoRaio
import app.eixo.util.decimalParaDigitos
import app.eixo.util.digitosParaDecimal
import app.eixo.util.digitosParaKm
import app.eixo.util.formatarDiaMesHora
import app.eixo.util.formatarKm
import app.eixo.util.formatarLitros
import app.eixo.util.formatarMoeda
import java.time.LocalDateTime

/*
 * MÓDULO COMBUSTÍVEL — registro de abastecimento.
 *
 * Lista: motorista vê só os abastecimentos dele; administrador vê todos.
 * Formulário: mesma tela para criar (abastecimentoId == null) e editar.
 * ARLA é opcional e nunca entra no cálculo de km/L (regra do modelo).
 */

// ---------------------------------------------------------------------------
//  LISTA DE ABASTECIMENTOS
// ---------------------------------------------------------------------------

@Composable
fun AbastecimentosTela(
    barra: @Composable () -> Unit,
    aoNovo: () -> Unit,
    aoAbrir: (abastecimentoId: String) -> Unit,
) {
    val c = Eixo.cores
    val todos by Dados.abastecimentos.itens.collectAsState()
    val caminhoes by Dados.caminhoes.itens.collectAsState()
    val usuario by Sessao.usuario.collectAsState()
    var busca by rememberSaveable { mutableStateOf("") }
    var detalheId by rememberSaveable { mutableStateOf<String?>(null) }

    // Motorista só enxerga os próprios registros.
    val visiveis = todos.filter { usuario?.perfil != Perfil.MOTORISTA || it.motoristaId == usuario?.id }

    fun placaDe(caminhaoId: String): String = caminhoes.firstOrNull { it.id == caminhaoId }?.placa ?: "—"

    val filtrados = visiveis
        .filter {
            busca.isBlank() ||
                placaDe(it.caminhaoId).contains(busca.trim(), ignoreCase = true) ||
                it.posto.contains(busca.trim(), ignoreCase = true)
        }
        .sortedByDescending { it.dataHora }

    // Detalhe aberto dentro do próprio módulo: não depende de rota nova na navegação.
    val aberto = detalheId
    if (aberto != null) {
        BackHandler { detalheId = null }
        AbastecimentoDetalheTela(
            abastecimentoId = aberto,
            aoVoltar = { detalheId = null },
            aoEditar = { aoAbrir(aberto) },
        )
        return
    }

    EixoTela(
        cabecalho = { EixoCabecalho("Combustível", sobretitulo = "Histórico") },
        rodape = { EixoRodapeAcao { EixoBotao("Novo abastecimento", aoClicar = aoNovo, icone = Icons.Outlined.Add) } },
        barraInferior = barra,
        rolavel = false,
    ) {
        EixoCampo(
            valor = busca,
            aoMudar = { busca = it },
            rotulo = "Buscar",
            placeholder = "Placa ou posto",
            teclado = KeyboardOptions(imeAction = ImeAction.Search),
            iconeFinal = { Icon(Icons.Outlined.Search, contentDescription = null, tint = c.textoTenue) },
        )
        EixoRotuloSecao(
            if (busca.isNotBlank()) "${filtrados.size} de ${visiveis.size} abastecimentos"
            else "${visiveis.size} abastecimentos",
        )

        if (filtrados.isEmpty()) {
            EixoEstadoVazio(
                icone = Icons.Outlined.LocalGasStation,
                titulo = if (visiveis.isEmpty()) "Nenhum abastecimento registrado" else "Nada encontrado",
                descricao = if (visiveis.isEmpty()) "Registre o primeiro abastecimento para acompanhar o consumo."
                else "Nenhum abastecimento com \"$busca\" na placa ou no posto.",
            )
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
                items(filtrados, key = { it.id }) { a ->
                    EixoLinhaLista(
                        titulo = "${placaDe(a.caminhaoId)} · ${formatarLitros(a.litros)}",
                        subtitulo = "${formatarDiaMesHora(a.dataHora)} · ${a.posto.ifBlank { "—" }}",
                        apoio = "${formatarKm(a.km)} km · ${formatarMoeda(a.valorTotal)} · ${a.combustivel.rotulo}",
                        inicio = {
                            Box(
                                Modifier.size(38.dp).clip(EixoRaio.sm).background(c.superficie2),
                                contentAlignment = Alignment.Center,
                            ) { Icon(Icons.Outlined.LocalGasStation, contentDescription = null, tint = c.textoFraco, modifier = Modifier.size(20.dp)) }
                        },
                        fim = if (a.tanqueCheio) {
                            { EixoEtiqueta("Tanque cheio", tom = Tom.Sucesso) }
                        } else null,
                        aoClicar = { detalheId = a.id },
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
//  FORMULÁRIO DE ABASTECIMENTO
// ---------------------------------------------------------------------------

/** `viagemId` vem preenchido quando o registro é aberto a partir de uma viagem; pode ser nulo (avulso). */
@Composable
fun AbastecimentoFormTela(abastecimentoId: String?, viagemId: String?, aoVoltar: () -> Unit) {
    val existente = remember(abastecimentoId) { abastecimentoId?.let { Dados.abastecimentos.buscar(it) } }
    val viagem = remember(viagemId) { viagemId?.let { Dados.viagens.buscar(it) } }
    val caminhoes by Dados.caminhoes.itens.collectAsState()
    val usuarios by Dados.usuarios.itens.collectAsState()
    val usuario by Sessao.usuario.collectAsState()

    val motoristas = usuarios.filter { it.perfil == Perfil.MOTORISTA && it.ativo }
    val admin = usuario?.perfil == Perfil.ADMIN

    // Estado do formulário (sobrevive a girar a tela).
    var caminhaoId by rememberSaveable { mutableStateOf(existente?.caminhaoId ?: viagem?.caminhaoId) }
    var motoristaId by rememberSaveable {
        mutableStateOf(
            existente?.motoristaId ?: viagem?.motoristaId ?: usuario?.takeIf { it.perfil == Perfil.MOTORISTA }?.id,
        )
    }
    var kmDigitos by rememberSaveable { mutableStateOf(existente?.km?.toString() ?: "") }
    var litrosDigitos by rememberSaveable { mutableStateOf(existente?.litros?.let { decimalParaDigitos(it) } ?: "") }
    var valorDigitos by rememberSaveable { mutableStateOf(existente?.valorTotal?.let { decimalParaDigitos(it) } ?: "") }
    var combustivel by rememberSaveable { mutableStateOf<TipoCombustivel?>(existente?.combustivel ?: TipoCombustivel.DIESEL_S10) }
    var posto by rememberSaveable { mutableStateOf(existente?.posto ?: "") }
    var tanqueCheio by rememberSaveable { mutableStateOf<Boolean?>(existente?.tanqueCheio) }
    var usaArla by rememberSaveable { mutableStateOf<Boolean?>(existente?.let { it.arlaLitros != null } ?: false) }
    var arlaLitrosDigitos by rememberSaveable { mutableStateOf(existente?.arlaLitros?.let { decimalParaDigitos(it) } ?: "") }
    var arlaValorDigitos by rememberSaveable { mutableStateOf(existente?.arlaValor?.let { decimalParaDigitos(it) } ?: "") }
    var dataHora by remember { mutableStateOf<LocalDateTime?>(existente?.dataHora ?: LocalDateTime.now()) }
    var tentouSalvar by rememberSaveable { mutableStateOf(false) }
    var confirmarExclusao by rememberSaveable { mutableStateOf(false) }

    // Valores derivados.
    val caminhao = caminhoes.firstOrNull { it.id == caminhaoId }
    val sugerido = caminhaoId?.let { Dados.ultimoKm(it) }
    val km = digitosParaKm(kmDigitos)
    val litros = digitosParaDecimal(litrosDigitos)
    val valorTotal = digitosParaDecimal(valorDigitos)
    val arlaLitros = digitosParaDecimal(arlaLitrosDigitos)
    val arlaValor = digitosParaDecimal(arlaValorDigitos)
    val precoLitro = if (litros > 0 && valorTotal > 0) "${formatarMoeda(valorTotal / litros)}/L" else "—"

    // Validação: calculada sempre, exibida só depois da primeira tentativa de salvar.
    val erroCaminhao = if (caminhao == null) "Escolha o caminhão abastecido." else null
    val erroMotorista = if (motoristaId == null) "Escolha o motorista do abastecimento." else null
    val erroKm = when {
        km == null || km <= 0L -> "Informe o KM que aparece no tacógrafo."
        existente == null && sugerido != null && km < sugerido ->
            "O KM não pode ser menor que o último registrado (${formatarKm(sugerido)} km). Confira o tacógrafo."
        else -> null
    }
    val erroLitros = if (litros <= 0.0) "Informe os litros abastecidos, maior que zero." else null
    val erroValor = if (valorTotal <= 0.0) "Informe o valor total pago." else null
    val erroCombustivel = if (combustivel == null) "Escolha o tipo de combustível." else null
    val erroPosto = if (posto.isBlank()) "Informe o nome do posto." else null
    val erroTanque = if (tanqueCheio == null) "Informe se o tanque foi cheio." else null
    val erroDataHora = if (dataHora == null) "Informe quando o abastecimento aconteceu." else null
    val erroArlaLitros = if (usaArla == true && arlaLitros <= 0.0) "Informe os litros de ARLA." else null
    val erroArlaValor = if (usaArla == true && arlaValor <= 0.0) "Informe o valor pago pelo ARLA." else null

    val valido = listOf(
        erroCaminhao, erroMotorista, erroKm, erroLitros, erroValor, erroCombustivel,
        erroPosto, erroTanque, erroDataHora, erroArlaLitros, erroArlaValor,
    ).all { it == null }

    fun salvar() {
        tentouSalvar = true
        if (!valido) return
        val comArla = usaArla == true
        Dados.abastecimentos.salvar(
            Abastecimento(
                id = existente?.id ?: Dados.abastecimentos.novoId(),
                viagemId = existente?.viagemId ?: viagemId,
                caminhaoId = caminhaoId!!,
                motoristaId = motoristaId!!,
                dataHora = dataHora!!,
                km = km!!,
                litros = litros,
                valorTotal = valorTotal,
                combustivel = combustivel!!,
                posto = posto.trim(),
                tanqueCheio = tanqueCheio!!,
                arlaLitros = if (comArla) arlaLitros else null,
                arlaValor = if (comArla) arlaValor else null,
            ),
        )
        aoVoltar()
    }

    EixoTela(
        cabecalho = {
            EixoCabecalho(
                titulo = if (existente == null) "Novo abastecimento" else "Editar abastecimento",
                sobretitulo = "Combustível",
                aoVoltar = aoVoltar,
                acoes = {
                    if (existente != null && admin) {
                        EixoBotaoCabecalho(Icons.Outlined.DeleteOutline, "Excluir abastecimento") { confirmarExclusao = true }
                    }
                },
            )
        },
        rodape = {
            EixoRodapeAcao {
                EixoBotao(if (existente == null) "Registrar abastecimento" else "Salvar alterações", aoClicar = ::salvar)
            }
        },
    ) {
        EixoSeletor(
            rotulo = "Caminhão",
            opcoes = caminhoes,
            selecionado = caminhao,
            aoSelecionar = { caminhaoId = it.id },
            textoDe = { "${it.placa} · ${it.modelo}" },
            erro = if (tentouSalvar) erroCaminhao else null,
        )
        if (admin) {
            EixoSeletor(
                rotulo = "Motorista",
                opcoes = motoristas,
                selecionado = motoristas.firstOrNull { it.id == motoristaId },
                aoSelecionar = { motoristaId = it.id },
                textoDe = { it.nome },
                erro = if (tentouSalvar) erroMotorista else null,
            )
        }
        EixoCampoDataHora(
            rotulo = "Data e hora",
            dataHora = dataHora,
            aoSelecionar = { dataHora = it },
            erro = if (tentouSalvar) erroDataHora else null,
        )
        EixoCampoKm(
            digitos = kmDigitos,
            aoMudar = { kmDigitos = it },
            sugerido = sugerido,
            origemSugestao = "último registro do caminhão",
            erro = if (tentouSalvar) erroKm else null,
        )
        EixoSeletor(
            rotulo = "Combustível",
            opcoes = TipoCombustivel.entries,
            selecionado = combustivel,
            aoSelecionar = { combustivel = it },
            textoDe = { it.rotulo },
            erro = if (tentouSalvar) erroCombustivel else null,
        )
        EixoCampoLitros(
            digitos = litrosDigitos,
            aoMudar = { litrosDigitos = it },
            erro = if (tentouSalvar) erroLitros else null,
        )
        EixoCampoDinheiro(
            centavos = valorDigitos,
            aoMudar = { valorDigitos = it },
            rotulo = "Valor total",
            erro = if (tentouSalvar) erroValor else null,
        )
        EixoCampoCalculado(
            valor = precoLitro,
            rotulo = "Preço por litro",
            dica = "Valor total dividido pelos litros.",
        )
        EixoCampo(
            valor = posto,
            aoMudar = { posto = it },
            rotulo = "Posto",
            placeholder = "Ex.: Posto Ipiranga BR-153",
            erro = if (tentouSalvar) erroPosto else null,
            teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        )
        EixoSimNao(
            rotulo = "Encheu o tanque?",
            valor = tanqueCheio,
            aoMudar = { tanqueCheio = it },
            dica = "Usado no cálculo de km/L.",
            erro = if (tentouSalvar) erroTanque else null,
        )
        EixoSimNao(
            rotulo = "Abasteceu ARLA?",
            valor = usaArla,
            aoMudar = { usaArla = it },
            dica = "Entra no custo por km, mas não no consumo.",
        )
        if (usaArla == true) {
            EixoCampoLitros(
                digitos = arlaLitrosDigitos,
                aoMudar = { arlaLitrosDigitos = it },
                rotulo = "Litros de ARLA",
                erro = if (tentouSalvar) erroArlaLitros else null,
            )
            EixoCampoDinheiro(
                centavos = arlaValorDigitos,
                aoMudar = { arlaValorDigitos = it },
                rotulo = "Valor do ARLA",
                erro = if (tentouSalvar) erroArlaValor else null,
            )
        }
    }

    if (confirmarExclusao && existente != null) {
        EixoDialogoConfirmacao(
            titulo = "Excluir este abastecimento?",
            texto = "O registro sai do histórico e deixa de contar no consumo. Essa ação não pode ser desfeita.",
            textoConfirmar = "Excluir abastecimento",
            perigoso = true,
            aoConfirmar = {
                Dados.abastecimentos.remover(existente.id)
                confirmarExclusao = false
                aoVoltar()
            },
            aoCancelar = { confirmarExclusao = false },
        )
    }
}