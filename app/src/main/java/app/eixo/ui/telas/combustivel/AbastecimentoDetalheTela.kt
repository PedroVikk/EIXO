package app.eixo.ui.telas.combustivel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.eixo.dados.Dados
import app.eixo.ui.componentes.EixoAlerta
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
import app.eixo.ui.design.EixoRaio
import app.eixo.util.formatarDataHora
import app.eixo.util.formatarKm
import app.eixo.util.formatarLitros
import app.eixo.util.formatarMoeda

/*
 * DETALHE DO ABASTECIMENTO (mockup).
 *
 * O que é REAL: caminhão, motorista, data, KM, combustível, litros, valor, posto, tanque cheio e ARLA.
 * O que é MOCKUP: forma de pagamento, data do pagamento e as fotos obrigatórias.
 * O modelo `Abastecimento` ainda não tem esses campos; quando a base criar (por exemplo
 * `formaPagamento`, `pagoEm` e uma lista de fotos), basta trocar os trechos marcados com MOCKUP.
 */

private data class FotoObrigatoria(val titulo: String, val descricao: String, val icone: ImageVector)

@Composable
fun AbastecimentoDetalheTela(
    abastecimentoId: String,
    aoVoltar: () -> Unit,
    aoEditar: () -> Unit,
) {
    val c = Eixo.cores
    val todos by Dados.abastecimentos.itens.collectAsState()
    val caminhoes by Dados.caminhoes.itens.collectAsState()
    val usuarios by Dados.usuarios.itens.collectAsState()
    val a = todos.firstOrNull { it.id == abastecimentoId }

    if (a == null) {
        EixoTela(
            cabecalho = { EixoCabecalho("Abastecimento", sobretitulo = "Combustível", aoVoltar = aoVoltar) },
            rodape = { EixoRodapeAcao { EixoBotao("Voltar para a lista", aoClicar = aoVoltar) } },
        ) {
            EixoEstadoVazio(
                icone = Icons.Outlined.LocalGasStation,
                titulo = "Abastecimento não encontrado",
                descricao = "Ele pode ter sido excluído. Volte para a lista e escolha outro.",
            )
        }
        return
    }

    val caminhao = caminhoes.firstOrNull { it.id == a.caminhaoId }
    val motorista = usuarios.firstOrNull { it.id == a.motoristaId }

    // MOCKUP: fotos que o app exigiria em cada abastecimento.
    val fotos = buildList {
        add(FotoObrigatoria("Painel / tacógrafo", "Mostra o KM no momento do abastecimento", Icons.Outlined.Speed))
        add(FotoObrigatoria("Bomba de combustível", "Litros e valor exibidos na bomba", Icons.Outlined.LocalGasStation))
        add(FotoObrigatoria("Comprovante de pagamento", "Nota ou cupom com valor e data", Icons.Outlined.Receipt))
        if (a.arlaLitros != null) add(FotoObrigatoria("ARLA", "Bomba ou comprovante do ARLA", Icons.Outlined.WaterDrop))
    }

    EixoTela(
        cabecalho = {
            EixoCabecalho(
                titulo = "${caminhao?.placa ?: "—"} · ${formatarLitros(a.litros)}",
                sobretitulo = "Abastecimento",
                aoVoltar = aoVoltar,
            )
        },
        rodape = {
            EixoRodapeAcao {
                EixoBotao("Editar abastecimento", aoClicar = aoEditar, icone = Icons.Outlined.Edit)
            }
        },
    ) {
        // MOCKUP: forma e data do pagamento. Hoje usamos a própria data do abastecimento.
        EixoAlerta(
            titulo = "Pago em ${formatarDataHora(a.dataHora)}",
            descricao = "${formatarMoeda(a.valorTotal)} · Cartão frota (exemplo)",
            icone = Icons.Outlined.CheckCircle,
            tom = Tom.Sucesso,
        )

        EixoRotuloSecao("Abastecimento")
        DetalheItem("Caminhão", caminhao?.let { "${it.placa} · ${it.modelo}" } ?: "—")
        DetalheItem("Motorista", motorista?.nome ?: "—")
        DetalheItem("Data e hora", formatarDataHora(a.dataHora), numerico = true)
        DetalheItem("KM no tacógrafo", "${formatarKm(a.km)} km", numerico = true)
        DetalheItem("Combustível", a.combustivel.rotulo)
        DetalheItem("Litros", formatarLitros(a.litros), numerico = true)
        DetalheItem("Posto", a.posto.ifBlank { "—" })
        DetalheItem("Tanque cheio", if (a.tanqueCheio) "Sim" else "Não")

        EixoRotuloSecao("Pagamento")
        DetalheItem("Valor total", formatarMoeda(a.valorTotal), numerico = true)
        DetalheItem("Preço por litro", "${formatarMoeda(a.precoLitro)}/L", numerico = true)
        DetalheItem("Forma de pagamento", "Cartão frota (exemplo)") // MOCKUP
        DetalheItem("Pago em", formatarDataHora(a.dataHora), numerico = true) // MOCKUP
        if (a.arlaLitros != null && a.arlaValor != null) {
            DetalheItem("ARLA", "${formatarLitros(a.arlaLitros)} · ${formatarMoeda(a.arlaValor)}", numerico = true)
        }

        EixoRotuloSecao("Fotos obrigatórias · ${fotos.size} de ${fotos.size} anexadas")
        fotos.forEach { foto ->
            EixoLinhaLista(
                titulo = foto.titulo,
                subtitulo = foto.descricao,
                apoio = "Obrigatória",
                inicio = {
                    Box(
                        Modifier.size(56.dp).clip(EixoRaio.sm).background(c.superficie2),
                        contentAlignment = Alignment.Center,
                    ) { Icon(foto.icone, contentDescription = null, tint = c.textoFraco, modifier = Modifier.size(24.dp)) }
                },
                fim = { EixoEtiqueta("Anexada", tom = Tom.Sucesso) },
                aoClicar = {},
            )
        }
        EixoAlerta(
            titulo = "Protótipo",
            descricao = "Forma de pagamento, data do pagamento e fotos são de exemplo. O modelo ainda não guarda esses dados.",
            icone = Icons.Outlined.Info,
            tom = Tom.Neutro,
        )
    }
}

/** Linha somente leitura: reaproveita o campo do design system para manter o mesmo visual do formulário. */
@Composable
private fun DetalheItem(rotulo: String, valor: String, numerico: Boolean = false) {
    EixoCampo(
        valor = valor,
        aoMudar = {},
        rotulo = rotulo,
        somenteLeitura = true,
        numerico = numerico,
    )
}