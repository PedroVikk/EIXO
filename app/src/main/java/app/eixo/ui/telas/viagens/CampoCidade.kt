package app.eixo.ui.telas.viagens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.eixo.dados.remoto.Estado
import app.eixo.dados.remoto.Localidades
import app.eixo.ui.componentes.EixoCampo
import app.eixo.ui.componentes.EixoSeletor
import app.eixo.ui.design.EixoEspaco
import kotlinx.coroutines.CancellationException

/*
 * Cidade de origem ou destino da viagem, gravada no formato "Cidade/UF" (ex.: "Goiânia/GO").
 * UF e cidade são escolhidas nas listas da API de localidades do IBGE (dados/remoto/Localidades).
 * Sem internet, o campo vira texto livre no mesmo formato — o cadastro não fica travado.
 */

/** Só a cidade de "Goiânia/GO" → "Goiânia". Texto sem barra volta inteiro. */
internal fun cidadeDe(valor: String): String = valor.substringBeforeLast('/').trim()

private fun ufDe(valor: String): String? =
    valor.substringAfterLast('/', "").trim().uppercase().takeIf { it.length == 2 }

/** Executa a busca; devolve null se falhar (sem rede, API fora do ar). */
private suspend fun <T> buscar(bloco: suspend () -> T): T? = try {
    bloco()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    null
}

@Composable
internal fun CampoCidade(
    rotulo: String,
    valor: String,
    aoMudar: (String) -> Unit,
    erro: String?,
    modifier: Modifier = Modifier,
) {
    val uf = ufDe(valor)
    val cidade = cidadeDe(valor)
    var semConexao by rememberSaveable { mutableStateOf(false) }

    // null enquanto carrega.
    val estados by produceState<List<Estado>?>(null) {
        value = buscar { Localidades.estados() } ?: run { semConexao = true; null }
    }
    val cidades by produceState<List<String>?>(null, uf) {
        value = null
        if (uf != null) value = buscar { Localidades.municipios(uf) } ?: run { semConexao = true; null }
    }

    if (semConexao) {
        EixoCampo(
            valor = valor,
            aoMudar = aoMudar,
            rotulo = rotulo,
            modifier = modifier,
            placeholder = "Cidade/UF",
            dica = "Sem conexão com o IBGE: digite no formato Cidade/UF.",
            erro = erro,
            teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        )
        return
    }

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
        EixoSeletor(
            rotulo = "UF",
            opcoes = estados.orEmpty(),
            selecionado = estados?.firstOrNull { it.sigla == uf },
            // Trocar de UF limpa a cidade: ela pertence à UF anterior.
            aoSelecionar = { novo -> if (novo.sigla != uf) aoMudar("/${novo.sigla}") },
            textoDe = { it.sigla },
            detalheDe = { it.nome },
            placeholder = if (estados == null) "…" else "UF",
            habilitado = estados != null,
            modifier = Modifier.weight(1f),
        )
        EixoSeletor(
            rotulo = rotulo,
            opcoes = cidades.orEmpty(),
            selecionado = cidade.ifBlank { null },
            aoSelecionar = { aoMudar("$it/$uf") },
            placeholder = when {
                uf == null -> "Escolha a UF"
                cidades == null -> "Carregando cidades…"
                else -> "Selecionar"
            },
            habilitado = cidades != null,
            buscavel = true,
            erro = erro,
            modifier = Modifier.weight(2.6f),
        )
    }
}
