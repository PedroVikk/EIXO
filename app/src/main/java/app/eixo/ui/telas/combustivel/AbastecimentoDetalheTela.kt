package app.eixo.ui.telas.combustivel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.eixo.dados.Dados
import app.eixo.ui.componentes.EixoAlerta
import app.eixo.ui.componentes.EixoBotao
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoCampo
import app.eixo.ui.componentes.EixoEstadoVazio
import app.eixo.ui.componentes.EixoRodapeAcao
import app.eixo.ui.componentes.EixoRotuloSecao
import app.eixo.ui.componentes.EixoTela
import app.eixo.ui.componentes.Tom
import app.eixo.util.formatarDataHora
import app.eixo.util.formatarKm
import app.eixo.util.formatarLitros
import app.eixo.util.formatarMoeda

/*
 * DETALHE DO ABASTECIMENTO — somente leitura, com atalho para editar.
 * Mostra só o que o modelo `Abastecimento` guarda (fotos e forma de pagamento ficam fora do escopo).
 */

@Composable
fun AbastecimentoDetalheTela(
    abastecimentoId: String,
    aoVoltar: () -> Unit,
    aoEditar: () -> Unit,
) {
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
        if (a.viagemId == null) {
            EixoAlerta(
                titulo = "Abastecimento avulso",
                descricao = "Registrado sem viagem vinculada.",
                icone = Icons.Outlined.LinkOff,
                tom = Tom.Atencao,
            )
        }

        EixoRotuloSecao("Abastecimento")
        DetalheItem("Caminhão", caminhao?.let { "${it.placa} · ${it.modelo}" } ?: "—")
        DetalheItem("Motorista", motorista?.nome ?: "—")
        DetalheItem("Data e hora", formatarDataHora(a.dataHora), numerico = true)
        DetalheItem("KM no tacógrafo", "${formatarKm(a.km)} km", numerico = true)
        DetalheItem("Combustível", a.combustivel.rotulo)
        DetalheItem("Litros", formatarLitros(a.litros), numerico = true)
        DetalheItem("Posto", a.posto.ifBlank { "—" })
        DetalheItem("Tanque cheio", if (a.tanqueCheio) "Sim" else "Não")

        EixoRotuloSecao("Valores")
        DetalheItem("Valor total", formatarMoeda(a.valorTotal), numerico = true)
        DetalheItem("Preço por litro", "${formatarMoeda(a.precoLitro)}/L", numerico = true)
        if (a.arlaLitros != null && a.arlaValor != null) {
            DetalheItem("ARLA", "${formatarLitros(a.arlaLitros)} · ${formatarMoeda(a.arlaValor)}", numerico = true)
        }
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