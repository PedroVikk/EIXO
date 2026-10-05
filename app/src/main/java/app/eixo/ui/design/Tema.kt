package app.eixo.ui.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Envolva o app inteiro (e cada @Preview) com EixoTema.
 * Dentro dele, leia tokens assim: `Eixo.cores.acao`, `EixoTipo.dado`, `EixoEspaco.s4`.
 */
@Composable
fun EixoTema(
    escuro: Boolean = isSystemInDarkTheme(),
    conteudo: @Composable () -> Unit,
) {
    val cores = if (escuro) EixoCoresEscuras else EixoCoresClaras
    val esquema = if (escuro) {
        darkColorScheme(
            primary = cores.acao, onPrimary = cores.sobreAcao,
            primaryContainer = cores.acaoSuave, onPrimaryContainer = cores.acao,
            secondary = cores.marca, background = cores.fundo, onBackground = cores.texto,
            surface = cores.superficie, onSurface = cores.texto,
            surfaceVariant = cores.superficie2, onSurfaceVariant = cores.textoFraco,
            surfaceContainerHigh = cores.superficie, surfaceContainer = cores.superficie,
            surfaceContainerHighest = cores.superficie2,
            outline = cores.borda, outlineVariant = cores.borda,
            error = cores.critico, errorContainer = cores.criticoSuave,
        )
    } else {
        lightColorScheme(
            primary = cores.acao, onPrimary = cores.sobreAcao,
            primaryContainer = cores.acaoSuave, onPrimaryContainer = cores.acao,
            secondary = cores.marca, background = cores.fundo, onBackground = cores.texto,
            surface = cores.superficie, onSurface = cores.texto,
            surfaceVariant = cores.superficie2, onSurfaceVariant = cores.textoFraco,
            surfaceContainerHigh = cores.superficie, surfaceContainer = cores.superficie,
            surfaceContainerHighest = cores.superficie2,
            outline = cores.borda, outlineVariant = cores.borda,
            error = cores.critico, errorContainer = cores.criticoSuave,
        )
    }
    CompositionLocalProvider(LocalEixoCores provides cores) {
        MaterialTheme(colorScheme = esquema, typography = TipografiaMaterial, content = conteudo)
    }
}

/** Ponto único de acesso aos tokens dentro de um @Composable. */
object Eixo {
    val cores: EixoCores
        @Composable @ReadOnlyComposable get() = LocalEixoCores.current
}
