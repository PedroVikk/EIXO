package app.eixo.ui.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.eixo.ui.componentes.EixoAlerta
import app.eixo.ui.componentes.EixoBotao
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoCartao
import app.eixo.ui.componentes.EixoCartaoEficiencia
import app.eixo.ui.componentes.EixoDialogoConfirmacao
import app.eixo.ui.componentes.EixoEstadoVazio
import app.eixo.ui.componentes.EixoEtiqueta
import app.eixo.ui.componentes.EixoKpi
import app.eixo.ui.componentes.EixoLinhaLista
import app.eixo.ui.componentes.EixoLinhaValor
import app.eixo.ui.componentes.EixoMedidor
import app.eixo.ui.componentes.EixoOdometro
import app.eixo.ui.componentes.EixoPlaca
import app.eixo.ui.componentes.EixoResumoNumeros
import app.eixo.ui.componentes.EixoRotuloSecao
import app.eixo.ui.componentes.NumeroResumo
import app.eixo.ui.componentes.Tom
import app.eixo.ui.componentes.VarianteBotao
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoEspaco
import app.eixo.ui.design.EixoTipo

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SecaoEtiquetas() {
    Secao("Etiquetas de status", "EixoEtiqueta(texto, tom, pulsando, compacta)") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2), verticalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
            EixoEtiqueta("Planejada", tom = Tom.Neutro)
            EixoEtiqueta("Em andamento", tom = Tom.Acao, pulsando = true)
            EixoEtiqueta("Finalizada", tom = Tom.Sucesso)
            EixoEtiqueta("Atenção", tom = Tom.Atencao)
            EixoEtiqueta("Crítico", tom = Tom.Critico)
            EixoEtiqueta("Pré-preenchido", tom = Tom.Acao, compacta = true)
            EixoEtiqueta("Opcional", tom = Tom.Neutro, compacta = true)
        }
        Text("Só \"em andamento\" pulsa: é o único estado que muda sozinho.", style = EixoTipo.apoio, color = Eixo.cores.textoFraco)
    }
}

