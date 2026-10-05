package app.eixo.ui.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.eixo.dados.TipoCombustivel
import app.eixo.ui.componentes.EixoBotao
import app.eixo.ui.componentes.EixoCampo
import app.eixo.ui.componentes.EixoCampoCalculado
import app.eixo.ui.componentes.EixoCampoData
import app.eixo.ui.componentes.EixoCampoDataHora
import app.eixo.ui.componentes.EixoCampoDinheiro
import app.eixo.ui.componentes.EixoCampoInteiro
import app.eixo.ui.componentes.EixoCampoKm
import app.eixo.ui.componentes.EixoCampoLitros
import app.eixo.ui.componentes.EixoCampoSenha
import app.eixo.ui.componentes.EixoSegmentado
import app.eixo.ui.componentes.EixoSeletor
import app.eixo.ui.componentes.EixoSimNao
import app.eixo.ui.componentes.TamanhoBotao
import app.eixo.ui.componentes.Tom
import app.eixo.ui.componentes.VarianteBotao
import app.eixo.ui.design.EixoEspaco
import app.eixo.util.digitosParaDecimal
import app.eixo.util.formatarMoeda
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
internal fun SecaoBotoes() {
    Secao("Botões", "EixoBotao(variante, tamanho, icone, habilitado, carregando)") {
        EixoBotao("Registrar abastecimento", aoClicar = {}, tamanho = TamanhoBotao.Grande, icone = Icons.Outlined.LocalGasStation)
        EixoBotao("Nova viagem", aoClicar = {}, icone = Icons.Outlined.Add)
        EixoBotao("Ver abastecimentos", aoClicar = {}, variante = VarianteBotao.Secundario)
        EixoBotao("Cancelar", aoClicar = {}, variante = VarianteBotao.Fantasma)
        Row(horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
            EixoBotao("Salvar", aoClicar = {}, variante = VarianteBotao.Sucesso, tamanho = TamanhoBotao.Pequeno, modifier = Modifier.weight(1f))
            EixoBotao("Excluir", aoClicar = {}, variante = VarianteBotao.Perigo, tamanho = TamanhoBotao.Pequeno, modifier = Modifier.weight(1f))
        }
        EixoBotao("Finalizar viagem", aoClicar = {}, habilitado = false)
        EixoBotao("Salvando", aoClicar = {}, carregando = true)
    }
}

@Composable
internal fun SecaoCampos() {
    var nome by rememberSaveable { mutableStateOf("João da Silva") }
    var vazio by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var obs by rememberSaveable { mutableStateOf("") }
    Secao("Campos de texto", "EixoCampo · EixoCampoSenha") {
        EixoCampo(valor = nome, aoMudar = { nome = it }, rotulo = "Nome completo")
        EixoCampo(valor = vazio, aoMudar = { vazio = it }, rotulo = "Cliente", placeholder = "Quem contratou o frete",
            erro = if (vazio.isBlank()) "Informe para quem é a carga." else null)
        EixoCampo(valor = obs, aoMudar = { obs = it }, rotulo = "Observação", etiqueta = "Opcional", tomEtiqueta = Tom.Neutro,
            linhas = 3, placeholder = "Ex.: manter refrigerada")
        EixoCampoSenha(valor = senha, aoMudar = { senha = it }, dica = "Pelo menos 8 caracteres, com letra e número.")
    }
}

@Composable
internal fun SecaoCamposNumericos() {
    var km by rememberSaveable { mutableStateOf("153980") }
    var litros by rememberSaveable { mutableStateOf("39000") }
    var valor by rememberSaveable { mutableStateOf("276900") }
    var peso by rememberSaveable { mutableStateOf("32000") }
    val l = digitosParaDecimal(litros)
    val v = digitosParaDecimal(valor)
    Secao(
        "Campos numéricos",
        "EixoCampoKm · EixoCampoLitros · EixoCampoDinheiro · EixoCampoInteiro · EixoCampoCalculado",
    ) {
        EixoCampoKm(digitos = km, aoMudar = { km = it }, sugerido = 153_500, origemSugestao = "último abastecimento")
        Row(horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
            EixoCampoLitros(digitos = litros, aoMudar = { litros = it }, modifier = Modifier.weight(1f))
            EixoCampoDinheiro(centavos = valor, aoMudar = { valor = it }, rotulo = "Valor total", modifier = Modifier.weight(1f))
        }
        EixoCampoCalculado(
            valor = if (l > 0) formatarMoeda(v / l) else "—",
            rotulo = "Preço por litro",
            dica = "Valor total ÷ litros. Confira com o cupom.",
        )
        EixoCampoInteiro(digitos = peso, aoMudar = { peso = it }, rotulo = "Peso", sufixo = "kg")
    }
}

@Composable
internal fun SecaoSelecao() {
    var combustivel by rememberSaveable { mutableStateOf<TipoCombustivel?>(TipoCombustivel.DIESEL_S10) }
    var filtro by rememberSaveable { mutableStateOf(0) }
    var tanque by rememberSaveable { mutableStateOf<Boolean?>(true) }
    var data by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var saida by rememberSaveable { mutableStateOf<LocalDateTime?>(null) }
    Secao("Seleção", "EixoSeletor · EixoSegmentado · EixoSimNao · EixoCampoData · EixoCampoDataHora") {
        EixoSeletor(
            rotulo = "Combustível", opcoes = TipoCombustivel.entries, selecionado = combustivel,
            aoSelecionar = { combustivel = it }, textoDe = { it.rotulo },
        )
        EixoSegmentado(opcoes = listOf("Todas", "Em andamento", "Finalizadas"), selecionado = filtro, aoSelecionar = { filtro = it })
        EixoSimNao(rotulo = "Tanque cheio?", valor = tanque, aoMudar = { tanque = it },
            dica = "Só abastecimento com tanque cheio serve de referência para km/L.")
        EixoCampoData(rotulo = "Validade da CNH", data = data, aoSelecionar = { data = it })
        EixoCampoDataHora(rotulo = "Saída prevista", dataHora = saida, aoSelecionar = { saida = it })
    }
}
