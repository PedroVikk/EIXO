package app.eixo.ui.telas.usuarios

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.eixo.dados.Dados
import app.eixo.dados.DadosMotorista
import app.eixo.dados.Perfil
import app.eixo.dados.Usuario
import app.eixo.ui.componentes.EixoAlerta
import app.eixo.ui.componentes.EixoBotao
import app.eixo.ui.componentes.EixoCabecalho
import app.eixo.ui.componentes.EixoCampo
import app.eixo.ui.componentes.EixoCampoData
import app.eixo.ui.componentes.EixoDialogoConfirmacao
import app.eixo.ui.componentes.EixoEstadoVazio
import app.eixo.ui.componentes.EixoEtiqueta
import app.eixo.ui.componentes.EixoLinhaLista
import app.eixo.ui.componentes.EixoNotaRodape
import app.eixo.ui.componentes.EixoRodapeAcao
import app.eixo.ui.componentes.EixoRotuloSecao
import app.eixo.ui.componentes.EixoSegmentado
import app.eixo.ui.componentes.EixoSeletor
import app.eixo.ui.componentes.EixoTela
import app.eixo.ui.componentes.TamanhoBotao
import app.eixo.ui.componentes.Tom
import app.eixo.ui.componentes.VarianteBotao
import app.eixo.ui.design.Eixo
import app.eixo.ui.design.EixoEspaco
import app.eixo.ui.design.EixoRaio
import app.eixo.util.formatarData
import java.time.LocalDate

/*
 * MÓDULO ACESSO — cadastro de motoristas e administradores.
 * Dados: Dados.usuarios (Repositorio<Usuario>). Motorista tem `motorista: DadosMotorista`.
 * Telas de referência no design system: "Novo acesso" (gestor).
 * Padrão a copiar: ui/telas/cargas/.
 */

/** Helper para calcular status da CNH do motorista. */
private fun statusCnh(usuario: Usuario): Pair<String, Tom?>? {
    val motorista = usuario.motorista ?: return null
    val hoje = LocalDate.now()
    val validade = motorista.validadeCnh
    val limiteVencimento = hoje.plusDays(30)

    return when {
        validade.isBefore(hoje) -> "CNH vencida em ${formatarData(validade)}" to Tom.Critico
        !validade.isAfter(limiteVencimento) -> "CNH vence em ${formatarData(validade)}" to Tom.Atencao
        else -> "CNH válida até ${formatarData(validade)}" to null
    }
}

