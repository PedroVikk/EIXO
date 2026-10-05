package app.eixo.ui.catalogo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.eixo.ui.componentes.EixoBotaoCabecalho
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoTela
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoEspaco
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTema
import app.eixo.ui.design.EixoTipo

/**
 * Vitrine de todos os componentes, funcionando de verdade. Cada bloco mostra o nome
 * exato da função para procurar no código. Use o botão do cabeçalho para ver no tema escuro.
 */
@Composable
fun CatalogoTela(aoVoltar: () -> Unit) {
    val sistemaEscuro = isSystemInDarkTheme()
    var escuro by rememberSaveable { mutableStateOf(sistemaEscuro) }
    EixoTema(escuro = escuro) {
        EixoTela(
            cabecalho = {
                EixoCabecalho(
                    titulo = "Catálogo",
                    sobretitulo = "Componentes do Eixo",
                    aoVoltar = aoVoltar,
                    acoes = {
                        EixoBotaoCabecalho(
                            if (escuro) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            if (escuro) "Ver no tema claro" else "Ver no tema escuro",
                        ) { escuro = !escuro }
                    },
                )
            },
            espacamento = EixoEspaco.s6,
        ) {
            SecaoCores()
            SecaoTipografia()
            SecaoBotoes()
            SecaoCampos()
            SecaoCamposNumericos()
            SecaoSelecao()
            SecaoEtiquetas()
            SecaoNumeros()
            SecaoEficiencia()
            SecaoAlertas()
            SecaoListas()
            SecaoEstrutura()
        }
    }
}

/** Bloco do catálogo: título, nomes das funções e a demonstração viva. */
@Composable
internal fun Secao(titulo: String, funcoes: String, conteudo: @Composable ColumnScope.() -> Unit) {
    val c = Eixo.cores
    Column(verticalArrangement = Arrangement.spacedBy(EixoEspaco.s3)) {
        Column {
            Text(titulo, style = EixoTipo.tituloTela, color = c.texto)
            Text(funcoes, style = EixoTipo.dadoPequeno, color = c.acao, modifier = Modifier.padding(top = 4.dp))
        }
        conteudo()
    }
}

@Composable
private fun Amostra(nome: String, cor: Color, modifier: Modifier = Modifier) {
    val c = Eixo.cores
    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(44.dp).clip(EixoRaio.sm).background(cor).border(1.dp, c.borda, EixoRaio.sm))
        Text(nome, style = EixoTipo.apoio.copy(fontSize = EixoTipo.microRotulo.fontSize), color = c.textoFraco, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun SecaoCores() {
    val c = Eixo.cores
    Secao("Cores", "Eixo.cores.<nome>") {
        val linhas = listOf(
            listOf("cabecalho" to c.cabecalho, "acao" to c.acao, "acaoSuave" to c.acaoSuave),
            listOf("normal" to c.normal, "atencao" to c.atencao, "critico" to c.critico),
            listOf("fundo" to c.fundo, "superficie" to c.superficie, "superficie2" to c.superficie2),
            listOf("texto" to c.texto, "textoFraco" to c.textoFraco, "borda" to c.borda),
        )
        linhas.forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
                linha.forEach { (nome, cor) -> Amostra(nome, cor, Modifier.weight(1f)) }
            }
        }
        Text(
            "Verde, âmbar e vermelho são só para estado. Nunca como cor de marca ou enfeite.",
            style = EixoTipo.apoio, color = c.textoFraco,
        )
    }
}

@Composable
private fun SecaoTipografia() {
    val c = Eixo.cores
    Secao("Tipografia", "EixoTipo.<estilo>") {
        Text("1.750", style = EixoTipo.display, color = c.texto)
        Text("tituloTela — Nova viagem", style = EixoTipo.tituloTela, color = c.texto)
        Text("tituloSecao — Abastecimentos", style = EixoTipo.tituloSecao, color = c.texto)
        Text("corpo — Registre o abastecimento antes de informar o valor.", style = EixoTipo.corpo, color = c.texto)
        Text("apoio — Último registro: 153.500 km", style = EixoTipo.apoio, color = c.textoFraco)
        Text("rotulo — Placa do caminhão", style = EixoTipo.rotulo, color = c.textoFraco)
        Text("MICRORROTULO — EM ANDAMENTO", style = EixoTipo.microRotulo, color = c.textoTenue)
        Text("dado — R$ 2.769,00 · 2,84 km/L", style = EixoTipo.dado, color = c.texto)
        Text("152.430", style = EixoTipo.dadoGrande, color = c.texto)
        Text("Todo número usa EixoTipo.dado*: mono e tabular, para coluna alinhar.", style = EixoTipo.apoio, color = c.textoFraco)
    }
}
