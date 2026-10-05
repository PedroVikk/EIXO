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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTipo

/**
 * Alerta com faixa de severidade de 3dp à esquerda: a cor dá a gravidade antes da leitura.
 * Sempre nomeie o objeto no título ou na descrição (placa, viagem, motorista) —
 * alerta genérico não gera ação.
 */
@Composable
fun EixoAlerta(
    titulo: String,
    icone: ImageVector,
    modifier: Modifier = Modifier,
    descricao: String? = null,
    tom: Tom = Tom.Acao,
    aoClicar: (() -> Unit)? = null,
) {
    val c = Eixo.cores
    val cores = tom.cores(c)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(EixoRaio.md)
            .background(c.superficie)
            .border(1.dp, c.borda, EixoRaio.md)
            .drawBehind { drawRect(cores.texto, size = Size(3.dp.toPx(), size.height)) }
            .then(if (aoClicar != null) Modifier.clickable(onClick = aoClicar) else Modifier)
            .padding(start = 13.dp, end = 13.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            Modifier.size(28.dp).clip(EixoRaio.sm).background(cores.fundo),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = cores.texto, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(titulo, style = EixoTipo.corpoForte.copy(fontSize = 13.5.sp, lineHeight = 18.sp), color = c.texto)
            if (descricao != null) {
                Text(descricao, style = EixoTipo.apoio.copy(fontSize = 12.sp), color = c.textoFraco, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

/**
 * Linha de lista. O padrão de toda listagem do app (viagens, cargas, motoristas, abastecimentos).
 *
 * @param inicio normalmente EixoPlaca ou um ícone.
 * @param fim normalmente EixoEtiqueta. Se nulo e houver clique, mostra uma seta.
 * @param destaque pinta a borda — use Tom.Critico para pendência que precisa subir aos olhos.
 */
@Composable
fun EixoLinhaLista(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    apoio: String? = null,
    tomApoio: Tom? = null,
    inicio: (@Composable () -> Unit)? = null,
    fim: (@Composable () -> Unit)? = null,
    destaque: Tom? = null,
    aoClicar: (() -> Unit)? = null,
) {
    val c = Eixo.cores
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(EixoRaio.md)
            .background(c.superficie)
            .border(1.dp, destaque?.cores(c)?.borda ?: c.borda, EixoRaio.md)
            .then(if (aoClicar != null) Modifier.clickable(onClick = aoClicar) else Modifier)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        inicio?.invoke()
        // Com três linhas, a etiqueta desce para a última: título e subtítulo ganham a largura
        // toda. Com placa + etiqueta lado a lado sobram ~126dp, e uma rota como
        // "Goiânia → São Paulo" seria cortada.
        val etiquetaEmbaixo = fim != null && subtitulo != null && apoio != null
        Column(Modifier.weight(1f)) {
            Text(titulo, style = EixoTipo.corpoForte.copy(fontSize = 14.sp), color = c.texto, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitulo != null) {
                Text(subtitulo, style = EixoTipo.apoio.copy(fontSize = 11.5.sp), color = c.textoFraco, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
            }
            if (apoio != null) {
                Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        apoio, style = EixoTipo.apoio.copy(fontSize = 11.5.sp), color = tomApoio?.cores(c)?.texto ?: c.textoFraco,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                    )
                    if (etiquetaEmbaixo) Box(Modifier.padding(start = 8.dp)) { fim!!() }
                }
            }
        }
        when {
            fim != null && !etiquetaEmbaixo -> fim()
            fim == null && aoClicar != null -> Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = c.textoTenue)
        }
    }
}

/** Linha de "rótulo ............ valor" dentro de um cartão de resumo. */
@Composable
fun EixoLinhaValor(rotulo: String, valor: String, modifier: Modifier = Modifier, numerico: Boolean = true, ultima: Boolean = false) {
    val c = Eixo.cores
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (!ultima) Modifier.drawBehind {
                drawRect(c.borda, topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 1.dp.toPx()), size = Size(size.width, 1.dp.toPx()))
            } else Modifier)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(rotulo, style = EixoTipo.apoio, color = c.textoFraco, modifier = Modifier.weight(1f))
        Text(valor, style = if (numerico) EixoTipo.dadoPequeno else EixoTipo.corpo.copy(fontSize = 13.sp), color = c.texto)
    }
}
