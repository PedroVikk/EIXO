package app.eixo.ui.componentes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eixo.dominio.Classificacao
import app.eixo.dominio.classificarConsumo
import app.eixo.dominio.desvioPercentual
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoEspaco
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTipo
import app.eixo.util.formatarDecimal
import app.eixo.util.formatarKm

/**
 * Cartão base: superfície + borda de 1dp. Hierarquia por borda, não por sombra —
 * sombra suave some com a tela sob sol direto.
 *
 * @param destaque pinta a borda com o tom (ex.: Tom.Critico para uma pendência).
 */
@Composable
fun EixoCartao(
    modifier: Modifier = Modifier,
    aoClicar: (() -> Unit)? = null,
    destaque: Tom? = null,
    preenchimento: Dp = EixoEspaco.s4,
    espacamento: Dp = 0.dp,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    val c = Eixo.cores
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(EixoRaio.lg)
            .background(c.superficie)
            .border(1.dp, destaque?.cores(c)?.borda ?: c.borda, EixoRaio.lg)
            .then(if (aoClicar != null) Modifier.clickable(onClick = aoClicar) else Modifier)
            .padding(preenchimento),
        verticalArrangement = Arrangement.spacedBy(espacamento),
        content = conteudo,
    )
}

/** Rótulo de seção em caixa-alta e monoespaçado. Com ação opcional à direita ("Ver todos"). */
@Composable
fun EixoRotuloSecao(
    texto: String,
    modifier: Modifier = Modifier,
    acao: String? = null,
    aoClicarAcao: (() -> Unit)? = null,
) {
    val c = Eixo.cores
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(texto.uppercase(), style = EixoTipo.microRotulo, color = c.textoTenue, modifier = Modifier.weight(1f))
        if (acao != null && aoClicarAcao != null) {
            Text(acao, style = EixoTipo.rotulo, color = c.acao, modifier = Modifier.clickable(onClick = aoClicarAcao).padding(4.dp))
        }
    }
}

/** Placa do veículo: mono, maiúscula, dentro de uma moldura. Âncora de leitura em lista. */
@Composable
fun EixoPlaca(placa: String, modifier: Modifier = Modifier, sobreEscuro: Boolean = false) {
    val c = Eixo.cores
    Text(
        text = placa.uppercase(),
        style = EixoTipo.dadoPequeno.copy(fontSize = 11.5.sp),
        color = if (sobreEscuro) c.cabecalhoTexto else c.texto,
        modifier = modifier
            .clip(EixoRaio.xs)
            .background(if (sobreEscuro) Color.White.copy(alpha = 0.08f) else c.superficie2)
            .border(1.dp, if (sobreEscuro) c.cabecalhoBorda else c.borda, EixoRaio.xs)
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

/** Indicador: rótulo pequeno, valor monoespaçado, linha de apoio. */
@Composable
fun EixoKpi(
    rotulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    apoio: String? = null,
    tomValor: Tom? = null,
    conteudoInferior: (@Composable () -> Unit)? = null,
) {
    val c = Eixo.cores
    EixoCartao(modifier, preenchimento = EixoEspaco.s3) {
        Text(rotulo.uppercase(), style = EixoTipo.microRotulo, color = c.textoTenue)
        Text(valor, style = EixoTipo.dado, color = tomValor?.cores(c)?.texto ?: c.texto, modifier = Modifier.padding(top = 6.dp))
        if (apoio != null) Text(apoio, style = EixoTipo.apoio.copy(fontSize = 11.sp), color = c.textoFraco, modifier = Modifier.padding(top = 3.dp))
        conteudoInferior?.invoke()
    }
}

data class NumeroResumo(val valor: String, val rotulo: String, val tom: Tom? = null)

/** Faixa de 2 a 4 números lado a lado — ex.: caminhões por situação. */
@Composable
fun EixoResumoNumeros(itens: List<NumeroResumo>, modifier: Modifier = Modifier) {
    val c = Eixo.cores
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        itens.forEach { item ->
            val tom = item.tom?.cores(c)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(EixoRaio.md)
                    .background(tom?.fundo ?: c.superficie)
                    .border(1.dp, tom?.borda ?: c.borda, EixoRaio.md)
                    .padding(vertical = 9.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(item.valor, style = EixoTipo.dado.copy(fontSize = 19.sp), color = tom?.texto ?: c.texto)
                Text(item.rotulo, style = EixoTipo.apoio.copy(fontSize = 10.sp), color = tom?.texto ?: c.textoFraco, maxLines = 1)
            }
        }
    }
}

/**
 * Odômetro: um dígito por caixa, como no painel do caminhão. Use só para KM.
 * Funciona sobre fundo escuro (cabeçalho) e claro.
 */
@Composable
fun EixoOdometro(km: Long, modifier: Modifier = Modifier, pequeno: Boolean = false, unidade: String = "KM") {
    val texto = formatarKm(km)
    val tamanho = if (pequeno) 21.sp else 30.sp
    val gradiente = Brush.verticalGradient(
        0f to Color(0xFF25334F), 0.46f to Color(0xFF141E33), 0.54f to Color(0xFF0B1424), 1f to Color(0xFF182338),
    )
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = "$texto quilômetros" },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        texto.forEach { ch ->
            if (ch.isDigit()) {
                Text(
                    ch.toString(),
                    style = EixoTipo.dadoGrande.copy(fontSize = tamanho, lineHeight = tamanho),
                    color = Color(0xFFF2F6FF),
                    modifier = Modifier
                        .shadow(2.dp, EixoRaio.xs)
                        .clip(EixoRaio.xs)
                        .background(gradiente)
                        .padding(horizontal = if (pequeno) 5.dp else 7.dp, vertical = if (pequeno) 5.dp else 8.dp),
                )
            } else {
                Text(ch.toString(), style = EixoTipo.dadoGrande.copy(fontSize = tamanho), color = Color(0xFF5C6E92))
            }
        }
        Text(unidade, style = EixoTipo.microRotulo, color = Color(0xFF7E93C4), modifier = Modifier.padding(start = 6.dp))
    }
}

