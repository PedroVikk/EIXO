package app.eixo.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoEspaco
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTipo

/**
 * Cabeçalho petróleo, igual nos dois temas — é a âncora da marca.
 *
 * @param sobretitulo linha pequena acima do título (ex.: "Controle da frota").
 * @param aoVoltar mostra a seta de voltar; omita nas telas principais da barra inferior.
 * @param acoes botões à direita — use EixoBotaoCabecalho.
 * @param conteudo o que vai embaixo do título (odômetro, rota, filtros).
 */
@Composable
fun EixoCabecalho(
    titulo: String,
    modifier: Modifier = Modifier,
    sobretitulo: String? = null,
    aoVoltar: (() -> Unit)? = null,
    acoes: @Composable RowScope.() -> Unit = {},
    conteudo: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val c = Eixo.cores
    Column(
        modifier
            .fillMaxWidth()
            .background(c.cabecalho)
            .statusBarsPadding()
            .padding(bottom = if (conteudo != null) EixoEspaco.s4 else EixoEspaco.s3),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = EixoEspaco.margemTela, end = EixoEspaco.margemTela, top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s3),
        ) {
            if (aoVoltar != null) EixoBotaoCabecalho(Icons.AutoMirrored.Outlined.ArrowBack, "Voltar", aoVoltar)
            Column(Modifier.weight(1f)) {
                if (sobretitulo != null) {
                    Text(sobretitulo.uppercase(), style = EixoTipo.microRotulo, color = c.cabecalhoRotulo)
                }
                Text(titulo, style = EixoTipo.tituloSecao.copy(fontSize = if (aoVoltar == null) 20.sp else 17.sp), color = c.cabecalhoTexto, maxLines = 1)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2), content = acoes)
        }
        if (conteudo != null) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = EixoEspaco.margemTela).padding(top = EixoEspaco.s3),
                verticalArrangement = Arrangement.spacedBy(EixoEspaco.s2),
                content = conteudo,
            )
        }
    }
}

/** Botão quadrado de ícone para o cabeçalho (voltar, filtrar, mais opções). */
@Composable
fun EixoBotaoCabecalho(icone: ImageVector, descricao: String, aoClicar: () -> Unit) {
    val c = Eixo.cores
    Box(
        Modifier
            .size(36.dp)
            .clip(EixoRaio.sm)
            .border(1.dp, c.cabecalhoBorda, EixoRaio.sm)
            .clickable(role = Role.Button, onClick = aoClicar)
            .semantics { contentDescription = descricao },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, contentDescription = null, tint = c.cabecalhoTexto, modifier = Modifier.size(19.dp))
    }
}

/**
 * Esqueleto de TODA tela do app. Garante fundo, margens, espaçamento entre blocos,
 * teclado empurrando o rodapé e as barras do sistema tratadas.
 *
 * ```
 * EixoTela(
 *     cabecalho = { EixoCabecalho("Nova carga", aoVoltar = voltar) },
 *     rodape = { EixoRodapeAcao { EixoBotao("Salvar", aoClicar = salvar) } },
 * ) {
 *     EixoCampo(...)
 *     EixoCampo(...)
 * }
 * ```
 * Para lista longa, passe `rolavel = false` e use uma LazyColumn com `Modifier.weight(1f)`.
 */
@Composable
fun EixoTela(
    cabecalho: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    rodape: (@Composable () -> Unit)? = null,
    barraInferior: (@Composable () -> Unit)? = null,
    rolavel: Boolean = true,
    espacamento: Dp = EixoEspaco.s3,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    val c = Eixo.cores
    Column(modifier.fillMaxSize().background(c.fundo).imePadding()) {
        cabecalho()
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(if (rolavel) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(EixoEspaco.margemTela)
                // sem rodapé nem barra, o próprio conteúdo desvia da barra de navegação
                .then(if (rodape == null && barraInferior == null) Modifier.navigationBarsPadding() else Modifier),
            verticalArrangement = Arrangement.spacedBy(espacamento),
            content = conteudo,
        )
        if (rodape != null) {
            // Com barra inferior embaixo, quem trata a barra do sistema é ela, não o rodapé.
            Box(if (barraInferior != null) Modifier.consumeWindowInsets(WindowInsets.navigationBars) else Modifier) {
                rodape()
            }
        }
        barraInferior?.invoke()
    }
}