@Composable
internal fun SecaoNumeros() {
    Secao("Números", "EixoKpi · EixoResumoNumeros · EixoOdometro · EixoPlaca") {
        EixoResumoNumeros(
            listOf(
                NumeroResumo("5", "Caminhões"),
                NumeroResumo("1", "Em viagem", Tom.Acao),
                NumeroResumo("4", "Livres"),
                NumeroResumo("2", "Pendências", Tom.Atencao),
            ),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
            EixoKpi("Combustível", "R$ 31.520", apoio = "diesel + ARLA", modifier = Modifier.weight(1f))
            EixoKpi("Média", "1,93 km/L", apoio = "ponderada", tomValor = Tom.Atencao, modifier = Modifier.weight(1f))
        }
        EixoCartao(espacamento = EixoEspaco.s3) {
            EixoRotuloSecao("Odômetro grande e pequeno")
            EixoOdometro(153_980)
            EixoOdometro(96_420, pequeno = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
            EixoPlaca("RTA4B12")
            EixoPlaca("QXZ7C45")
        }
    }
}

@Composable
internal fun SecaoEficiencia() {
    Secao("Eficiência de consumo", "EixoMedidor · EixoCartaoEficiencia") {
        EixoCartaoEficiencia("RTA4B12", real = 1.96, esperado = 2.00, descricao = "Scania R450", rodape = "3 abastecimentos · 1.550 km")
        EixoCartaoEficiencia("PLM2D88", real = 1.75, esperado = 1.80, descricao = "Actros 2651")
        EixoCartaoEficiencia("KJH5E31", real = 1.55, esperado = 2.05, descricao = "DAF XF 480")
        EixoCartao {
            EixoRotuloSecao("Medidor sozinho")
            EixoMedidor(real = 2.12, esperado = 2.00, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
internal fun SecaoAlertas() {
    Secao("Alertas", "EixoAlerta(titulo, icone, descricao, tom)") {
        EixoAlerta("2 caminhões consumindo acima do previsto", Icons.Outlined.Speed, descricao = "PLM2D88 · KJH5E31 · este mês", tom = Tom.Critico)
        EixoAlerta("1 abastecimento sem viagem", Icons.Outlined.Route, descricao = "QXZ7C45 · ontem, 07:15", tom = Tom.Atencao)
        EixoAlerta("Viagem #000126 sai hoje às 14:00", Icons.Outlined.Route, descricao = "Anápolis → Uberlândia · Carlos Nunes", tom = Tom.Acao)
        EixoAlerta("Abastecimento salvo", Icons.Outlined.CheckCircle, descricao = "Viagem #000125 · 3 fotos anexadas", tom = Tom.Sucesso)
    }
}

@Composable
internal fun SecaoListas() {
    Secao("Listas", "EixoLinhaLista · EixoLinhaValor · EixoRotuloSecao") {
        EixoRotuloSecao("3 viagens", acao = "Ver todas", aoClicarAcao = {})
        EixoLinhaLista(
            titulo = "Goiânia → São Paulo",
            subtitulo = "#000125 · João da Silva",
            apoio = "1.550 km · 3 abastecimentos",
            inicio = { EixoPlaca("RTA4B12") },
            fim = { EixoEtiqueta("Em viagem", tom = Tom.Acao, pulsando = true) },
            aoClicar = {},
        )
        EixoLinhaLista(
            titulo = "Jataí → Santos",
            subtitulo = "#000122 · Pedro Alves",
            apoio = "Check-out não feito",
            tomApoio = Tom.Critico,
            inicio = { EixoPlaca("KJH5E31") },
            fim = { EixoEtiqueta("Pendente", tom = Tom.Critico) },
            destaque = Tom.Critico,
            aoClicar = {},
        )
        EixoLinhaLista(titulo = "Soja em grão", subtitulo = "Cooperativa Vale Verde · NF 123456", aoClicar = {})
        EixoCartao {
            EixoLinhaValor("KM", "153.980")
            EixoLinhaValor("Litros", "390,00 L")
            EixoLinhaValor("Valor total", "R$ 2.769,00")
            EixoLinhaValor("Posto", "Auto Posto Vale", numerico = false, ultima = true)
        }
    }
}

@Composable
internal fun SecaoEstrutura() {
    var dialogo by rememberSaveable { mutableStateOf(false) }
    Secao("Estrutura", "EixoTela · EixoCabecalho · EixoRodapeAcao · EixoBarraInferior · EixoEstadoVazio · EixoDialogoConfirmacao") {
        Text(
            "Toda tela começa com EixoTela. O cabeçalho petróleo é EixoCabecalho; a ação principal vai em EixoRodapeAcao.",
            style = EixoTipo.apoio, color = Eixo.cores.textoFraco,
        )
        // Fora do topo da tela, o cabeçalho não deve reservar a altura da barra de status.
        Column(Modifier.padding(vertical = 4.dp).consumeWindowInsets(WindowInsets.statusBars)) {
            EixoCabecalho(titulo = "Viagem #000125", sobretitulo = "Em andamento", aoVoltar = {}) {
                Text("Goiânia → São Paulo", style = EixoTipo.tituloTela, color = Eixo.cores.cabecalhoTexto)
                EixoOdometro(153_500, pequeno = true)
            }
        }
        EixoCartao {
            EixoEstadoVazio(
                icone = Icons.Outlined.Inventory2,
                titulo = "Nenhuma carga cadastrada",
                descricao = "Cadastre a primeira carga para poder vinculá-la a uma viagem.",
            )
        }
        EixoBotao("Abrir diálogo de confirmação", aoClicar = { dialogo = true }, variante = VarianteBotao.Secundario, icone = Icons.Outlined.PhotoCamera)
        if (dialogo) {
            EixoDialogoConfirmacao(
                titulo = "Excluir \"Soja em grão\"?",
                texto = "A carga sai da lista. Essa ação não pode ser desfeita.",
                textoConfirmar = "Excluir carga",
                perigoso = true,
                aoConfirmar = { dialogo = false },
                aoCancelar = { dialogo = false },
            )
        }
    }
}