/** Tom correspondente a uma classificação de consumo. */
fun Classificacao.tom(): Tom = when (this) {
    Classificacao.NORMAL -> Tom.Sucesso
    Classificacao.ATENCAO -> Tom.Atencao
    Classificacao.CRITICO -> Tom.Critico
}

/**
 * Medidor de eficiência — a assinatura do produto. A barra é o consumo real; o traço
 * vertical é o esperado do caminhão. A distância entre os dois é a resposta.
 *
 * O esperado fica sempre em 80% da trilha, para sobrar espaço visível acima da meta.
 */
@Composable
fun EixoMedidor(
    real: Double,
    esperado: Double,
    modifier: Modifier = Modifier,
    legendaDireita: String = "meta",
) {
    val c = Eixo.cores
    val classificacao = classificarConsumo(real, esperado)
    val cor = classificacao.tom().cores(c).texto
    val maximo = if (esperado > 0) esperado / 0.8 else 1.0
    val alvo = (real / maximo).toFloat().coerceIn(0f, 1f)
    var iniciado by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { iniciado = true }
    val fracao by animateFloatAsState(if (iniciado) alvo else 0f, tween(700), label = "medidor")
    val desvio = desvioPercentual(real, esperado)
    var larguraTrilha by remember { mutableStateOf(0) }
    val densidade = LocalDensity.current

    Column(modifier.fillMaxWidth().semantics {
        contentDescription = "Consumo ${formatarDecimal(real)} contra esperado ${formatarDecimal(esperado)}, ${classificacao.rotulo}"
    }) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(17.dp)
                .onSizeChanged { larguraTrilha = it.width },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(EixoRaio.pilula)
                    .background(c.superficie2)
                    .border(1.dp, c.borda, EixoRaio.pilula),
            ) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(fracao).clip(EixoRaio.pilula).background(cor))
            }
            // traço do esperado
            val xMarca = with(densidade) { (larguraTrilha * 0.8f).toDp() }
            Box(Modifier.offset(x = xMarca - 1.dp).width(2.dp).fillMaxHeight().clip(EixoRaio.pilula).background(c.texto.copy(alpha = 0.6f)))
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            val sinal = if (desvio > 0) "+" else ""
            Text("$sinal${formatarDecimal(desvio, 1).let { if (desvio < 0) "-$it" else it }}%",
                style = EixoTipo.microRotulo.copy(letterSpacing = 0.sp), color = c.textoTenue, modifier = Modifier.weight(1f))
            Text(legendaDireita, style = EixoTipo.microRotulo.copy(letterSpacing = 0.sp), color = c.textoTenue)
        }
    }
}

/** Cartão pronto de eficiência de um caminhão: placa, etiqueta, valor e medidor. */
@Composable
fun EixoCartaoEficiencia(
    placa: String,
    real: Double,
    esperado: Double,
    modifier: Modifier = Modifier,
    descricao: String? = null,
    rodape: String? = null,
    aoClicar: (() -> Unit)? = null,
) {
    val c = Eixo.cores
    val classificacao = classificarConsumo(real, esperado)
    EixoCartao(modifier, aoClicar = aoClicar, espacamento = 8.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EixoPlaca(placa)
            if (descricao != null) Text(descricao, style = EixoTipo.apoio, color = c.textoFraco, modifier = Modifier.weight(1f), maxLines = 1)
            else Box(Modifier.weight(1f))
            EixoEtiqueta(classificacao.rotulo, tom = classificacao.tom())
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(formatarDecimal(real), style = EixoTipo.dado.copy(fontSize = 22.sp), color = classificacao.tom().cores(c).texto)
            Text("km/L", style = EixoTipo.apoio, color = c.textoFraco, modifier = Modifier.weight(1f).padding(bottom = 3.dp))
            Text("esperado ${formatarDecimal(esperado)}", style = EixoTipo.dadoPequeno.copy(fontSize = 11.5.sp), color = c.textoFraco, modifier = Modifier.padding(bottom = 3.dp))
        }
        EixoMedidor(real, esperado)
        if (rodape != null) Text(rodape, style = EixoTipo.apoio.copy(fontSize = 11.5.sp), color = c.textoFraco)
    }
}

