package app.eixo.ui.componentes

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import app.eixo.util.formatarKm
import kotlin.math.abs

/*
 * Campos de número. Todos GUARDAM só dígitos (String) e DESENHAM a pontuação pela máscara.
 * Converta para número só na hora de salvar, com as funções de util/Formatos.kt:
 *   digitosParaKm("152430")      -> 152430L
 *   digitosParaDecimal("276900") -> 2769.0
 */

// Teclado numérico puro: sem vírgula, ponto ou sinal. A máscara cuida da pontuação.
private val tecladoDigitos = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next)

/**
 * Quilometragem. Quando há um valor sugerido (último KM conhecido), o campo mostra
 * a etiqueta "Pré-preenchido" e a dica com a origem e a diferença — o valor sugerido
 * nunca entra em silêncio.
 *
 * @param sugerido último KM conhecido; null se não houver.
 * @param origemSugestao de onde veio, ex.: "último abastecimento, 21/08".
 */
@Composable
fun EixoCampoKm(
    digitos: String,
    aoMudar: (String) -> Unit,
    modifier: Modifier = Modifier,
    rotulo: String = "KM no tacógrafo",
    sugerido: Long? = null,
    origemSugestao: String? = null,
    erro: String? = null,
) {
    val atual = digitos.toLongOrNull()
    val dica = when {
        sugerido == null -> null
        atual == null || atual == sugerido ->
            "Sugerido: ${formatarKm(sugerido)} km" + (origemSugestao?.let { " · $it" } ?: "")
        else -> {
            val diferenca = atual - sugerido
            val verbo = if (diferenca > 0) "acrescentou" else "reduziu"
            "Vinha ${formatarKm(sugerido)}" + (origemSugestao?.let { " ($it)" } ?: "") +
                " · você $verbo ${formatarKm(abs(diferenca))} km"
        }
    }
    EixoCampo(
        valor = digitos,
        aoMudar = { aoMudar(somenteDigitos(it, maximo = 7)) },
        rotulo = rotulo,
        modifier = modifier,
        sufixo = "km",
        etiqueta = if (sugerido != null) "Pré-preenchido" else null,
        dica = dica,
        erro = erro,
        grande = true,
        numerico = true,
        teclado = tecladoDigitos,
        transformacao = MascaraKm,
    )
}

/** Dinheiro. Preenche da direita para a esquerda: digitar 276900 mostra R$ 2.769,00. */
@Composable
fun EixoCampoDinheiro(
    centavos: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    dica: String? = null,
    erro: String? = null,
    etiqueta: String? = null,
) {
    val mascara = remember { MascaraDecimal(2) }
    EixoCampo(
        valor = centavos,
        aoMudar = { aoMudar(somenteDigitos(it, maximo = 11)) },
        rotulo = rotulo,
        modifier = modifier,
        prefixo = "R$",
        etiqueta = etiqueta,
        dica = dica,
        erro = erro,
        grande = true,
        numerico = true,
        teclado = tecladoDigitos,
        transformacao = mascara,
    )
}

/** Litros, com duas casas: digitar 39000 mostra 390,00 L. */
@Composable
fun EixoCampoLitros(
    digitos: String,
    aoMudar: (String) -> Unit,
    modifier: Modifier = Modifier,
    rotulo: String = "Litros",
    dica: String? = null,
    erro: String? = null,
) {
    val mascara = remember { MascaraDecimal(2) }
    EixoCampo(
        valor = digitos,
        aoMudar = { aoMudar(somenteDigitos(it, maximo = 8)) },
        rotulo = rotulo,
        modifier = modifier,
        sufixo = "L",
        dica = dica,
        erro = erro,
        grande = true,
        numerico = true,
        teclado = tecladoDigitos,
        transformacao = mascara,
    )
}

/** Número inteiro simples (peso em kg, quantidade). */
@Composable
fun EixoCampoInteiro(
    digitos: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    sufixo: String? = null,
    dica: String? = null,
    erro: String? = null,
    maximo: Int = 9,
) {
    EixoCampo(
        valor = digitos,
        aoMudar = { aoMudar(somenteDigitos(it, maximo)) },
        rotulo = rotulo,
        modifier = modifier,
        sufixo = sufixo,
        dica = dica,
        erro = erro,
        numerico = true,
        teclado = tecladoDigitos,
        transformacao = MascaraKm, // mesma pontuação de milhar
    )
}

/** Valor calculado pelo sistema, só para conferência (ex.: preço por litro). */
@Composable
fun EixoCampoCalculado(
    valor: String,
    rotulo: String,
    modifier: Modifier = Modifier,
    dica: String? = null,
) {
    EixoCampo(
        valor = valor,
        aoMudar = {},
        rotulo = rotulo,
        modifier = modifier,
        sufixo = "calculado",
        dica = dica,
        somenteLeitura = true,
        numerico = true,
    )
}
