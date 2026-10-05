package app.eixo.ui.componentes

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/*
 * Máscaras visuais. O campo GUARDA só dígitos; a máscara apenas DESENHA a pontuação.
 * O motorista nunca digita ponto, vírgula ou R$.
 *
 *   MascaraKm       "152430" → "152.430"
 *   MascaraDecimal  "276900" → "2.769,00"   (preenche da direita, centavos primeiro)
 */

/** Limpa o que chega do teclado: só dígitos, sem zero à esquerda, com limite. */
fun somenteDigitos(texto: String, maximo: Int): String =
    texto.filter(Char::isDigit).trimStart('0').take(maximo)

private fun agrupar(digitos: String): String = buildString {
    digitos.forEachIndexed { i, c ->
        if (i > 0 && (digitos.length - i) % 3 == 0) append('.')
        append(c)
    }
}

/**
 * Mapeia posições casando dígitos da direita para a esquerda. Funciona para as duas
 * máscaras porque ambas só inserem pontuação (e, no decimal, zeros à esquerda).
 */
private fun mapeamentoPelaDireita(original: String, transformado: String): OffsetMapping {
    val o2t = IntArray(original.length + 1)
    val t2o = IntArray(transformado.length + 1)
    o2t[original.length] = transformado.length
    t2o[transformado.length] = original.length
    var oi = original.length
    for (ti in transformado.length - 1 downTo 0) {
        if (transformado[ti].isDigit() && oi > 0) {
            oi--
            o2t[oi] = ti
        }
        t2o[ti] = oi
    }
    return object : OffsetMapping {
        override fun originalToTransformed(offset: Int) = o2t[offset.coerceIn(0, original.length)]
        override fun transformedToOriginal(offset: Int) = t2o[offset.coerceIn(0, transformado.length)]
    }
}

object MascaraKm : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatado = agrupar(text.text)
        return TransformedText(AnnotatedString(formatado), mapeamentoPelaDireita(text.text, formatado))
    }
}

class MascaraDecimal(private val casas: Int = 2) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text
        if (digitos.isEmpty()) return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        val completo = digitos.padStart(casas + 1, '0')
        val inteiro = completo.dropLast(casas).trimStart('0').ifEmpty { "0" }
        val formatado = "${agrupar(inteiro)},${completo.takeLast(casas)}"
        return TransformedText(AnnotatedString(formatado), mapeamentoPelaDireita(digitos, formatado))
    }

    override fun equals(other: Any?) = other is MascaraDecimal && other.casas == casas
    override fun hashCode() = casas
}
