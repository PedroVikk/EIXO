package app.eixo.ui.componentes

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoRaio
import app.eixo.ui.design.EixoTipo
import app.eixo.util.formatarData
import app.eixo.util.formatarDataHora
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

// O DatePicker do Material trabalha em milissegundos UTC à meia-noite.
private fun LocalDate.paraMillisUtc(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.paraDataUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
private fun BotaoDialogo(texto: String, aoClicar: () -> Unit, destaque: Boolean = false) {
    val c = Eixo.cores
    TextButton(onClick = aoClicar) {
        Text(texto.uppercase(), style = EixoTipo.botao,
            color = if (destaque) c.acao else c.textoFraco)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoData(inicial: LocalDate?, aoConfirmar: (LocalDate) -> Unit, aoFechar: () -> Unit) {
    val c = Eixo.cores
    val estado = rememberDatePickerState(initialSelectedDateMillis = (inicial ?: LocalDate.now()).paraMillisUtc())
    DatePickerDialog(
        onDismissRequest = aoFechar,
        confirmButton = {
            BotaoDialogo("Confirmar", destaque = true, aoClicar = {
                estado.selectedDateMillis?.let { aoConfirmar(it.paraDataUtc()) }
            })
        },
        dismissButton = { BotaoDialogo("Cancelar", aoFechar) },
        shape = EixoRaio.xl,
        colors = DatePickerDefaults.colors(containerColor = c.superficie),
    ) {
        DatePicker(state = estado, colors = DatePickerDefaults.colors(containerColor = c.superficie))
    }
}

/** Campo de data. Abre o calendário do sistema; mostra dd/MM/aaaa. */
@Composable
fun EixoCampoData(
    rotulo: String,
    data: LocalDate?,
    aoSelecionar: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Selecionar data",
    dica: String? = null,
    erro: String? = null,
) {
    val c = Eixo.cores
    var aberto by rememberSaveable { mutableStateOf(false) }
    MolduraCampo(rotulo, modifier, dica = dica, erro = erro) {
        CaixaCampo(
            focado = aberto, temErro = erro != null, somenteLeitura = false, grande = false,
            aoClicar = { aberto = true },
            descricao = "$rotulo: ${data?.let(::formatarData) ?: placeholder}",
        ) {
            Text(
                data?.let(::formatarData) ?: placeholder,
                style = if (data == null) EixoTipo.campo else EixoTipo.dado.copy(fontSize = 16.sp),
                color = if (data == null) c.textoTenue else c.texto,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = c.textoTenue, modifier = Modifier.size(20.dp))
        }
    }
    if (aberto) {
        DialogoData(data, aoConfirmar = { aoSelecionar(it); aberto = false }, aoFechar = { aberto = false })
    }
}

/** Data e hora em dois passos: calendário, depois relógio (24 h). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EixoCampoDataHora(
    rotulo: String,
    dataHora: LocalDateTime?,
    aoSelecionar: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Selecionar data e hora",
    dica: String? = null,
    erro: String? = null,
) {
    val c = Eixo.cores
    var passo by rememberSaveable { mutableStateOf(0) } // 0 fechado, 1 data, 2 hora
    var dataEscolhida by remember { mutableStateOf<LocalDate?>(null) }

    MolduraCampo(rotulo, modifier, dica = dica, erro = erro) {
        CaixaCampo(
            focado = passo != 0, temErro = erro != null, somenteLeitura = false, grande = false,
            aoClicar = { passo = 1 },
            descricao = "$rotulo: ${dataHora?.let(::formatarDataHora) ?: placeholder}",
        ) {
            Text(
                dataHora?.let(::formatarDataHora) ?: placeholder,
                style = if (dataHora == null) EixoTipo.campo else EixoTipo.dado.copy(fontSize = 16.sp),
                color = if (dataHora == null) c.textoTenue else c.texto,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = c.textoTenue, modifier = Modifier.size(20.dp))
        }
    }

    if (passo == 1) {
        DialogoData(
            inicial = dataHora?.toLocalDate(),
            aoConfirmar = { dataEscolhida = it; passo = 2 },
            aoFechar = { passo = 0 },
        )
    }
    if (passo == 2) {
        val inicial = dataHora?.toLocalTime() ?: LocalTime.of(6, 0)
        val estado = rememberTimePickerState(initialHour = inicial.hour, initialMinute = inicial.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { passo = 0 },
            containerColor = c.superficie,
            shape = EixoRaio.xl,
            title = { Text("Horário", style = EixoTipo.tituloSecao, color = c.texto) },
            text = { TimePicker(state = estado, colors = TimePickerDefaults.colors(containerColor = c.superficie)) },
            confirmButton = {
                BotaoDialogo("Confirmar", destaque = true, aoClicar = {
                    val data = dataEscolhida ?: LocalDate.now()
                    aoSelecionar(data.atTime(estado.hour, estado.minute))
                    passo = 0
                })
            },
            dismissButton = { BotaoDialogo("Voltar", { passo = 1 }) },
        )
    }
}
