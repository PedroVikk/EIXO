package app.eixo.ui.telas.cargas

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Link
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.eixo.dados.Carga
import app.eixo.dados.Dados
import app.eixo.dados.TipoCarga
import app.eixo.ui.componentes.EixoAlerta
import app.eixo.ui.componentes.EixoBotao
import app.eixo.ui.componentes.EixoBotaoCabecalho
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoCampo
import app.eixo.ui.componentes.EixoCampoInteiro
import app.eixo.ui.componentes.EixoDialogoConfirmacao
import app.eixo.ui.componentes.EixoRodapeAcao
import app.eixo.ui.componentes.EixoSeletor
import app.eixo.ui.componentes.EixoTela
import app.eixo.ui.componentes.Tom

/*
 * Formulário de referência. Padrões que ele demonstra:
 *  - mesma tela para criar (cargaId == null) e editar;
 *  - estado com rememberSaveable (sobrevive a girar a tela);
 *  - erros só aparecem depois da primeira tentativa de salvar — nada de tela
 *    vermelha antes de a pessoa digitar;
 *  - mensagem de erro diz o que fazer, não só que errou;
 *  - excluir pede confirmação e é bloqueado quando quebraria outra entidade.
 */

@Composable
fun CargaFormTela(cargaId: String?, aoVoltar: () -> Unit) {
    val existente = remember(cargaId) { cargaId?.let { Dados.cargas.buscar(it) } }
    val viagens by Dados.viagens.itens.collectAsState()

    var descricao by rememberSaveable { mutableStateOf(existente?.descricao ?: "") }
    var tipo by rememberSaveable { mutableStateOf(existente?.tipo) }
    var pesoKg by rememberSaveable { mutableStateOf(existente?.pesoKg?.toString() ?: "") }
    var notaFiscal by rememberSaveable { mutableStateOf(existente?.notaFiscal ?: "") }
    var cliente by rememberSaveable { mutableStateOf(existente?.cliente ?: "") }
    var observacao by rememberSaveable { mutableStateOf(existente?.observacao ?: "") }
    var tentouSalvar by rememberSaveable { mutableStateOf(false) }
    var confirmarExclusao by rememberSaveable { mutableStateOf(false) }

    // Validação: calculada sempre, exibida só depois da primeira tentativa.
    val erroDescricao = if (descricao.isBlank()) "Informe o que está sendo transportado." else null
    val erroTipo = if (tipo == null) "Escolha o tipo da carga." else null
    val erroPeso = if ((pesoKg.toLongOrNull() ?: 0L) <= 0L) "Informe o peso em quilos, maior que zero." else null
    val erroCliente = if (cliente.isBlank()) "Informe para quem é a carga." else null
    val valido = listOf(erroDescricao, erroTipo, erroPeso, erroCliente).all { it == null }

    val viagemVinculada = existente?.let { carga -> viagens.firstOrNull { it.cargaId == carga.id } }

    fun salvar() {
        tentouSalvar = true
        if (!valido) return
        Dados.cargas.salvar(
            Carga(
                id = existente?.id ?: Dados.cargas.novoId(),
                descricao = descricao.trim(),
                tipo = tipo!!,
                pesoKg = pesoKg.toLong(),
                notaFiscal = notaFiscal.trim(),
                cliente = cliente.trim(),
                observacao = observacao.trim(),
            ),
        )
        aoVoltar()
    }

    EixoTela(
        cabecalho = {
            EixoCabecalho(
                titulo = if (existente == null) "Nova carga" else "Editar carga",
                sobretitulo = "Cargas",
                aoVoltar = aoVoltar,
                acoes = {
                    if (existente != null && viagemVinculada == null) {
                        EixoBotaoCabecalho(Icons.Outlined.DeleteOutline, "Excluir carga") { confirmarExclusao = true }
                    }
                },
            )
        },
        rodape = {
            EixoRodapeAcao {
                EixoBotao(if (existente == null) "Cadastrar carga" else "Salvar alterações", aoClicar = ::salvar)
            }
        },
    ) {
        if (viagemVinculada != null) {
            EixoAlerta(
                titulo = "Vinculada à viagem #${viagemVinculada.numero}",
                descricao = "${viagemVinculada.rota} · por isso não pode ser excluída.",
                icone = Icons.Outlined.Link,
                tom = Tom.Acao,
            )
        }
        EixoCampo(
            valor = descricao, aoMudar = { descricao = it },
            rotulo = "Descrição",
            placeholder = "Ex.: Soja em grão",
            erro = if (tentouSalvar) erroDescricao else null,
            teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
        )
        EixoSeletor(
            rotulo = "Tipo de carga",
            opcoes = TipoCarga.entries,
            selecionado = tipo,
            aoSelecionar = { tipo = it },
            textoDe = { it.rotulo },
            erro = if (tentouSalvar) erroTipo else null,
        )
        EixoCampoInteiro(
            digitos = pesoKg, aoMudar = { pesoKg = it },
            rotulo = "Peso", sufixo = "kg",
            erro = if (tentouSalvar) erroPeso else null,
        )
        EixoCampo(
            valor = cliente, aoMudar = { cliente = it },
            rotulo = "Cliente",
            placeholder = "Quem contratou o frete",
            erro = if (tentouSalvar) erroCliente else null,
            teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        )
        EixoCampo(
            valor = notaFiscal, aoMudar = { notaFiscal = it.filter(Char::isDigit).take(9) },
            rotulo = "Nota fiscal",
            etiqueta = "Opcional", tomEtiqueta = Tom.Neutro,
            numerico = true,
            teclado = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number, imeAction = ImeAction.Next),
        )
        EixoCampo(
            valor = observacao, aoMudar = { observacao = it },
            rotulo = "Observação",
            etiqueta = "Opcional", tomEtiqueta = Tom.Neutro,
            placeholder = "Ex.: manter refrigerada",
            linhas = 3,
            teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Default),
        )
    }

    if (confirmarExclusao && existente != null) {
        EixoDialogoConfirmacao(
            titulo = "Excluir \"${existente.descricao}\"?",
            texto = "A carga sai da lista. Essa ação não pode ser desfeita.",
            textoConfirmar = "Excluir carga",
            perigoso = true,
            aoConfirmar = {
                Dados.cargas.remover(existente.id)
                confirmarExclusao = false
                aoVoltar()
            },
            aoCancelar = { confirmarExclusao = false },
        )
    }
}
