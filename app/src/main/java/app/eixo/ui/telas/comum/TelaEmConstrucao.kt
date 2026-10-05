package app.eixo.ui.telas.comum

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.runtime.Composable
import app.eixo.ui.componentes.EixoBotaoCabecalho
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoEstadoVazio
import app.eixo.ui.componentes.EixoTela

/**
 * Marcador de tela ainda não construída. Cada membro do grupo SUBSTITUI a chamada
 * a esta função pelo conteúdo real da sua tela — a assinatura da tela não muda.
 */
@Composable
fun TelaEmConstrucao(
    titulo: String,
    modulo: String,
    oQueFazer: String,
    aoVoltar: (() -> Unit)? = null,
    barra: (@Composable () -> Unit)? = null,
    aoSair: (() -> Unit)? = null,
) {
    EixoTela(
        cabecalho = {
            EixoCabecalho(
                titulo, sobretitulo = "Em construção · $modulo", aoVoltar = aoVoltar,
                acoes = { if (aoSair != null) EixoBotaoCabecalho(Icons.AutoMirrored.Outlined.Logout, "Sair", aoSair) },
            )
        },
        barraInferior = barra,
    ) {
        EixoEstadoVazio(
            icone = Icons.Outlined.Construction,
            titulo = "Tela ainda não construída",
            descricao = "$oQueFazer\n\nReferência pronta: telas de Cargas. Componentes: tela Catálogo.",
        )
    }
}
