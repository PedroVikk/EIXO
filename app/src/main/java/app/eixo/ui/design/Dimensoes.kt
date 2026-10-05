package app.eixo.ui.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Espaçamento em base 4. Use estes valores — nunca um `padding(13.dp)` solto. */
object EixoEspaco {
    val s1 = 4.dp
    val s2 = 8.dp
    val s3 = 12.dp
    val s4 = 16.dp
    val s5 = 20.dp
    val s6 = 24.dp
    val s8 = 32.dp

    /** Margem lateral padrão de toda tela. */
    val margemTela = 16.dp
}

object EixoRaio {
    val xs = RoundedCornerShape(6.dp)   // placa, dígito do odômetro
    val sm = RoundedCornerShape(8.dp)   // quadro de ícone
    val md = RoundedCornerShape(12.dp)  // campo, botão, alerta
    val lg = RoundedCornerShape(14.dp)  // cartão
    val xl = RoundedCornerShape(28.dp)  // folha inferior, diálogo
    val pilula = RoundedCornerShape(999.dp)
}

/** Alvo de toque mínimo. O motorista opera de pé, com uma mão. */
object EixoAlvo {
    val pequeno = 42.dp
    val normal = 52.dp
    val grande = 58.dp
}