/** Área fixa no pé da tela para a ação principal. Fica na zona do polegar. */
@Composable
fun EixoRodapeAcao(modifier: Modifier = Modifier, conteudo: @Composable ColumnScope.() -> Unit) {
    val c = Eixo.cores
    Column(
        modifier
            .fillMaxWidth()
            .background(c.superficie)
            .drawBehind { drawRect(c.borda, size = Size(size.width, 1.dp.toPx())) }
            .navigationBarsPadding()
            .padding(horizontal = EixoEspaco.margemTela, vertical = EixoEspaco.s3),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = conteudo,
    )
}

/** Texto pequeno de apoio sob um botão — ex.: por que ele está desabilitado. */
@Composable
fun EixoNotaRodape(texto: String) {
    Text(texto.uppercase(), style = EixoTipo.microRotulo.copy(fontSize = 9.5.sp), color = Eixo.cores.textoTenue, textAlign = TextAlign.Center)
}

data class ItemBarra(val rota: String, val rotulo: String, val icone: ImageVector)

/** Barra inferior. Gestor tem até 5 destinos; motorista, 3. Nunca a mesma barra para os dois. */
@Composable
fun EixoBarraInferior(
    itens: List<ItemBarra>,
    rotaAtual: String?,
    aoNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Eixo.cores
    Row(
        modifier
            .fillMaxWidth()
            .background(c.superficie)
            .drawBehind { drawRect(c.borda, size = Size(size.width, 1.dp.toPx())) }
            .navigationBarsPadding()
            .padding(horizontal = 4.dp, vertical = 6.dp),
    ) {
        itens.forEach { item ->
            val ativo = item.rota == rotaAtual
            val cor = if (ativo) c.acao else c.textoTenue
            Column(
                Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clip(EixoRaio.md)
                    .clickable(role = Role.Tab) { if (!ativo) aoNavegar(item.rota) }
                    .semantics { selected = ativo },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(item.icone, contentDescription = null, tint = cor, modifier = Modifier.size(24.dp))
                Text(item.rotulo, style = EixoTipo.rotulo.copy(fontSize = 10.5.sp), color = cor, maxLines = 1, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

/** Estado vazio: ícone, título, explicação e, se fizer sentido, a ação que resolve. */
@Composable
fun EixoEstadoVazio(
    icone: ImageVector,
    titulo: String,
    modifier: Modifier = Modifier,
    descricao: String? = null,
    acao: (@Composable () -> Unit)? = null,
) {
    val c = Eixo.cores
    Column(
        modifier.fillMaxWidth().padding(vertical = EixoEspaco.s8, horizontal = EixoEspaco.s4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.size(60.dp).clip(EixoRaio.pilula).background(c.superficie2).border(1.dp, c.borda, EixoRaio.pilula),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = c.textoTenue, modifier = Modifier.size(28.dp))
        }
        Text(titulo, style = EixoTipo.tituloSecao, color = c.texto, textAlign = TextAlign.Center)
        if (descricao != null) Text(descricao, style = EixoTipo.apoio, color = c.textoFraco, textAlign = TextAlign.Center)
        if (acao != null) Box(Modifier.padding(top = EixoEspaco.s2)) { acao() }
    }
}

/**
 * Confirmação antes de ação irreversível (excluir, cancelar viagem).
 * O botão diz exatamente o que acontece: "Excluir carga", não "OK".
 */
@Composable
fun EixoDialogoConfirmacao(
    titulo: String,
    texto: String,
    textoConfirmar: String,
    aoConfirmar: () -> Unit,
    aoCancelar: () -> Unit,
    perigoso: Boolean = false,
) {
    val c = Eixo.cores
    AlertDialog(
        onDismissRequest = aoCancelar,
        containerColor = c.superficie,
        shape = EixoRaio.xl,
        title = { Text(titulo, style = EixoTipo.tituloSecao, color = c.texto) },
        text = { Text(texto, style = EixoTipo.corpo, color = c.textoFraco) },
        confirmButton = {
            EixoBotao(
                textoConfirmar, aoClicar = aoConfirmar, larguraTotal = false, tamanho = TamanhoBotao.Pequeno,
                variante = if (perigoso) VarianteBotao.Perigo else VarianteBotao.Primario,
            )
        },
        dismissButton = {
            EixoBotao("Cancelar", aoClicar = aoCancelar, larguraTotal = false, tamanho = TamanhoBotao.Pequeno, variante = VarianteBotao.Secundario)
        },
    )
}
