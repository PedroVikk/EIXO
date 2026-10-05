package app.eixo.ui.componentes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoAlvo
import app.eixo.ui.design.EixoEspaco
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTipo

enum class VarianteBotao { Primario, Secundario, Fantasma, Sucesso, Perigo }

enum class TamanhoBotao(val altura: Dp) {
    Pequeno(EixoAlvo.pequeno),
    Normal(EixoAlvo.normal),
    /** Ação principal do fluxo do motorista. */
    Grande(EixoAlvo.grande),
}

/**
 * Botão do Eixo. Texto sempre em caixa-alta — de relance, a palavra vira forma.
 *
 * Regras de uso:
 *  - No máximo UM botão Primario por tela.
 *  - Desabilitado precisa de um texto logo abaixo explicando o porquê.
 *  - Ocupa a largura toda por padrão; use `larguraTotal = false` dentro de diálogos.
 */
@Composable
fun EixoBotao(
    texto: String,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    variante: VarianteBotao = VarianteBotao.Primario,
    tamanho: TamanhoBotao = TamanhoBotao.Normal,
    icone: ImageVector? = null,
    habilitado: Boolean = true,
    carregando: Boolean = false,
    larguraTotal: Boolean = true,
) {
    val c = Eixo.cores
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val escala by animateFloatAsState(if (pressionado && habilitado) 0.97f else 1f, label = "escalaBotao")

    val fundo: Color
    val conteudo: Color
    val borda: Color
    when {
        !habilitado -> { fundo = c.superficie2; conteudo = c.textoTenue; borda = c.borda }
        variante == VarianteBotao.Primario -> { fundo = c.acao; conteudo = c.sobreAcao; borda = Color.Transparent }
        variante == VarianteBotao.Secundario -> { fundo = Color.Transparent; conteudo = c.texto; borda = c.borda }
        variante == VarianteBotao.Fantasma -> { fundo = c.superficie2; conteudo = c.texto; borda = c.borda }
        variante == VarianteBotao.Sucesso -> { fundo = c.normal; conteudo = c.sobreNormal; borda = Color.Transparent }
        else -> { fundo = c.critico; conteudo = Color.White; borda = Color.Transparent }
    }
    // Só o primário ganha sombra, e na cor da própria ação: o botão parece aceso.
    val comSombra = habilitado && variante == VarianteBotao.Primario
    val tamanhoTexto = when (tamanho) {
        TamanhoBotao.Pequeno -> 12.sp
        TamanhoBotao.Normal -> 14.sp
        TamanhoBotao.Grande -> 15.sp
    }

    Box(
        modifier = modifier
            .then(if (larguraTotal) Modifier.fillMaxWidth() else Modifier)
            .graphicsLayer { scaleX = escala; scaleY = escala }
            .then(if (comSombra) Modifier.shadow(10.dp, EixoRaio.md, ambientColor = c.acao, spotColor = c.acao) else Modifier)
            .clip(EixoRaio.md)
            .background(fundo)
            .border(1.dp, borda, EixoRaio.md)
            .clickable(
                interactionSource = interacao,
                indication = ripple(color = conteudo),
                enabled = habilitado && !carregando,
                role = Role.Button,
                onClick = aoClicar,
            )
            .heightIn(min = tamanho.altura)
            .padding(horizontal = EixoEspaco.s4),
        contentAlignment = Alignment.Center,
    ) {
        if (carregando) {
            CircularProgressIndicator(Modifier.size(20.dp), color = conteudo, strokeWidth = 2.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                if (icone != null) {
                    Icon(icone, contentDescription = null, tint = conteudo,
                        modifier = Modifier.size(if (tamanho == TamanhoBotao.Grande) 22.dp else 18.dp))
                }
                Text(texto.uppercase(), style = EixoTipo.botao.copy(fontSize = tamanhoTexto), color = conteudo, maxLines = 1)
            }
        }
    }
}
