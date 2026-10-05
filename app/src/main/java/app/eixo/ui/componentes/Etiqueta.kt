package app.eixo.ui.componentes

import android.provider.Settings
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoCores
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTipo

/**
 * Tom semântico. Verde, âmbar e vermelho só aparecem através daqui —
 * nunca como cor de marca ou decoração.
 */
enum class Tom { Neutro, Acao, Sucesso, Atencao, Critico }

internal data class CoresTom(val texto: Color, val fundo: Color, val borda: Color)

internal fun Tom.cores(c: EixoCores): CoresTom = when (this) {
    Tom.Neutro -> CoresTom(c.textoFraco, c.superficie2, c.borda)
    Tom.Acao -> CoresTom(c.acao, c.acaoSuave, c.acao.copy(alpha = 0.26f))
    Tom.Sucesso -> CoresTom(c.normal, c.normalSuave, c.normal.copy(alpha = 0.26f))
    Tom.Atencao -> CoresTom(c.atencao, c.atencaoSuave, c.atencao.copy(alpha = 0.30f))
    Tom.Critico -> CoresTom(c.critico, c.criticoSuave, c.critico.copy(alpha = 0.30f))
}

/** Respeita a opção do sistema "remover animações". */
@Composable
internal fun animacoesReduzidas(): Boolean {
    val contexto = LocalContext.current
    return remember {
        Settings.Global.getFloat(contexto.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/**
 * Etiqueta de status. Texto SEMPRE presente — estado nunca é comunicado só por cor.
 *
 * @param pulsando use apenas para estado que muda sozinho (viagem em andamento).
 * @param compacta versão sem ponto, para ficar ao lado do rótulo de campo.
 */
@Composable
fun EixoEtiqueta(
    texto: String,
    modifier: Modifier = Modifier,
    tom: Tom = Tom.Neutro,
    pulsando: Boolean = false,
    compacta: Boolean = false,
) {
    val cores = tom.cores(Eixo.cores)
    val pulsoAtivo = pulsando && !animacoesReduzidas()
    val transicao = rememberInfiniteTransition(label = "pulso")
    val opacidade by transicao.animateFloat(
        initialValue = 1f,
        targetValue = if (pulsoAtivo) 0.3f else 1f,
        animationSpec = infiniteRepeatable(tween(950), RepeatMode.Reverse),
        label = "opacidadePulso",
    )
    Row(
        modifier = modifier
            .clip(if (compacta) EixoRaio.xs else EixoRaio.pilula)
            .background(cores.fundo)
            .border(1.dp, cores.borda, if (compacta) EixoRaio.xs else EixoRaio.pilula)
            .padding(horizontal = if (compacta) 6.dp else 9.dp, vertical = if (compacta) 2.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (!compacta) {
            Box(
                Modifier
                    .size(6.dp)
                    .alpha(opacidade)
                    .clip(CircleShape)
                    .background(cores.texto),
            )
        }
        Text(
            texto.uppercase(),
            style = EixoTipo.microRotulo.copy(fontSize = if (compacta) 9.sp else 9.5.sp),
            color = cores.texto,
            maxLines = 1,
        )
    }
}
