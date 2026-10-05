package app.eixo.ui.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.eixo.R

/*
 * Três famílias, três trabalhos:
 *  - Archivo  → títulos, rótulos e botões. A largura variável é o truque:
 *               comprimida vira título, expandida vira placa de sinalização.
 *  - Plex Sans → todo texto de leitura e de campo.
 *  - Plex Mono → TODO número: KM, litros, R$, placa, data. Sempre tabular.
 */

@OptIn(ExperimentalTextApi::class)
private fun archivo(peso: Int, largura: Float) = Font(
    R.font.archivo_variavel,
    weight = FontWeight(peso),
    variationSettings = FontVariation.Settings(
        FontVariation.weight(peso),
        FontVariation.width(largura),
    ),
)

@OptIn(ExperimentalTextApi::class)
private fun plexSans(peso: Int) = Font(
    R.font.plex_sans_variavel,
    weight = FontWeight(peso),
    variationSettings = FontVariation.Settings(FontVariation.weight(peso), FontVariation.width(100f)),
)

/** Archivo comprimida — número herói, título grande. */
val FonteDisplay = FontFamily(archivo(700, 84f), archivo(800, 84f))

/** Archivo normal — título de tela e de seção. */
val FonteTitulo = FontFamily(archivo(600, 96f), archivo(700, 96f))

/** Archivo expandida — botão e rótulo em caixa-alta. */
val FonteRotulo = FontFamily(archivo(500, 108f), archivo(700, 104f))

val FonteCorpo = FontFamily(plexSans(400), plexSans(500), plexSans(600), plexSans(700))

val FonteDado = FontFamily(
    Font(R.font.plex_mono_regular, FontWeight.Normal),
    Font(R.font.plex_mono_medium, FontWeight.Medium),
    Font(R.font.plex_mono_semibold, FontWeight.SemiBold),
)

private const val TABULAR = "tnum"

/** Escala tipográfica. Fique nela — não invente tamanho novo em tela. */
object EixoTipo {
    val display = TextStyle(fontFamily = FonteDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 36.sp, letterSpacing = (-0.03).em)
    val tituloTela = TextStyle(fontFamily = FonteTitulo, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = (-0.01).em)
    val tituloSecao = TextStyle(fontFamily = FonteTitulo, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 22.sp)
    val botao = TextStyle(fontFamily = FonteRotulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.07.em)
    val corpo = TextStyle(fontFamily = FonteCorpo, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp)
    val corpoForte = corpo.copy(fontWeight = FontWeight.SemiBold)
    /** Valor digitado em campo. Nunca abaixo de 16sp. */
    val campo = TextStyle(fontFamily = FonteCorpo, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
    val apoio = TextStyle(fontFamily = FonteCorpo, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp)
    val rotulo = TextStyle(fontFamily = FonteCorpo, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp)
    /** Rótulo pequeno em caixa-alta: etiqueta de status, cabeçalho de seção. */
    val microRotulo = TextStyle(fontFamily = FonteDado, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.18.em)
    val dado = TextStyle(fontFamily = FonteDado, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp, fontFeatureSettings = TABULAR)
    val dadoGrande = TextStyle(fontFamily = FonteDado, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 32.sp, letterSpacing = (-0.02).em, fontFeatureSettings = TABULAR)
    val dadoPequeno = TextStyle(fontFamily = FonteDado, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp, fontFeatureSettings = TABULAR)
}

/** Para que os componentes do Material (diálogos, menus) herdem as fontes certas. */
internal val TipografiaMaterial = Typography(
    displaySmall = EixoTipo.display,
    headlineSmall = EixoTipo.tituloTela,
    titleLarge = EixoTipo.tituloTela,
    titleMedium = EixoTipo.tituloSecao,
    titleSmall = EixoTipo.corpoForte,
    bodyLarge = EixoTipo.campo,
    bodyMedium = EixoTipo.corpo,
    bodySmall = EixoTipo.apoio,
    labelLarge = EixoTipo.botao,
    labelMedium = EixoTipo.rotulo,
    labelSmall = EixoTipo.microRotulo,
)
