package app.eixo.ui.telas.acesso

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.eixo.dados.Sessao
import app.eixo.ui.componentes.EixoBotao
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoCartao
import app.eixo.ui.componentes.EixoEtiqueta
import app.eixo.ui.componentes.EixoLinhaValor
import app.eixo.ui.componentes.EixoTela
import app.eixo.ui.componentes.Tom
import app.eixo.ui.componentes.VarianteBotao
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTipo
import app.eixo.util.formatarData

/** Conta do usuário logado: quem é, perfil, e sair. */
@Composable
fun ContaTela(barra: @Composable () -> Unit, aoSair: () -> Unit, aoAbrirCatalogo: () -> Unit) {
    val c = Eixo.cores
    val usuario by Sessao.usuario.collectAsState()
    val u = usuario ?: return
    val iniciais = u.nome.split(" ").filter { it.isNotBlank() }.let { p -> (p.first().take(1) + p.last().take(1)).uppercase() }

    EixoTela(cabecalho = { EixoCabecalho("Conta") }, barraInferior = barra) {
        EixoCartao(espacamento = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(48.dp).clip(EixoRaio.md).background(Brush.linearGradient(listOf(Color(0xFF3D6BF0), Color(0xFF12275C)))),
                    contentAlignment = Alignment.Center,
                ) { Text(iniciais, style = EixoTipo.dado.copy(color = Color.White)) }
                Column(Modifier.weight(1f)) {
                    Text(u.nome, style = EixoTipo.tituloSecao, color = c.texto)
                    Text(u.login, style = EixoTipo.dadoPequeno, color = c.textoFraco)
                }
                EixoEtiqueta(u.perfil.rotulo, tom = Tom.Acao)
            }
            u.motorista?.let { m ->
                Column {
                    EixoLinhaValor("CNH", "${m.cnh} · ${m.categoriaCnh}")
                    EixoLinhaValor("Validade da CNH", formatarData(m.validadeCnh))
                    EixoLinhaValor("Telefone", m.telefone, ultima = true)
                }
            }
        }
        EixoBotao("Catálogo de componentes", aoClicar = aoAbrirCatalogo, variante = VarianteBotao.Fantasma, icone = Icons.Outlined.Widgets)
        EixoBotao("Sair", aoClicar = aoSair, variante = VarianteBotao.Secundario, icone = Icons.AutoMirrored.Outlined.Logout)
    }
}
