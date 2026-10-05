package app.eixo.ui.telas.cargas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import app.eixo.dados.Carga
import app.eixo.dados.Dados
import app.eixo.dados.StatusViagem
import app.eixo.dados.Viagem
import app.eixo.ui.componentes.EixoBotao
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoCampo
import app.eixo.ui.componentes.EixoEstadoVazio
import app.eixo.ui.componentes.EixoEtiqueta
import app.eixo.ui.componentes.EixoLinhaLista
import app.eixo.ui.componentes.EixoRodapeAcao
import app.eixo.ui.componentes.EixoRotuloSecao
import app.eixo.ui.componentes.EixoTela
import app.eixo.ui.componentes.Tom
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoEspaco
import app.eixo.ui.design.EixoRaio
import app.eixo.util.formatarKm

/*
 * ================================================================================
 *  TELA DE REFERÊNCIA — copie este padrão nas telas do seu módulo.
 * ================================================================================
 *  1. A tela recebe callbacks de navegação; nunca conhece o NavController.
 *  2. Lê os dados com `Dados.xxx.itens.collectAsState()` — atualiza sozinha.
 *  3. Esqueleto sempre com EixoTela + EixoCabecalho (+ EixoRodapeAcao / barra).
 *  4. Lista longa: `rolavel = false` e LazyColumn com `Modifier.weight(1f)`.
 *  5. Sem cor, fonte ou espaçamento literal: tudo de Eixo.cores, EixoTipo, EixoEspaco.
 */

/** Situação da carga derivada das viagens — exemplo de regra que cruza duas entidades. */
internal fun situacaoDaCarga(carga: Carga, viagens: List<Viagem>): Pair<String, Tom>? {
    val viagem = viagens.firstOrNull { it.cargaId == carga.id && it.status in setOf(StatusViagem.EM_ANDAMENTO, StatusViagem.PLANEJADA) }
    return when (viagem?.status) {
        StatusViagem.EM_ANDAMENTO -> "Em viagem" to Tom.Acao
        StatusViagem.PLANEJADA -> "Agendada" to Tom.Neutro
        else -> null
    }
}

@Composable
fun CargasTela(
    barra: @Composable () -> Unit,
    aoNova: () -> Unit,
    aoAbrir: (cargaId: String) -> Unit,
) {
    val c = Eixo.cores
    val cargas by Dados.cargas.itens.collectAsState()
    val viagens by Dados.viagens.itens.collectAsState()
    var busca by rememberSaveable { mutableStateOf("") }

    val filtradas = cargas.filter {
        busca.isBlank() || listOf(it.descricao, it.cliente, it.notaFiscal).any { campo -> campo.contains(busca.trim(), ignoreCase = true) }
    }.sortedBy { it.descricao }

    EixoTela(
        cabecalho = { EixoCabecalho("Cargas", sobretitulo = "Cadastro") },
        rodape = { EixoRodapeAcao { EixoBotao("Nova carga", aoClicar = aoNova, icone = Icons.Outlined.Add) } },
        barraInferior = barra,
        rolavel = false,
    ) {
        EixoCampo(
            valor = busca,
            aoMudar = { busca = it },
            rotulo = "Buscar",
            placeholder = "Descrição, cliente ou nota fiscal",
            teclado = KeyboardOptions(imeAction = ImeAction.Search),
            iconeFinal = { Icon(Icons.Outlined.Search, contentDescription = null, tint = c.textoTenue) },
        )
        EixoRotuloSecao(
            when {
                busca.isNotBlank() -> "${filtradas.size} de ${cargas.size} cargas"
                else -> "${cargas.size} cargas cadastradas"
            },
        )

        if (filtradas.isEmpty()) {
            EixoEstadoVazio(
                icone = Icons.Outlined.Inventory2,
                titulo = if (cargas.isEmpty()) "Nenhuma carga cadastrada" else "Nada encontrado",
                descricao = if (cargas.isEmpty()) "Cadastre a primeira carga para poder vinculá-la a uma viagem."
                else "Nenhuma carga com \"$busca\" na descrição, no cliente ou na nota fiscal.",
            )
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
                items(filtradas, key = { it.id }) { carga ->
                    val situacao = situacaoDaCarga(carga, viagens)
                    EixoLinhaLista(
                        titulo = carga.descricao,
                        subtitulo = "${carga.cliente} · NF ${carga.notaFiscal.ifBlank { "—" }}",
                        apoio = "${carga.tipo.rotulo} · ${formatarKm(carga.pesoKg)} kg",
                        inicio = {
                            Box(
                                Modifier.size(38.dp).clip(EixoRaio.sm).background(c.superficie2),
                                contentAlignment = Alignment.Center,
                            ) { Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = c.textoFraco, modifier = Modifier.size(20.dp)) }
                        },
                        fim = if (situacao != null) {
                            { EixoEtiqueta(situacao.first, tom = situacao.second, pulsando = situacao.second == Tom.Acao) }
                        } else null,
                        aoClicar = { aoAbrir(carga.id) },
                    )
                }
            }
        }
    }
}
