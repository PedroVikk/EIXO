package app.eixo.ui.navegacao

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.eixo.dados.Perfil
import app.eixo.dados.Sessao
import app.eixo.ui.catalogo.CatalogoTela
import app.eixo.ui.componentes.EixoBarraInferior
import app.eixo.ui.componentes.ItemBarra
import app.eixo.ui.design.EixoTema
import app.eixo.ui.telas.acesso.ContaTela
import app.eixo.ui.telas.acesso.LoginTela
import app.eixo.ui.telas.cargas.CargaFormTela
import app.eixo.ui.telas.cargas.CargasTela
import app.eixo.ui.telas.combustivel.AbastecimentoFormTela
import app.eixo.ui.telas.combustivel.AbastecimentosTela
import app.eixo.ui.telas.painel.PainelTela
import app.eixo.ui.telas.usuarios.UsuarioFormTela
import app.eixo.ui.telas.usuarios.UsuariosTela
import app.eixo.ui.telas.viagens.ViagemDetalheTela
import app.eixo.ui.telas.viagens.ViagemFormTela
import app.eixo.ui.telas.viagens.ViagensTela

/*
 * Mapa de navegação do app. Dono: quem cuida da base (componentes + navegação).
 * Os módulos NÃO mexem aqui — se uma tela precisa de um callback novo, combine com o dono.
 */

private fun itensDaBarra(perfil: Perfil): List<ItemBarra> = when (perfil) {
    Perfil.ADMIN -> listOf(
        ItemBarra(Rotas.PAINEL, "Painel", Icons.Outlined.SpaceDashboard),
        ItemBarra(Rotas.VIAGENS, "Viagens", Icons.Outlined.Route),
        ItemBarra(Rotas.CARGAS, "Cargas", Icons.Outlined.Inventory2),
        ItemBarra(Rotas.ABASTECIMENTOS, "Combustível", Icons.Outlined.LocalGasStation),
        ItemBarra(Rotas.USUARIOS, "Pessoas", Icons.Outlined.Groups),
    )
    // Motorista: menos destinos, nada administrativo.
    Perfil.MOTORISTA -> listOf(
        ItemBarra(Rotas.VIAGENS, "Viagens", Icons.Outlined.Route),
        ItemBarra(Rotas.ABASTECIMENTOS, "Combustível", Icons.Outlined.LocalGasStation),
        ItemBarra(Rotas.CONTA, "Conta", Icons.Outlined.Person),
    )
}

private val argumentoIdOpcional = navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null }

@Composable
fun EixoApp() {
    EixoTema {
        val nav = rememberNavController()
        val usuario by Sessao.usuario.collectAsState()
        val entradaAtual by nav.currentBackStackEntryAsState()
        val perfil = usuario?.perfil ?: Perfil.ADMIN

        val barra: @Composable () -> Unit = {
            EixoBarraInferior(
                itens = itensDaBarra(perfil),
                rotaAtual = entradaAtual?.destination?.route,
                aoNavegar = { rota ->
                    nav.navigate(rota) {
                        popUpTo(Rotas.inicioDe(perfil)) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
        val voltar: () -> Unit = { nav.popBackStack() }
        val sair: () -> Unit = {
            Sessao.sair()
            nav.irParaLogin()
        }

        NavHost(navController = nav, startDestination = Rotas.LOGIN) {
            composable(Rotas.LOGIN) {
                LoginTela(
                    aoEntrar = { u ->
                        nav.navigate(Rotas.inicioDe(u.perfil)) { popUpTo(Rotas.LOGIN) { inclusive = true } }
                    },
                    aoAbrirCatalogo = { nav.navigate(Rotas.CATALOGO) },
                )
            }
            composable(Rotas.CATALOGO) { CatalogoTela(aoVoltar = voltar) }
            composable(Rotas.CONTA) {
                ContaTela(barra = barra, aoSair = sair, aoAbrirCatalogo = { nav.navigate(Rotas.CATALOGO) })
            }

            composable(Rotas.PAINEL) {
                PainelTela(barra = barra, aoSair = sair, aoAbrirViagens = { nav.navigate(Rotas.VIAGENS) })
            }

            composable(Rotas.USUARIOS) {
                UsuariosTela(barra = barra, aoNovo = { nav.navigate(Rotas.usuarioForm()) }, aoAbrir = { nav.navigate(Rotas.usuarioForm(it)) })
            }
            composable(Rotas.USUARIO_FORM, listOf(argumentoIdOpcional)) { entrada ->
                UsuarioFormTela(usuarioId = entrada.arguments?.getString("id"), aoVoltar = voltar)
            }

            composable(Rotas.CARGAS) {
                CargasTela(barra = barra, aoNova = { nav.navigate(Rotas.cargaForm()) }, aoAbrir = { nav.navigate(Rotas.cargaForm(it)) })
            }
            composable(Rotas.CARGA_FORM, listOf(argumentoIdOpcional)) { entrada ->
                CargaFormTela(cargaId = entrada.arguments?.getString("id"), aoVoltar = voltar)
            }

            composable(Rotas.VIAGENS) {
                ViagensTela(barra = barra, aoNova = { nav.navigate(Rotas.viagemForm()) }, aoAbrir = { nav.navigate(Rotas.viagemDetalhe(it)) })
            }
            composable(Rotas.VIAGEM_FORM, listOf(argumentoIdOpcional)) { entrada ->
                ViagemFormTela(viagemId = entrada.arguments?.getString("id"), aoVoltar = voltar)
            }
            composable(Rotas.VIAGEM_DETALHE, listOf(navArgument("id") { type = NavType.StringType })) { entrada ->
                val id = entrada.arguments?.getString("id").orEmpty()
                ViagemDetalheTela(
                    viagemId = id,
                    aoVoltar = voltar,
                    aoEditar = { nav.navigate(Rotas.viagemForm(id)) },
                    aoRegistrarAbastecimento = { nav.navigate(Rotas.abastecimentoForm(viagemId = it)) },
                )
            }

            composable(Rotas.ABASTECIMENTOS) {
                AbastecimentosTela(barra = barra, aoNovo = { nav.navigate(Rotas.abastecimentoForm()) }, aoAbrir = { nav.navigate(Rotas.abastecimentoForm(id = it)) })
            }
            composable(
                Rotas.ABASTECIMENTO_FORM,
                listOf(
                    argumentoIdOpcional,
                    navArgument("viagemId") { type = NavType.StringType; nullable = true; defaultValue = null },
                ),
            ) { entrada ->
                AbastecimentoFormTela(
                    abastecimentoId = entrada.arguments?.getString("id"),
                    viagemId = entrada.arguments?.getString("viagemId"),
                    aoVoltar = voltar,
                )
            }
        }
    }
}

private fun NavHostController.irParaLogin() {
    navigate(Rotas.LOGIN) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
