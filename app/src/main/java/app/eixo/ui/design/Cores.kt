package app.eixo.ui.design

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Paleta do Eixo. Regra que organiza tudo: a cor da marca nunca é cor de estado.
 * Verde, âmbar e vermelho só carregam significado (normal, atenção, crítico).
 *
 * Nunca use Color(0xFF...) dentro de uma tela — leia sempre de `Eixo.cores`.
 */
@Immutable
data class EixoCores(
    val fundo: Color,
    val superficie: Color,
    val superficie2: Color,
    val borda: Color,
    val texto: Color,
    val textoFraco: Color,
    val textoTenue: Color,
    val marca: Color,
    /** Fundo do cabeçalho — petróleo nos dois temas. */
    val cabecalho: Color,
    val acao: Color,
    val acaoSuave: Color,
    /** Texto e ícone sobre `acao` (botão primário). */
    val sobreAcao: Color,
    val normal: Color,
    val normalSuave: Color,
    val sobreNormal: Color,
    val atencao: Color,
    val atencaoSuave: Color,
    val critico: Color,
    val criticoSuave: Color,
    val escuro: Boolean,
) {
    // Cabeçalho é sempre escuro, então o texto sobre ele é fixo nos dois temas.
    val cabecalhoTexto: Color get() = Color(0xFFEAEFF9)
    val cabecalhoTextoFraco: Color get() = Color(0xFFC7D2E8)
    val cabecalhoRotulo: Color get() = Color(0xFF7E93C4)
    val cabecalhoBorda: Color get() = Color(0x29FFFFFF)
}

val EixoCoresClaras = EixoCores(
    fundo = Color(0xFFEFF2F7),
    superficie = Color(0xFFFFFFFF),
    superficie2 = Color(0xFFF5F7FB),
    borda = Color(0xFFE1E7F1),
    texto = Color(0xFF0D1526),
    textoFraco = Color(0xFF5A6478),
    textoTenue = Color(0xFF8B95AC),
    marca = Color(0xFF12275C),
    cabecalho = Color(0xFF0E1B3A),
    acao = Color(0xFF2D5BE3),
    acaoSuave = Color(0xFFE8EEFD),
    sobreAcao = Color(0xFFFFFFFF),
    normal = Color(0xFF0F7A52),
    normalSuave = Color(0xFFE3F3EB),
    sobreNormal = Color(0xFFFFFFFF),
    atencao = Color(0xFFA96C08),
    atencaoSuave = Color(0xFFFAEFDA),
    critico = Color(0xFFBE3229),
    criticoSuave = Color(0xFFFCEAE7),
    escuro = false,
)

val EixoCoresEscuras = EixoCores(
    fundo = Color(0xFF080E1B),
    superficie = Color(0xFF111B2E),
    superficie2 = Color(0xFF16223A),
    borda = Color(0xFF25324B),
    texto = Color(0xFFEAEFF9),
    textoFraco = Color(0xFF96A3BC),
    textoTenue = Color(0xFF6B7793),
    marca = Color(0xFF94B0F8),
    cabecalho = Color(0xFF0A1222),
    acao = Color(0xFF5B85F5),
    acaoSuave = Color(0xFF182747),
    sobreAcao = Color(0xFF050F1F),
    normal = Color(0xFF3EBE8B),
    normalSuave = Color(0xFF122A22),
    sobreNormal = Color(0xFF04140E),
    atencao = Color(0xFFE0A03A),
    atencaoSuave = Color(0xFF2B2213),
    critico = Color(0xFFF0736A),
    criticoSuave = Color(0xFF2E1614),
    escuro = true,
)

val LocalEixoCores = staticCompositionLocalOf { EixoCoresClaras }
