package app.eixo.ui.telas.acesso

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eixo.dados.Perfil
import app.eixo.dados.ResultadoLogin
import app.eixo.dados.Sessao
import app.eixo.dados.Usuario
import app.eixo.ui.componentes.EixoAlerta
import app.eixo.ui.componentes.EixoBotao
import app.eixo.ui.componentes.EixoCampo
import app.eixo.ui.componentes.EixoCampoSenha
import app.eixo.ui.componentes.EixoRotuloSecao
import app.eixo.ui.componentes.TamanhoBotao
import app.eixo.ui.componentes.Tom
import app.eixo.ui.componentes.VarianteBotao
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoEspaco
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTema
import app.eixo.ui.design.EixoTipo

/*
 * MÓDULO ACESSO — já funciona, para o grupo conseguir navegar desde o primeiro dia.
 * Quem pegar o módulo Acesso: refinar visual e textos conforme a tela "Entrar"
 * do design system, e decidir se os atalhos de demonstração ficam na apresentação.
 */

@Composable
fun LoginTela(aoEntrar: (Usuario) -> Unit, aoAbrirCatalogo: () -> Unit) {
    // O login é sempre escuro, sobre o petróleo da marca, nos dois temas do aparelho.
    EixoTema(escuro = true) {
        val c = Eixo.cores
        var login by rememberSaveable { mutableStateOf("") }
        var senha by rememberSaveable { mutableStateOf("") }
        var erro by rememberSaveable { mutableStateOf<String?>(null) }

        fun entrar() {
            when (val resultado = Sessao.entrar(login, senha)) {
                is ResultadoLogin.Sucesso -> { erro = null; aoEntrar(resultado.usuario) }
                ResultadoLogin.CredenciaisInvalidas -> erro = "E-mail, matrícula ou senha incorretos."
                ResultadoLogin.AcessoInativo -> erro = "Este acesso foi desativado. Fale com o administrador."
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(c.cabecalho)
                .background(Brush.radialGradient(listOf(Color(0x4D2D5BE3), Color.Transparent), radius = 1400f)),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = EixoEspaco.s5, vertical = EixoEspaco.s6),
                verticalArrangement = Arrangement.spacedBy(EixoEspaco.s4),
            ) {
                Spacer(Modifier.height(EixoEspaco.s8))
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(EixoRaio.lg)
                        .background(Brush.linearGradient(listOf(Color(0xFF3D6BF0), Color(0xFF12275C)))),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                Column {
                    Text("Eixo", style = EixoTipo.display.copy(fontSize = 40.sp, lineHeight = 42.sp), color = c.texto)
                    Text("Gestão de frota", style = EixoTipo.corpo, color = c.textoFraco)
                }
                Spacer(Modifier.height(EixoEspaco.s2))

                if (erro != null) {
                    EixoAlerta(titulo = erro!!, icone = Icons.Outlined.ErrorOutline, tom = Tom.Critico)
                }
                EixoCampo(
                    valor = login, aoMudar = { login = it; erro = null },
                    rotulo = "E-mail ou matrícula",
                    placeholder = "admin@eixo.app ou 1001",
                    teclado = KeyboardOptions(imeAction = ImeAction.Next),
                )
                EixoCampoSenha(
                    valor = senha, aoMudar = { senha = it; erro = null },
                    acoesTeclado = KeyboardActions(onDone = { entrar() }),
                )
                EixoBotao("Entrar", aoClicar = ::entrar, tamanho = TamanhoBotao.Grande)
                EixoAlerta(
                    titulo = "Esqueceu a senha?",
                    descricao = "Fale com o administrador da empresa. Não existe cadastro pelo próprio app.",
                    icone = Icons.Outlined.Key,
                )

                Spacer(Modifier.height(EixoEspaco.s4))
                EixoRotuloSecao("Modo demonstração · senha ${Sessao.SENHA_DEMO}")
                EixoBotao(
                    "Entrar como administrador", variante = VarianteBotao.Secundario, tamanho = TamanhoBotao.Pequeno,
                    aoClicar = { Sessao.entrarComo(Perfil.ADMIN)?.let(aoEntrar) },
                )
                EixoBotao(
                    "Entrar como motorista", variante = VarianteBotao.Secundario, tamanho = TamanhoBotao.Pequeno,
                    aoClicar = { Sessao.entrarComo(Perfil.MOTORISTA)?.let(aoEntrar) },
                )
                EixoBotao(
                    "Catálogo de componentes", variante = VarianteBotao.Fantasma, tamanho = TamanhoBotao.Pequeno,
                    icone = Icons.Outlined.Widgets, aoClicar = aoAbrirCatalogo,
                )
            }
        }
    }
}
