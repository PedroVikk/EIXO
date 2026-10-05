package app.eixo.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTipo

/**
 * Moldura comum a todo campo: rótulo SEMPRE visível em cima (nunca flutuante —
 * rótulo flutuante some justo quando o motorista precisa conferir), etiqueta opcional
 * ao lado do rótulo, e dica ou erro embaixo.
 */
@Composable
internal fun MolduraCampo(
    rotulo: String,
    modifier: Modifier = Modifier,
    etiqueta: String? = null,
    tomEtiqueta: Tom = Tom.Acao,
    dica: String? = null,
    erro: String? = null,
    campo: @Composable () -> Unit,
) {
    val c = Eixo.cores
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(rotulo, style = EixoTipo.rotulo, color = c.textoFraco)
            if (etiqueta != null) EixoEtiqueta(etiqueta, tom = tomEtiqueta, compacta = true)
        }
        campo()
        when {
            erro != null -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = c.critico, modifier = Modifier.size(14.dp))
                Text(erro, style = EixoTipo.apoio.copy(fontSize = 12.sp), color = c.critico)
            }
            dica != null -> Text(dica, style = EixoTipo.apoio.copy(fontSize = 12.sp), color = c.textoTenue)
        }
    }
}

/** Caixa visual do campo (borda, fundo, altura). Reaproveitada pelo seletor e pela data. */
@Composable
internal fun CaixaCampo(
    focado: Boolean,
    temErro: Boolean,
    somenteLeitura: Boolean,
    grande: Boolean,
    modifier: Modifier = Modifier,
    multilinha: Boolean = false,
    aoClicar: (() -> Unit)? = null,
    descricao: String? = null,
    conteudo: @Composable RowScope.() -> Unit,
) {
    val c = Eixo.cores
    val corBorda = when {
        temErro -> c.critico
        focado -> c.acao
        else -> c.borda
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (grande) 60.dp else 52.dp)
            .clip(EixoRaio.md)
            .background(if (somenteLeitura) c.superficie2 else c.superficie)
            .border(if (focado) 2.dp else 1.5.dp, corBorda, EixoRaio.md)
            .then(if (aoClicar != null) Modifier.clickable(role = Role.Button, onClick = aoClicar) else Modifier)
            .then(if (descricao != null) Modifier.semantics { contentDescription = descricao } else Modifier)
            .padding(horizontal = 14.dp, vertical = if (multilinha) 14.dp else 0.dp),
        verticalAlignment = if (multilinha) Alignment.Top else Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = conteudo,
    )
}

/**
 * Campo de texto do Eixo.
 *
 * @param numerico usa a fonte de dados (mono, tabular). Ligue para qualquer número.
 * @param etiqueta pílula ao lado do rótulo — ex.: "Pré-preenchido", "Opcional".
 * @param linhas mais de 1 vira campo de várias linhas (observação).
 */
@Composable
fun EixoCampo(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    dica: String? = null,
    erro: String? = null,
    prefixo: String? = null,
    sufixo: String? = null,
    etiqueta: String? = null,
    tomEtiqueta: Tom = Tom.Acao,
    somenteLeitura: Boolean = false,
    habilitado: Boolean = true,
    grande: Boolean = false,
    numerico: Boolean = false,
    teclado: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    acoesTeclado: KeyboardActions = KeyboardActions.Default,
    transformacao: VisualTransformation = VisualTransformation.None,
    linhas: Int = 1,
    iconeFinal: (@Composable () -> Unit)? = null,
) {
    val c = Eixo.cores
    val interacao = remember { MutableInteractionSource() }
    val focado by interacao.collectIsFocusedAsState()
    val estiloTexto: TextStyle = if (numerico) {
        EixoTipo.dado.copy(fontSize = if (grande) 20.sp else 17.sp)
    } else {
        EixoTipo.campo
    }

    MolduraCampo(rotulo, modifier, etiqueta, tomEtiqueta, dica, erro) {
        BasicTextField(
            value = valor,
            onValueChange = aoMudar,
            enabled = habilitado,
            readOnly = somenteLeitura,
            textStyle = estiloTexto.copy(color = if (habilitado) c.texto else c.textoTenue),
            keyboardOptions = teclado,
            keyboardActions = acoesTeclado,
            singleLine = linhas == 1,
            minLines = if (linhas > 1) linhas else 1,
            maxLines = if (linhas > 1) linhas + 3 else 1,
            visualTransformation = transformacao,
            interactionSource = interacao,
            cursorBrush = SolidColor(c.acao),
            modifier = Modifier.semantics { contentDescription = rotulo },
            decorationBox = { campoInterno ->
                CaixaCampo(
                    focado = focado && !somenteLeitura,
                    temErro = erro != null,
                    somenteLeitura = somenteLeitura,
                    grande = grande,
                    multilinha = linhas > 1,
                ) {
                    if (prefixo != null) Text(prefixo, style = EixoTipo.dadoPequeno, color = c.textoFraco)
                    Box(Modifier.weight(1f)) {
                        if (valor.isEmpty() && placeholder != null) {
                            Text(placeholder, style = estiloTexto, color = c.textoTenue, maxLines = 1)
                        }
                        campoInterno()
                    }
                    if (sufixo != null) Text(sufixo, style = EixoTipo.dadoPequeno, color = c.textoFraco)
                    iconeFinal?.invoke()
                }
            },
        )
    }
}

@Composable
fun EixoCampoSenha(
    valor: String,
    aoMudar: (String) -> Unit,
    modifier: Modifier = Modifier,
    rotulo: String = "Senha",
    dica: String? = null,
    erro: String? = null,
    acaoTeclado: ImeAction = ImeAction.Done,
    acoesTeclado: KeyboardActions = KeyboardActions.Default,
) {
    var visivel by rememberSaveable { mutableStateOf(false) }
    val c = Eixo.cores
    EixoCampo(
        valor = valor,
        aoMudar = aoMudar,
        rotulo = rotulo,
        modifier = modifier,
        dica = dica,
        erro = erro,
        teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = acaoTeclado),
        acoesTeclado = acoesTeclado,
        transformacao = if (visivel) VisualTransformation.None else PasswordVisualTransformation(),
        iconeFinal = {
            Icon(
                imageVector = if (visivel) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = if (visivel) "Esconder senha" else "Mostrar senha",
                tint = c.textoFraco,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(role = Role.Button) { visivel = !visivel },
            )
        },
    )
}