/** Lista de pessoas com filtro por perfil (Motoristas | Administradores). */
@Composable
fun UsuariosTela(
    barra: @Composable () -> Unit,
    aoNovo: () -> Unit,
    aoAbrir: (usuarioId: String) -> Unit,
) {
    val c = Eixo.cores
    val configuracao = LocalConfiguration.current
    val ePaisagem = configuracao.orientation == Configuration.ORIENTATION_LANDSCAPE

    val usuarios by Dados.usuarios.itens.collectAsState()
    var perfilIndice by rememberSaveable { mutableIntStateOf(0) } // 0 = Motoristas, 1 = Administradores

    val perfilAlvo = if (perfilIndice == 0) Perfil.MOTORISTA else Perfil.ADMIN
    val filtrados = usuarios.filter { it.perfil == perfilAlvo }
    val rotuloPerfil = if (perfilIndice == 0) "motoristas" else "administradores"

    EixoTela(
        cabecalho = {
            EixoCabecalho(
                titulo = "Pessoas",
                sobretitulo = "Cadastro",
                acoes = {
                    if (ePaisagem) {
                        EixoBotao(
                            texto = "Nova pessoa",
                            aoClicar = aoNovo,
                            icone = Icons.Outlined.Add,
                            tamanho = TamanhoBotao.Pequeno,
                            larguraTotal = false,
                        )
                    }
                },
            )
        },
        rodape = if (!ePaisagem) {
            { EixoRodapeAcao { EixoBotao("Nova pessoa", aoClicar = aoNovo, icone = Icons.Outlined.Add) } }
        } else null,
        barraInferior = barra,
        rolavel = false,
    ) {
        EixoSegmentado(
            opcoes = listOf("Motoristas", "Administradores"),
            selecionado = perfilIndice,
            aoSelecionar = { perfilIndice = it },
        )

        EixoRotuloSecao("${filtrados.size} $rotuloPerfil")

        if (filtrados.isEmpty()) {
            EixoEstadoVazio(
                icone = Icons.Outlined.Groups,
                titulo = "Nenhum $rotuloPerfil cadastrado",
                descricao = "Cadastre o primeiro $rotuloPerfil no botão abaixo.",
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(EixoEspaco.s2),
            ) {
                items(filtrados, key = { it.id }) { usuario ->
                    val infoCnh = statusCnh(usuario)
                    val cnhVencida = infoCnh?.second == Tom.Critico
                    val cnhVencendo = infoCnh?.second == Tom.Atencao

                    val etiquetaFim: (@Composable () -> Unit)? = when {
                        !usuario.ativo -> {
                            { EixoEtiqueta("Inativo", tom = Tom.Neutro) }
                        }
                        cnhVencida -> {
                            { EixoEtiqueta("Vencida", tom = Tom.Critico) }
                        }
                        cnhVencendo -> {
                            { EixoEtiqueta("Vence logo", tom = Tom.Atencao) }
                        }
                        else -> null
                    }

                    val subtituloTexto = if (usuario.perfil == Perfil.MOTORISTA) {
                        val cat = usuario.motorista?.categoriaCnh
                        "Matrícula ${usuario.login}" + if (!cat.isNullOrBlank()) " · CNH $cat" else ""
                    } else {
                        "E-mail ${usuario.login}"
                    }

                    EixoLinhaLista(
                        titulo = usuario.nome,
                        subtitulo = subtituloTexto,
                        apoio = infoCnh?.first,
                        tomApoio = infoCnh?.second,
                        inicio = {
                            Box(
                                Modifier
                                    .size(38.dp)
                                    .clip(EixoRaio.sm)
                                    .background(c.superficie2),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = c.textoFraco,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                        fim = etiquetaFim,
                        aoClicar = { aoAbrir(usuario.id) },
                    )
                }
            }
        }
    }
}

/**
 * Cadastro e edição. `usuarioId == null` = novo.
 * Campos comuns: nome, login (e-mail p/ admin, matrícula p/ motorista), perfil, ativo.
 * Só para motorista: CPF, CNH, categoria, validade da CNH (EixoCampoData), telefone.
 */
@Composable
fun UsuarioFormTela(usuarioId: String?, aoVoltar: () -> Unit) {
    val c = Eixo.cores
    val usuarios by Dados.usuarios.itens.collectAsState()
    val viagens by Dados.viagens.itens.collectAsState()
    val abastecimentos by Dados.abastecimentos.itens.collectAsState()

    val existente = remember(usuarioId) { usuarioId?.let { Dados.usuarios.buscar(it) } }

    if (usuarioId != null && existente == null) {
        EixoTela(
            cabecalho = {
                EixoCabecalho(
                    titulo = "Pessoa não encontrada",
                    sobretitulo = "Pessoas",
                    aoVoltar = aoVoltar,
                )
            },
        ) {
            EixoEstadoVazio(
                icone = Icons.Outlined.ErrorOutline,
                titulo = "Cadastro não encontrado",
                descricao = "Não foi possível encontrar uma pessoa com o ID informado.",
                acao = {
                    EixoBotao("Voltar para a lista", aoClicar = aoVoltar, variante = VarianteBotao.Secundario)
                },
            )
        }
        return
    }

    // Verificação de vínculos reais (por ID) em viagens ou abastecimentos
    val viagemVinculada = existente?.let { u -> viagens.firstOrNull { it.motoristaId == u.id } }
    val abastecimentoVinculado = existente?.let { u -> abastecimentos.firstOrNull { it.motoristaId == u.id } }
    val possuiVinculo = viagemVinculada != null || abastecimentoVinculado != null

    var perfil by rememberSaveable { mutableStateOf(existente?.perfil ?: Perfil.MOTORISTA) }
    var nome by rememberSaveable { mutableStateOf(existente?.nome ?: "") }
    var login by rememberSaveable { mutableStateOf(existente?.login ?: "") }
    var ativo by rememberSaveable { mutableStateOf(existente?.ativo ?: true) }

    // Campos específicos do motorista
    var cpf by rememberSaveable { mutableStateOf(existente?.motorista?.cpf ?: "") }
    var cnh by rememberSaveable { mutableStateOf(existente?.motorista?.cnh ?: "") }
    var categoriaCnh by rememberSaveable { mutableStateOf(existente?.motorista?.categoriaCnh ?: "") }
    var validadeCnhEpoch by rememberSaveable { mutableStateOf(existente?.motorista?.validadeCnh?.toEpochDay()) }
    var telefone by rememberSaveable { mutableStateOf(existente?.motorista?.telefone ?: "") }

    var tentouSalvar by rememberSaveable { mutableStateOf(false) }
    var confirmarExclusao by rememberSaveable { mutableStateOf(false) }

    val validadeCnh = validadeCnhEpoch?.let { LocalDate.ofEpochDay(it) }

    // Validações
    val erroNome = if (nome.isBlank()) "Informe o nome completo." else null
    val loginLimpo = login.trim()
    val eEmailValidoCom = android.util.Patterns.EMAIL_ADDRESS.matcher(loginLimpo).matches() &&
        loginLimpo.endsWith(".com", ignoreCase = true)

    val erroLogin = when {
        loginLimpo.isBlank() -> if (perfil == Perfil.MOTORISTA) "Informe a matrícula." else "Informe o e-mail."
        perfil == Perfil.ADMIN && !eEmailValidoCom ->
            "Informe um e-mail válido, como nome@dominio.com."
        usuarios.any { it.id != existente?.id && it.login.equals(loginLimpo, ignoreCase = true) } ->
            "Este login/e-mail já está cadastrado."
        else -> null
    }

    val erroCpf = if (perfil == Perfil.MOTORISTA && cpf.isBlank()) "Informe o CPF." else null
    val erroCnh = if (perfil == Perfil.MOTORISTA && cnh.isBlank()) "Informe a CNH." else null
    val erroCategoria = if (perfil == Perfil.MOTORISTA && categoriaCnh.isBlank()) "Escolha a categoria da CNH." else null
    val erroValidade = if (perfil == Perfil.MOTORISTA && validadeCnh == null) "Selecione a validade da CNH." else null
    val erroTelefone = if (perfil == Perfil.MOTORISTA && telefone.isBlank()) "Informe o telefone." else null

    val valido = listOfNotNull(erroNome, erroLogin, erroCpf, erroCnh, erroCategoria, erroValidade, erroTelefone).isEmpty()

    fun salvar() {
        tentouSalvar = true
        if (!valido) return

        val dadosMotorista = if (perfil == Perfil.MOTORISTA) {
            DadosMotorista(
                cpf = cpf.trim(),
                cnh = cnh.trim(),
                categoriaCnh = categoriaCnh.trim(),
                validadeCnh = validadeCnh!!,
                telefone = telefone.trim(),
            )
        } else {
            null
        }

        Dados.usuarios.salvar(
            Usuario(
                id = existente?.id ?: Dados.usuarios.novoId(),
                nome = nome.trim(),
                login = loginLimpo,
                perfil = perfil,
                ativo = ativo,
                motorista = dadosMotorista,
            ),
        )
        aoVoltar()
    }

    EixoTela(
        cabecalho = {
            EixoCabecalho(
                titulo = if (existente == null) "Novo cadastro" else "Editar cadastro",
                sobretitulo = "Pessoas",
                aoVoltar = aoVoltar,
                acoes = {
                    if (existente != null && !possuiVinculo) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(EixoRaio.sm)
                                .background(c.critico)
                                .clickable(role = Role.Button) {
                                    val aindaExiste = Dados.usuarios.buscar(existente.id) != null
                                    val aindaVinculado = viagens.any { it.motoristaId == existente.id } ||
                                        abastecimentos.any { it.motoristaId == existente.id }
                                    if (aindaExiste && !aindaVinculado) {
                                        confirmarExclusao = true
                                    }
                                }
                                .semantics { contentDescription = "Excluir pessoa" },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(19.dp),
                            )
                        }
                    }
                },
            )
        },
        rodape = {
            EixoRodapeAcao {
                EixoBotao(
                    texto = if (existente == null) "Cadastrar pessoa" else "Salvar alterações",
                    aoClicar = ::salvar,
                    habilitado = valido,
                )
                if (tentouSalvar && !valido) {
                    EixoNotaRodape("Preencha todos os campos obrigatórios")
                }
            }
        },
    ) {
        if (possuiVinculo) {
            val descricaoAlerta = when {
                viagemVinculada != null -> "Vinculada à viagem #${viagemVinculada.numero} · por isso não pode ser excluída."
                abastecimentoVinculado != null -> "Vinculada a registro de abastecimento · por isso não pode ser excluída."
                else -> "Possui registros vinculados no sistema · por isso não pode ser excluída."
            }
            EixoAlerta(
                titulo = "Pessoa vinculada no sistema",
                descricao = descricaoAlerta,
                icone = Icons.Outlined.Link,
                tom = Tom.Acao,
            )
        }

        EixoRotuloSecao("Perfil")
        EixoSegmentado(
            opcoes = listOf("Motorista", "Administrador"),
            selecionado = if (perfil == Perfil.MOTORISTA) 0 else 1,
            aoSelecionar = { perfil = if (it == 0) Perfil.MOTORISTA else Perfil.ADMIN },
        )

        EixoCampo(
            valor = nome,
            aoMudar = { nome = it },
            rotulo = "Nome completo",
            placeholder = "Ex.: João da Silva",
            erro = if (tentouSalvar) erroNome else null,
            teclado = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
        )

        EixoCampo(
            valor = login,
            aoMudar = { login = it },
            rotulo = if (perfil == Perfil.MOTORISTA) "Matrícula" else "E-mail",
            placeholder = if (perfil == Perfil.MOTORISTA) "1001" else "admin@eixo.app",
            dica = if (perfil == Perfil.MOTORISTA) "É o login do motorista no app." else "É o e-mail de acesso do administrador.",
            erro = if (tentouSalvar) erroLogin else null,
            numerico = (perfil == Perfil.MOTORISTA),
            teclado = KeyboardOptions(
                keyboardType = if (perfil == Perfil.MOTORISTA) KeyboardType.Number else KeyboardType.Email,
                imeAction = if (perfil == Perfil.MOTORISTA) ImeAction.Next else ImeAction.Done,
            ),
        )

        if (perfil == Perfil.MOTORISTA) {
            EixoRotuloSecao("Dados do motorista")

            EixoCampo(
                valor = cpf,
                aoMudar = { cpf = it },
                rotulo = "CPF",
                placeholder = "000.000.000-00",
                erro = if (tentouSalvar) erroCpf else null,
                numerico = true,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            )

            EixoCampo(
                valor = cnh,
                aoMudar = { cnh = it },
                rotulo = "CNH",
                placeholder = "00000000000",
                erro = if (tentouSalvar) erroCnh else null,
                numerico = true,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            )

            EixoSeletor(
                rotulo = "Categoria",
                opcoes = listOf("A", "B", "C", "D", "E", "AB", "AC", "AD", "AE"),
                selecionado = categoriaCnh.ifBlank { null },
                aoSelecionar = { categoriaCnh = it },
                placeholder = "Selecionar categoria",
                erro = if (tentouSalvar) erroCategoria else null,
            )

            EixoCampoData(
                rotulo = "Validade da CNH",
                data = validadeCnh,
                aoSelecionar = { validadeCnhEpoch = it.toEpochDay() },
                erro = if (tentouSalvar) erroValidade else null,
            )

            EixoCampo(
                valor = telefone,
                aoMudar = { telefone = it },
                rotulo = "Telefone",
                placeholder = "(62) 90000-0000",
                erro = if (tentouSalvar) erroTelefone else null,
                numerico = true,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            )
        }

        EixoRotuloSecao("Situação")
        EixoSegmentado(
            opcoes = listOf("Ativo", "Inativo"),
            selecionado = if (ativo) 0 else 1,
            aoSelecionar = { ativo = (it == 0) },
            tomSelecionado = if (ativo) Tom.Sucesso else Tom.Neutro,
        )
    }

    if (confirmarExclusao && existente != null) {
        EixoDialogoConfirmacao(
            titulo = "Excluir \"${existente.nome}\"?",
            texto = "A pessoa será removida do sistema. Essa ação não pode ser desfeita.",
            textoConfirmar = "Excluir pessoa",
            perigoso = true,
            aoConfirmar = {
                val aindaExiste = Dados.usuarios.buscar(existente.id) != null
                val aindaVinculado = viagens.any { it.motoristaId == existente.id } ||
                    abastecimentos.any { it.motoristaId == existente.id }
                if (aindaExiste && !aindaVinculado) {
                    Dados.usuarios.remover(existente.id)
                    confirmarExclusao = false
                    aoVoltar()
                } else {
                    confirmarExclusao = false
                }
            },
            aoCancelar = {
                confirmarExclusao = false
            },
        )
    }
}