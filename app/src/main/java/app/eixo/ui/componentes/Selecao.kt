package app.eixo.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoAlvo
import app.eixo.ui.design.EixoEspaco
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTipo

/**
 * Seletor de uma opção numa lista (motorista, caminhão, carga, combustível...).
 * Abre uma folha inferior em vez de um menu suspenso: alvo maior, lista rolável,
 * e cabe o detalhe de cada opção.
 *
 * Exemplo:
 * ```
 * EixoSeletor(
 *     rotulo = "Motorista",
 *     opcoes = motoristas,
 *     selecionado = motoristaEscolhido,
 *     aoSelecionar = { motoristaEscolhido = it },
 *     textoDe = { it.nome },
 *     detalheDe = { "CNH ${it.categoriaCnh}" },
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EixoSeletor(
    rotulo: String,
    opcoes: List<T>,
    selecionado: T?,
    aoSelecionar: (T) -> Unit,
    modifier: Modifier = Modifier,
    textoDe: (T) -> String = { it.toString() },
    detalheDe: ((T) -> String?)? = null,
    placeholder: String = "Selecionar",
    dica: String? = null,
    erro: String? = null,
    habilitado: Boolean = true,
) {
    val c = Eixo.cores
    var aberto by rememberSaveable { mutableStateOf(false) }

    MolduraCampo(rotulo, modifier, dica = dica, erro = erro) {
        CaixaCampo(
            focado = aberto,
            temErro = erro != null,
            somenteLeitura = !habilitado,
            grande = false,
            aoClicar = if (habilitado) ({ aberto = true }) else null,
            descricao = "$rotulo: ${selecionado?.let(textoDe) ?: placeholder}",
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = selecionado?.let(textoDe) ?: placeholder,
                    style = EixoTipo.campo,
                    color = if (selecionado == null) c.textoTenue else c.texto,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val detalhe = selecionado?.let { detalheDe?.invoke(it) }
                if (detalhe != null) {
                    Text(detalhe, style = EixoTipo.apoio.copy(fontSize = 11.5.sp), color = c.textoFraco, maxLines = 1)
                }
            }
            Icon(Icons.Outlined.UnfoldMore, contentDescription = null, tint = c.textoTenue, modifier = Modifier.size(20.dp))
        }
    }

    if (aberto) {
        ModalBottomSheet(
            onDismissRequest = { aberto = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.superficie,
            shape = EixoRaio.xl,
        ) {
            Column(Modifier.padding(horizontal = EixoEspaco.margemTela).navigationBarsPadding()) {
                Text(rotulo, style = EixoTipo.tituloSecao, color = c.texto)
                Text(
                    if (opcoes.isEmpty()) "Nenhuma opção cadastrada." else "${opcoes.size} opções",
                    style = EixoTipo.apoio, color = c.textoFraco,
                    modifier = Modifier.padding(top = 2.dp, bottom = EixoEspaco.s3),
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(EixoEspaco.s2), modifier = Modifier.padding(bottom = EixoEspaco.s4)) {
                    items(opcoes) { opcao ->
                        val marcado = opcao == selecionado
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = EixoAlvo.normal)
                                .clip(EixoRaio.md)
                                .background(if (marcado) c.acaoSuave else c.superficie2)
                                .border(1.5.dp, if (marcado) c.acao else c.borda, EixoRaio.md)
                                .clickable(role = Role.RadioButton) {
                                    aoSelecionar(opcao)
                                    aberto = false
                                }
                                .semantics { selected = marcado }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(textoDe(opcao), style = EixoTipo.corpoForte, color = if (marcado) c.acao else c.texto)
                                detalheDe?.invoke(opcao)?.let {
                                    Text(it, style = EixoTipo.apoio, color = c.textoFraco)
                                }
                            }
                            if (marcado) Icon(Icons.Outlined.Check, contentDescription = null, tint = c.acao)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Escolha entre poucas opções lado a lado (2 ou 3). Grande o bastante para o polegar.
 * @param tomSelecionado use Tom.Sucesso quando a opção marcada é a "boa" (ex.: tanque cheio = Sim).
 */
@Composable
fun EixoSegmentado(
    opcoes: List<String>,
    selecionado: Int?,
    aoSelecionar: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tomSelecionado: Tom = Tom.Acao,
) {
    val c = Eixo.cores
    val corSel = tomSelecionado.cores(c)
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
        opcoes.forEachIndexed { indice, texto ->
            val marcado = indice == selecionado
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = EixoAlvo.normal)
                    .clip(EixoRaio.md)
                    .background(if (marcado) corSel.fundo else c.superficie)
                    .border(1.5.dp, if (marcado) corSel.texto else c.borda, EixoRaio.md)
                    .clickable(role = Role.RadioButton) { aoSelecionar(indice) }
                    .semantics { selected = marcado },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (marcado) Icon(Icons.Outlined.Check, contentDescription = null, tint = corSel.texto, modifier = Modifier.size(18.dp))
                    Text(texto, style = EixoTipo.corpoForte, color = if (marcado) corSel.texto else c.textoFraco)
                }
            }
        }
    }
}

/** Pergunta de sim ou não com rótulo — ex.: "Tanque cheio?". */
@Composable
fun EixoSimNao(
    rotulo: String,
    valor: Boolean?,
    aoMudar: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    dica: String? = null,
    erro: String? = null,
    simEhPositivo: Boolean = true,
) {
    MolduraCampo(rotulo, modifier, dica = dica, erro = erro) {
        EixoSegmentado(
            opcoes = listOf("Sim", "Não"),
            selecionado = when (valor) { true -> 0; false -> 1; null -> null },
            aoSelecionar = { aoMudar(it == 0) },
            tomSelecionado = if (simEhPositivo && valor == true) Tom.Sucesso else Tom.Acao,
        )
    }
}
